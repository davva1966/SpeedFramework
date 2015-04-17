// {{CopyrightNotice}}

package com.ss.framework.dialogue.ip;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.ss.framework.dialogue.api.DialogueListener;
import com.ss.framework.dialogue.api.DialogueServerMessage;


public class SocketDataProcessor implements Runnable {

	protected boolean stop = false;

	private BlockingQueue<DataEvent> queue = new LinkedBlockingQueue<DataEvent>();

	protected SocketDialogue dialogue;

	protected class DataEvent {

		protected SocketDialogueServer server;

		protected SocketChannel socket;

		protected byte[] data;

		protected String messageType;

		public DataEvent(SocketDialogueServer server, SocketChannel socket, byte[] data, String messageType) throws IOException {
			this.server = server;
			this.socket = socket;
			this.data = data;
			this.messageType = messageType;
		}

	}

	public SocketDataProcessor() {
		this(null);
	}

	public SocketDataProcessor(SocketDialogue dialogue) {
		this.dialogue = dialogue;
	}

	public void processData(SocketDialogueServer server, SocketChannel socket, byte[] data, String messageType) throws IOException {
		synchronized (queue) {
			queue.add(new DataEvent(server, socket, data, messageType));
			queue.notify();
		}
	}

	public void run() {

		DataEvent dataEvent = null;

		while (stop == false) {
			// Wait for data to become available
			try {
				dataEvent = queue.poll(1, TimeUnit.SECONDS);
				while (dataEvent == null && stop == false)
					dataEvent = queue.poll(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				if (stop)
					break;
				else
					continue;
			}

			if (dataEvent != null) {
				DialogueServerMessage message = new SocketDialogueServerMessage(dataEvent.server, dataEvent.socket, dataEvent.data, dataEvent.messageType.equals(SocketDialogueServerMessage.TYPE_RESPONSE));

				// process data
				if (dialogue != null) {
					Iterator<DialogueListener> iterator = dialogue.getListeners().iterator();
					while (iterator.hasNext()) {
						DialogueListener listener = iterator.next();
						listener.messageRecieved(message);
					}
				}
			}
		}
	}

	public void start() {
		stop = false;
	}

	public void stop() {
		stop = true;
		synchronized (queue) {
			queue.notify();
		}
	}
}
