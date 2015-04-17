// {{CopyrightNotice}}

package com.ss.framework.dialogue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ss.framework.dialogue.api.Destination;
import com.ss.framework.dialogue.api.Dialogue;
import com.ss.framework.dialogue.api.DialogueListener;
import com.ss.framework.dialogue.api.DialogueResponse;
import com.ss.framework.dialogue.api.DialogueResponseSet;


public abstract class AbstractDialogue implements Dialogue {

	protected DialogueEngine engine = null;

	protected Map<String, Destination> destinationMap = Collections.synchronizedMap(new HashMap<String, Destination>());

	protected List<DialogueListener> listeners = Collections.synchronizedList(new ArrayList<DialogueListener>());

	protected Map<String, Object> options = new HashMap<String, Object>();

	public AbstractDialogue() {
		super();

	}

	public AbstractDialogue(DialogueEngine engine) {
		super();
		this.engine = engine;

	}

	public void addDestination(Destination destination) throws DialogueException {
		if (destinationMap.containsKey(destination.getAlias()))
			throw new DialogueException("Destination with alias: " + destination.getAlias() + " already active in this dialogue");
		destinationMap.put(destination.getAlias(), destination);
	}

	public void removeDestination(String destinationAlias) {
		destinationMap.remove(destinationAlias);
	}

	public List<String> getDestinationAliases() {
		List<String> list = new ArrayList<String>();
		list.addAll(destinationMap.keySet());
		return list;
	}

	public List<Destination> getDestinations() {
		List<Destination> list = new ArrayList<Destination>();
		list.addAll(destinationMap.values());
		return list;
	}

	public Destination getDestination(String destinationAlias) {
		return destinationMap.get(destinationAlias);
	}

	public void addListener(DialogueListener listener) {
		listeners.add(listener);
	}

	public void removeListener(DialogueListener listener) {
		listeners.remove(listener);

	}

	public List<DialogueListener> getListeners() {
		return listeners;
	}

	public void sendMessage(String destinationAlias, String message) throws DialogueConnectionException, DialogueException {
		sendMessage(destinationAlias, message.getBytes());

	}

	public void sendMessage(List<String> destinationAliases, String message) throws DialogueExceptionSet {
		sendMessage(destinationAliases, message.getBytes());

	}

	public void sendMessage(String destinationAlias, byte[] message) throws DialogueConnectionException, DialogueException {
		engine.send(destinationAlias, message);

	}

	public void sendMessage(List<String> destinationAliases, byte[] message) throws DialogueExceptionSet {
		engine.send(destinationAliases, message);

	}

	public void sendMessageToAll(String message) throws DialogueExceptionSet {
		sendMessageToAll(message.getBytes());

	}

	public void sendMessageToAll(byte[] message) throws DialogueExceptionSet {
		engine.sendToAll(message);

	}

	public DialogueResponse sendResponseMessage(String destinationAlias, String message, int timeoutMillis) throws DialogueException {
		return sendResponseMessage(destinationAlias, message.getBytes(), timeoutMillis);

	}

	public DialogueResponse sendResponseMessage(String destinationAlias, byte[] message, int timeoutMillis) throws DialogueException {
		if (destinationMap.containsKey(destinationAlias) == false)
			throw new DialogueException("Destination with alias: " + destinationAlias + " not active in this dialogue");
		DialogueResponseHandler handler = new DialogueResponseHandlerImpl(destinationAlias);
		engine.send(destinationAlias, message, handler);
		return handler.waitForResponse(timeoutMillis).getResponse(destinationAlias);

	}

	public DialogueResponseSet sendResponseMessage(List<String> destinationAliases, String message, int timeoutMillis) throws DialogueException {
		return sendResponseMessage(destinationAliases, message.getBytes(), timeoutMillis);

	}

	public DialogueResponseSet sendResponseMessage(List<String> destinationAliases, byte[] message, int timeoutMillis) throws DialogueException {
		DialogueResponseHandler handler = new DialogueResponseHandlerImpl(destinationAliases);
		try {
			engine.send(destinationAliases, message, handler);
		} catch (DialogueExceptionSet e) {
			throw new DialogueException(e.getMessage());
		}

		return handler.waitForResponse(timeoutMillis);

	}

	public DialogueResponseSet sendResponseMessageToAll(String message, int timeoutMillis) throws DialogueException {
		return sendResponseMessageToAll(message.getBytes(), timeoutMillis);

	}

	public DialogueResponseSet sendResponseMessageToAll(byte[] message, int timeoutMillis) throws DialogueException {
		DialogueResponseHandler handler = new DialogueResponseHandlerImpl();
		try {
			engine.sendToAll(message, handler);
		} catch (DialogueExceptionSet e) {
			throw new DialogueException(e.getMessage());
		}
		return handler.waitForResponse(timeoutMillis);

	}

	public void setOptions(Map<String, Object> options) throws DialogueUnsupportedOptionException {
		this.options = options;
	}

	public Map<String, Object> getOptions() {
		return options;
	}

	public void start() throws DialogueException {
		engine.start();

	}

	public void stop() {
		engine.stop();

	}

	public void finalize() throws Throwable {
		try {
			stop();
		} finally {
			super.finalize();
		}
	}

}
