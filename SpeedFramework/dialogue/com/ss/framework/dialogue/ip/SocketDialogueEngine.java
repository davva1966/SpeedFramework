// {{CopyrightNotice}}

package com.ss.framework.dialogue.ip;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.List;

import com.ss.framework.dialogue.DialogueConnectionException;
import com.ss.framework.dialogue.DialogueEngine;
import com.ss.framework.dialogue.DialogueException;
import com.ss.framework.dialogue.DialogueExceptionSet;
import com.ss.framework.dialogue.DialogueResponseHandler;
import com.ss.framework.dialogue.api.Dialogue;


public class SocketDialogueEngine implements DialogueEngine {

	protected SocketDialogueServer server;

	protected SocketDialogueClient client;

	protected SocketDialogue dialogue;

	public SocketDialogueEngine(int localPort) throws IOException {
		this(null, localPort);

	}

	public SocketDialogueEngine(InetAddress localAdress, int localPort) throws IOException {
		super();
		if (localAdress == null && localPort > 0)
			localAdress = InetAddress.getLocalHost();

		if (localPort > 0) {
			SocketDataProcessor dataProcessor = new SocketDataProcessor((SocketDialogue) getDialogue());
			server = new SocketDialogueServer(localAdress, localPort, dataProcessor);
		}

		client = new SocketDialogueClient((SocketDialogue) getDialogue());

	}

	public synchronized Dialogue getDialogue() {
		if (dialogue == null)
			dialogue = new SocketDialogue(this);

		return dialogue;

	}

	public void send(String destinationAlias, byte[] message) throws DialogueConnectionException, DialogueException {
		try {
			send(destinationAlias, message, null);
		} catch (DialogueException e) {
			if (e.getCause() instanceof ConnectException)
				throw new DialogueConnectionException(e.getCause());
			else
				throw e;
		}

	}

	public void send(List<String> destinationAliases, byte[] message) throws DialogueExceptionSet {
		send(destinationAliases, message, null);

	}

	public void sendToAll(byte[] message) throws DialogueExceptionSet {
		sendToAll(message, null);

	}

	public void send(String destinationAlias, byte[] message, DialogueResponseHandler responseHandler) throws DialogueException {
		try {
			if (client == null || client.isStarted() == false)
				throw new DialogueException("Dialogue not started or not started in client mode");
			client.send(destinationAlias, message, responseHandler);
		} catch (IOException e) {
			throw new DialogueException(e);
		}

	}

	public void send(List<String> destinationAliases, byte[] message, DialogueResponseHandler responseHandler) throws DialogueExceptionSet {

		if (client == null || client.isStarted() == false)
			throw new DialogueExceptionSet("Dialogue not started or not started in client mode");
		client.send(destinationAliases, message, responseHandler);

	}

	public void sendToAll(byte[] message, DialogueResponseHandler responseHandler) throws DialogueExceptionSet {
		if (client == null || client.isStarted() == false)
			throw new DialogueExceptionSet("Dialogue not started or not started in client mode");
		client.sendToAll(message, responseHandler);

	}

	public void start() throws DialogueException {
		try {
			if (server != null)
				server.start();
			if (client != null)
				client.start();

			// Wait for the services to start
			int waitTimeRemaining = 5000;
			while (waitTimeRemaining > 0 && ((server != null && !server.isStarted()) || (client != null && !client.isStarted()))) {
				try {
					Thread.sleep(200);
				} catch (Exception e) {
				}
				waitTimeRemaining -= 200;
			}

		} catch (IOException e) {
			throw new DialogueException(e);
		}

	}

	public void stop() {
		if (server != null)
			server.stop();
		if (client != null)
			client.stop();

	}

}
