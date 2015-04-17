package com.ss.framework.dialogue.ip;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ss.framework.dialogue.DialogueExceptionSet;
import com.ss.framework.dialogue.DialogueResponseHandler;
import com.ss.framework.dialogue.api.Destination;


public class SocketDialogueClient extends NonBlockingSocketTransport {

	protected boolean started = false;

	protected ThreadGroup threadGroup;

	protected SocketChannelMonitor socketChannelMonitor;

	protected Map<SocketChannel, DialogueResponseHandler> responseHandlerMap = Collections.synchronizedMap(new HashMap<SocketChannel, DialogueResponseHandler>());

	protected SocketDialogue dialogue;

	protected Map<SocketChannel, String> destinationSocketMap = Collections.synchronizedMap(new HashMap<SocketChannel, String>());

	protected Map<SocketChannel, IOException> socketChannelExceptionMap = Collections.synchronizedMap(new HashMap<SocketChannel, IOException>());

	public SocketDialogueClient(SocketDialogue dialogue) throws IOException {
		this.dialogue = dialogue;
	}

	protected Selector initSelector() throws IOException {
		// Create a new selector
		return Selector.open();
	}

	protected synchronized SocketChannel initiateConnection(String destinationAlias) throws IOException {

		SocketDestination dest = (SocketDestination) dialogue.getDestination(destinationAlias);
		if (dest == null)
			throw new IOException("Destination with alias: " + destinationAlias + ", not active in dialogue");

		SocketChannel socketChannel = dest.socketChannel;

		boolean expired = socketChannelMonitor != null && socketChannelMonitor.socketChannelExpired(socketChannel);
		if (socketChannel == null || (socketChannel.isConnectionPending() == false && socketChannel.isConnected() == false) || expired) {

			// Cleanup if the socket channel has been closed
			if (socketChannel != null && (socketChannel.isConnectionPending() == false && socketChannel.isConnected() == false))
				cleanup(socketChannel);

			socketChannel = SocketChannel.open();
			dest.socketChannel = socketChannel;
			socketChannel.configureBlocking(false);

			// Kick off connection establishment
			socketChannel.connect(new InetSocketAddress(dest.hostAddress, dest.port));

			destinationSocketMap.put(socketChannel, destinationAlias);

			if (socketChannelMonitor != null)
				socketChannelMonitor.addSocketChannel(socketChannel);

			// Queue a channel registration. As part of the registration we'll register
			// an interest in connection events. These are raised when a channel
			// is ready to complete connection establishment.
			if (changeRequests.offer(new ChangeRequest(socketChannel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT)) == false)
				throw new IOException("Internal request buffer is full");

		} else {
			ChangeRequest writeRequest = new ChangeRequest(socketChannel, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE);
			if (changeRequests.contains(writeRequest) == false)
				if (changeRequests.offer(writeRequest) == false)
					throw new IOException("Internal request buffer is full");
		}

		return socketChannel;
	}

	protected void processReadData(SocketChannel socketChannel, byte[] data, String messageType) throws IOException {
		// Handle the response
		handleResponse(socketChannel, data, messageType);

	}

	public void send(List<String> destinationAliases, byte[] data, DialogueResponseHandler handler) throws DialogueExceptionSet {
		DialogueExceptionSet exceptionSet = null;
		for (String alias : destinationAliases) {
			try {
				send(alias, data, handler);
			} catch (Exception e) {
				if (exceptionSet == null)
					exceptionSet = new DialogueExceptionSet("One or more errors occured during send operation.");
				exceptionSet.addException(alias, e);
			}
		}

		if (exceptionSet != null)
			throw exceptionSet;

	}

	public void sendToAll(byte[] data, DialogueResponseHandler handler) throws DialogueExceptionSet {
		if (handler != null)
			handler.setDestinationAliases(dialogue.getDestinationAliases());

		DialogueExceptionSet exceptionSet = null;
		for (Destination dest : dialogue.getDestinations()) {
			try {
				send(dest.getAlias(), data, handler);
			} catch (Exception e) {
				if (exceptionSet == null)
					exceptionSet = new DialogueExceptionSet("One or more errors occured during send operation.");
				exceptionSet.addException(dest.getAlias(), e);
			}
		}

		if (exceptionSet != null)
			throw exceptionSet;

	}

	public void send(String destinationAlias, byte[] data, DialogueResponseHandler handler) throws IOException {
		// Start a new connection
		SocketChannel socketChannel = initiateConnection(destinationAlias);
		// Send data
		send(socketChannel, data, handler);

	}

	protected void send(SocketChannel socketChannel, byte[] data, DialogueResponseHandler handler) throws IOException {
		// Register the response handler
		if (handler != null)
			responseHandlerMap.put(socketChannel, handler);

		// And queue the data we want written
		synchronized (pendingData) {
			BlockingQueue<SendRequest> queue = pendingData.get(socketChannel);
			if (queue == null) {
				queue = new LinkedBlockingQueue<SendRequest>();
				pendingData.put(socketChannel, queue);
			}

			SendRequest sendRequest = new SendRequest(ByteBuffer.wrap(data), handler != null);

			if (queue.offer(sendRequest) == false)
				throw new IOException("Dialogue Client data buffer is full");

		}

		// Finally, wake up our selecting thread so it can make the required changes
		selector.wakeup();
		synchronized (this) {
			while (changeRequests.isEmpty() == false) {
				try {
					wait();
				} catch (Exception e) {
					break;
				}
			}
		}

		// Wait for the connection to complete or throw an exception. If handler is null this is a normal message so we throw
		// exception if the connection failed.
		if (handler == null) {
			synchronized (this) {
				while (socketChannel.isConnected() == false && socketChannelExceptionMap.containsKey(socketChannel) == false) {
					try {
						wait();
					} catch (Exception e) {
						break;
					}
					if (socketChannelExceptionMap.containsKey(socketChannel))
						throw socketChannelExceptionMap.get(socketChannel);
				}
			}
			// This is a response message. Exceptions will be thrown when the response is accessed.
		} else {
			synchronized (this) {
				while (socketChannel.isConnected() == false && handler.hasException() == false) {
					try {
						wait();
					} catch (Exception e) {
						break;
					}
				}
			}

		}

	}

	protected void write(SelectionKey key) throws IOException {
		if (socketChannelMonitor != null)
			socketChannelMonitor.nudge((SocketChannel) key.channel());
		super.write(key);
	}

	protected void read(SelectionKey key) throws IOException {
		if (socketChannelMonitor != null)
			socketChannelMonitor.nudge((SocketChannel) key.channel());
		super.read(key);

	}

	protected void handleResponse(SocketChannel socketChannel, byte[] data, String messageType) throws IOException {

		// Look up the handler for this channel
		DialogueResponseHandler responseHandler = responseHandlerMap.get(socketChannel);

		if (responseHandler != null) {
			// Make a correctly sized copy of the data before handing it
			// to the client
			byte[] responseData = new byte[data.length];
			System.arraycopy(data, 0, responseData, 0, data.length);

			// And pass the response to it
			String destinationAlias = destinationSocketMap.get(socketChannel);
			responseHandler.handleResponse(destinationAlias, responseData);

		}

	}

	protected void finishConnection(SelectionKey key) {
		synchronized (this) {
			try {
				super.finishConnection(key);
			} catch (IOException e) {
				// Notify the response handler that a connection exception occured
				DialogueResponseHandler responseHandler = responseHandlerMap.get(key.channel());
				if (responseHandler != null) {
					String destinationAlias = destinationSocketMap.get(key.channel());
					responseHandler.handleException(destinationAlias, e);
				} else {
					socketChannelExceptionMap.put((SocketChannel) key.channel(), e);
				}
			} finally {
				notify();
			}
		}

	}

	public synchronized void start() throws IOException {
		if (threadGroup == null) {
			super.start();
			threadGroup = new ThreadGroup("DialogueClient");
			new Thread(threadGroup, this, "DialogueClient").start();
			if (socketChannelMonitor == null)
				socketChannelMonitor = new SocketChannelMonitor();

			// Set socket channel timeout/lifetime values (if specified)
			Integer idleTimeout = (Integer) dialogue.getOptions().get(SocketDialogue.SOCKET_IDLE_TIMEOUT_SECONDS);
			if (idleTimeout != null)
				socketChannelMonitor.setSocketIdleTimeout(idleTimeout);
			Integer lifetime = (Integer) dialogue.getOptions().get(SocketDialogue.SOCKET_LIFETIME_SECONDS);
			if (lifetime != null)
				socketChannelMonitor.setSocketLifetime(lifetime);

			// Set debug mode (if specified)
			Boolean debug = (Boolean) dialogue.getOptions().get(SocketDialogue.DEBUG);
			if (debug != null)
				socketChannelMonitor.setDebugMode(debug);

			socketChannelMonitor.start();
			new Thread(threadGroup, socketChannelMonitor, "SocketMonitor").start();
			started = true;
		}

	}

	public synchronized void stop() {
		if (threadGroup != null) {
			started = false;
			super.stop();

			// Wait for client to stop (wait max 10 seconds)
			int elapsedTime = 0;
			while (threadGroup.activeCount() > 1 && elapsedTime < 10000) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				elapsedTime += 100;
			}

			if (socketChannelMonitor != null)
				socketChannelMonitor.stop();

			// Wait for socket monitor to stop (wait max 10 seconds)
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
		}

		threadGroup = null;

	}

	public boolean isStarted() {
		return started;
	}

	protected void cleanup(SocketChannel socketChannel) {
		// Remove old socket channel mappings
		responseHandlerMap.remove(socketChannel);
		destinationSocketMap.remove(socketChannel);
		socketChannelExceptionMap.remove(socketChannel);
		pendingData.remove(socketChannel);

	}

}
