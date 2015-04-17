// {{CopyrightNotice}}

package com.ss.framework.dialogue.ip;

import java.nio.channels.SocketChannel;

import com.ss.framework.dialogue.AbstractDialogueServerMessage;
import com.ss.framework.dialogue.DialogueException;


public class SocketDialogueServerMessage extends AbstractDialogueServerMessage {

	public static final String TYPE_NORMAL = "N";

	public static final String TYPE_RESPONSE = "R";

	protected SocketDialogueServer server = null;

	protected SocketChannel socketChannel = null;

	protected boolean responseRequested = false;

	public SocketDialogueServerMessage(SocketDialogueServer server, SocketChannel socketChannel, byte[] data, boolean responseRequested) {
		super(data);
		this.server = server;
		this.socketChannel = socketChannel;
		this.responseRequested = responseRequested;
	}

	public boolean responseRequested() {
		return responseRequested;
	}

	public void respond(String response) throws DialogueException {
		respond(response.getBytes());

	}

	public void respond(byte[] response) throws DialogueException {
		if (responseRequested == false)
			throw new DialogueException("Attempt to respond to a message that did not request a response");

		try {
			server.send(socketChannel, response);
		} catch (Exception e) {
			throw new DialogueException("Error when responding to message. Cause: " + e);
		}

	}

}
