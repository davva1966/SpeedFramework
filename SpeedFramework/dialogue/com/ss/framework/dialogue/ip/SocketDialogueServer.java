package com.ss.framework.dialogue.ip;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketDialogueServer extends NonBlockingSocketTransport {

	protected boolean started = false;

	protected InetAddress hostAddress;

	protected int port;

	protected ServerSocketChannel serverChannel;

	protected ThreadGroup threadGroup;

	protected SocketDataProcessor dataProcessor;

	public SocketDialogueServer(InetAddress hostAddress, int port, SocketDataProcessor dataProcessor) throws IOException {
		this.hostAddress = hostAddress;
		this.port = port;
		this.dataProcessor = dataProcessor;
	}

	protected Selector initSelector() throws IOException {
		// Create a new selector
		Selector socketSelector = Selector.open();

		// Create a new non-blocking server socket channel
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress(hostAddress, port);
		serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}

	protected void processReadData(SocketChannel socketChannel, byte[] data, String messageType) throws IOException {
		byte[] dataCopy = new byte[data.length];
		System.arraycopy(data, 0, dataCopy, 0, data.length);
		dataProcessor.processData(this, socketChannel, dataCopy, messageType);
	}

	public void send(SocketChannel socket, byte[] data) throws IOException {
		// Indicate we want the interest ops set changed
		if (changeRequests.offer(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE)) == false)
			throw new IOException("Internal request buffer is full");

		// And queue the data we want written
		synchronized (pendingData) {
			BlockingQueue<SendRequest> queue = pendingData.get(socket);
			if (queue == null) {
				queue = new LinkedBlockingQueue<SendRequest>();
				pendingData.put(socket, queue);
			}

			SendRequest sendRequest = new SendRequest(ByteBuffer.wrap(data), false);

			if (queue.offer(sendRequest) == false)
				throw new IOException("Dialogue Server data buffer is full");
		}

		// Finally, wake up our selecting thread so it can make the required changes
		try {
			selector.wakeup();
			synchronized (this) {
				while (changeRequests.isEmpty() == false) {
					wait();
				}
			}
		} catch (Exception e) {
		}
	}

	public void start() throws IOException {
		if (threadGroup == null) {
			super.start();
			threadGroup = new ThreadGroup("DialogueServer");
			if (dataProcessor == null)
				dataProcessor = new SocketDataProcessor();
			dataProcessor.start();
			new Thread(threadGroup, dataProcessor, "DataProcessor").start();
			new Thread(threadGroup, this, "DialogueServer").start();
			started = true;
		}

	}

	public void stop() {
		if (threadGroup != null) {
			started = false;
			super.stop();

			// Wait for server to stop (wait max 10 seconds)
			int elapsedTime = 0;
			while (threadGroup.activeCount() > 1 && elapsedTime < 10000) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				elapsedTime += 100;
			}

			if (dataProcessor != null)
				dataProcessor.stop();

			// Wait for data processor to stop (wait max 10 seconds)
			elapsedTime = 0;
			while (threadGroup.activeCount() > 0 && elapsedTime < 10000) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				elapsedTime += 100;
			}

			// If any thread is still alive, terminate it
			if (threadGroup.activeCount() > 0)
				threadGroup.interrupt();

			// Make sure the server socket channel is closed
			try {
				if (serverChannel != null)
					serverChannel.close();
			} catch (IOException e) {
			}
		}

		threadGroup = null;

	}

	public boolean isStarted() {
		return started;
	}

}
