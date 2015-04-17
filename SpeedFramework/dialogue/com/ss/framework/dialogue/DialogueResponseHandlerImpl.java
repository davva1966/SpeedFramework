package com.ss.framework.dialogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ss.framework.dialogue.api.DialogueResponseSet;


public class DialogueResponseHandlerImpl implements DialogueResponseHandler {

	protected List<String> destinationAliases = new ArrayList<String>();

	protected Map<String, Exception> exceptionMap = new HashMap<String, Exception>();

	protected Map<String, byte[]> responseMap = new HashMap<String, byte[]>();

	public DialogueResponseHandlerImpl() {
		super();

	}

	public DialogueResponseHandlerImpl(String destinationAlias) {
		this();
		destinationAliases.add(destinationAlias);

	}

	public DialogueResponseHandlerImpl(List<String> destinationAliases) {
		this();
		setDestinationAliases(destinationAliases);

	}

	public void setDestinationAliases(List<String> destinationAliases) {
		this.destinationAliases = destinationAliases;
	}

	public synchronized void handleException(String destinationAlias, Exception exception) {
		exceptionMap.put(destinationAlias, exception);
		if (responseMap.size() + exceptionMap.size() >= destinationAliases.size())
			notify();

	}

	public synchronized boolean handleResponse(String destinationAlias, byte[] response) {
		responseMap.put(destinationAlias, response);
		if (responseMap.size() + exceptionMap.size() >= destinationAliases.size())
			notify();
		return true;
	}

	public synchronized DialogueResponseSet waitForResponse(int timeoutMillis) {
		if (responseMap.size() + exceptionMap.size() < destinationAliases.size()) {
			try {
				wait(timeoutMillis);
			} catch (InterruptedException e) {
			}
		}

		DialogueResponseSetImpl responseSet = new DialogueResponseSetImpl();
		try {
			for (String alias : destinationAliases) {
				if (exceptionMap.containsKey(alias))
					responseSet.addResponse(alias, new DialogueResponseImpl(exceptionMap.get(alias)));
				else
					responseSet.addResponse(alias, new DialogueResponseImpl(responseMap.get(alias)));
			}
		} catch (DialogueException e) {
			System.out.println(e);
		}

		return responseSet;
	}

	public boolean hasException() {
		return exceptionMap.size() > 0;
	}
}
