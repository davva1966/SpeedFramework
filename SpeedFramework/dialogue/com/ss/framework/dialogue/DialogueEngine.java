// {{CopyrightNotice}}

package com.ss.framework.dialogue;

import java.util.List;

import com.ss.framework.dialogue.api.Dialogue;


public interface DialogueEngine {

	public Dialogue getDialogue();

	public void send(String destinationAlias, byte[] message) throws DialogueConnectionException, DialogueException;

	public void send(List<String> destinationAliases, byte[] message) throws DialogueExceptionSet;

	public void send(String destinationAlias, byte[] message, DialogueResponseHandler responseHandler) throws DialogueException;

	public void send(List<String> destinationAliases, byte[] message, DialogueResponseHandler responseHandler) throws DialogueExceptionSet;

	public void sendToAll(byte[] message) throws DialogueExceptionSet;

	public void sendToAll(byte[] message, DialogueResponseHandler responseHandler) throws DialogueExceptionSet;

	public void start() throws DialogueException;

	public void stop();
}
