// {{CopyrightNotice}}

package com.ss.framework.dialogue.ip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class NonBlockingSocketTransport implements Runnable {

	protected boolean stop = false;

	// The selector we'll be monitoring
	protected Selector selector;

	// A list of ChangeRequest instances
	protected BlockingQueue<ChangeRequest> changeRequests = new LinkedBlockingQueue<ChangeRequest>();

	// Maps a SocketChannel to a list of ByteBuffer instances
	protected Map<SocketChannel, BlockingQueue<SendRequest>> pendingData = Collections.synchronizedMap(new HashMap<SocketChannel, BlockingQueue<SendRequest>>());

	protected class ChangeRequest {

		public static final int REGISTER = 1;

		public static final int CHANGEOPS = 2;

		public SocketChannel socketChannel;

		public int type;

		public int ops;

		public ChangeRequest(SocketChannel socketChannel, int type, int ops) {
			this.socketChannel = socketChannel;
			this.type = type;
			this.ops = ops;
		}

		public boolean equals(Object o) {
			if (o instanceof ChangeRequest) {
				ChangeRequest c = (ChangeRequest) o;
				return c.socketChannel.equals(this.socketChannel) && c.type == this.type && c.ops == this.ops;
			}

			return false;
		}
	}

	protected class SendRequest {

		public ByteBuffer data;

		public boolean wantResponse = false;

		public SendRequest(ByteBuffer data, boolean wantResponse) {
			this.data = data;
			this.wantResponse = wantResponse;
		}

		public ByteBuffer getRequestTypeBuffer() {
			if (wantResponse)
				return ByteBuffer.wrap(SocketDialogueServerMessage.TYPE_RESPONSE.getBytes());

			return ByteBuffer.wrap(SocketDialogueServerMessage.TYPE_NORMAL.getBytes());

		}

	}

	protected abstract Selector initSelector() throws IOException;

	protected void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		BlockingQueue<SendRequest> queue = pendingData.get(socketChannel);

		// Write until there's not more data ...
		while (!queue.isEmpty()) {
			SendRequest sendRequest = queue.poll();
			// Write message type (If response is requested or not)
			socketChannel.write(sendRequest.getRequestTypeBuffer());
			// Write message length
			socketChannel.write(intToByteBuffer(sendRequest.data.array().length));
			// Write message
			socketChannel.write(sendRequest.data);
			if (sendRequest.data.remaining() > 0) {
				// ... or the socket's buffer fills up
				break;
			}
		}

		if (queue.isEmpty()) {
			// We wrote away all data, so we're no longer interested
			// in writing on this socket. Switch back to waiting for
			// data.
			key.interestOps(SelectionKey.OP_READ);
		}
	}

	protected void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Create a read buffer to recieve the message length
		ByteBuffer readBuffer = ByteBuffer.allocate(1);

		// Attempt to read off the channel
		int numRead = 0;
		String messageType = SocketDialogueServerMessage.TYPE_NORMAL;
		try {

			// Read message type (If response is requested or not)
			numRead = socketChannel.read(readBuffer);
			while (numRead < 1) {
				numRead = socketChannel.read(readBuffer);
				if (numRead == -1)
					break;
			}

			if (numRead > 0) {
				messageType = new String(readBuffer.array());
				if (messageType.equals(SocketDialogueServerMessage.TYPE_NORMAL) == false && messageType.equals(SocketDialogueServerMessage.TYPE_RESPONSE) == false)
					throw new RuntimeException("Invalid message type (" + messageType + ") received");
			}

			// Read message size
			readBuffer = ByteBuffer.allocate(4);
			numRead = socketChannel.read(readBuffer);
			int totalRead = numRead;
			while (totalRead < 4) {
				numRead = socketChannel.read(readBuffer);
				if (numRead == -1)
					break;
				totalRead += numRead;
			}

			if (totalRead > 0 && totalRead != 4)
				throw new RuntimeException("Invalid message length received");

			if (totalRead > 0) {

				int messageSize = byteBufferToInt(readBuffer);

				// Create a read buffer to recieve the message
				readBuffer = ByteBuffer.allocate(messageSize);

				numRead = socketChannel.read(readBuffer);

				totalRead = numRead;
				while (totalRead < messageSize) {
					numRead = socketChannel.read(readBuffer);
					if (numRead == -1)
						break;
					totalRead += numRead;
				}

				if (totalRead < messageSize)
					throw new RuntimeException("Actual length of received message does not match specified message length");
			}

		} catch (IOException e) {
			// The remote entity forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			return;
		}

		processReadData(socketChannel, readBuffer.array(), messageType);

	}

	protected void processReadData(SocketChannel socketChannel, byte[] data, String messageType) throws IOException {
	}

	public void run() {
		while (true) {

			try {
				// Process any pending changes
				performPendingChanges();

				// Wait for an event one of the registered channels
				selector.select();

				// Iterate over the set of keys for which events are available
				Iterator selectedKeys = selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isAcceptable()) {
						accept(key);
					} else if (key.isConnectable()) {
						try {
							finishConnection(key);
						} catch (Exception e) {
						}
					} else if (key.isReadable()) {
						read(key);
					} else if (key.isWritable()) {
						write(key);
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			}

			if (stop == true && changeRequests.isEmpty())
				break;
		}

		try {
			if (selector != null)
				selector.close();

		} catch (Exception e) {
		}

	}

	protected void accept(SelectionKey key) throws IOException {
		// For an accept to be pending the channel must be a server socket channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating
		// we'd like to be notified when there's data waiting to be read
		socketChannel.register(selector, SelectionKey.OP_READ);
	}

	protected void finishConnection(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			// Cancel the channel's registration with our selector
			key.cancel();
			throw e;
		}

		// Register an interest in writing on this channel
		key.interestOps(SelectionKey.OP_WRITE);

	}

	public void start() throws IOException {
		stop = false;
		selector = initSelector();

	}

	public void stop() {
		stop = true;
		if (selector != null)
			selector.wakeup();

	}

	protected ByteBuffer intToByteBuffer(int i) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);
		dataStream.writeInt(i);
		return ByteBuffer.wrap(byteStream.toByteArray());

	}

	protected int byteBufferToInt(ByteBuffer buf) throws IOException {
		InputStream byteStream = new ByteArrayInputStream(buf.array());
		DataInputStream dataStream = new DataInputStream(byteStream);
		return dataStream.readInt();
	}

	protected synchronized void performPendingChanges() {
		try {
			// Process any pending changes
			ChangeRequest change = changeRequests.poll();
			while (change != null) {
				switch (change.type) {
				case ChangeRequest.CHANGEOPS:
					SelectionKey key = change.socketChannel.keyFor(selector);
					key.interestOps(change.ops);
				case ChangeRequest.REGISTER:
					change.socketChannel.register(selector, change.ops);
					return;
				}
				change = changeRequests.poll();
			}

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			notify();
		}
	}

}
