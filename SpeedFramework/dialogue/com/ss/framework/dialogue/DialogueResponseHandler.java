// {{CopyrightNotice}}

package com.ss.framework.dialogue;

import java.util.List;

import com.ss.framework.dialogue.api.DialogueResponseSet;


public interface DialogueResponseHandler {

	public void handleException(String destinationAlias, Exception exception);

	public boolean handleResponse(String destinationAlias, byte[] response);

	public DialogueResponseSet waitForResponse(int timeoutMillis);

	public void setDestinationAliases(List<String> destinationAliases);

	public boolean hasException();

}
