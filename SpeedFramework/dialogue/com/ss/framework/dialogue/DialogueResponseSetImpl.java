// {{CopyrightNotice}}

package com.ss.framework.dialogue;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ss.framework.dialogue.api.DialogueResponse;
import com.ss.framework.dialogue.api.DialogueResponseSet;


public class DialogueResponseSetImpl implements DialogueResponseSet {

	protected Map<String, DialogueResponse> responseMap = new HashMap<String, DialogueResponse>();

	public DialogueResponseSetImpl() {

	}

	public Set<String> getDestinationAliases() {
		return responseMap.keySet();
	}

	public DialogueResponse getResponse(String destinationAlias) throws DialogueException {
		if (responseMap.containsKey(destinationAlias) == false)
			throw new DialogueException("Response for destination alias: " + destinationAlias + " not found in response set");

		return responseMap.get(destinationAlias);
	}

	public String getString(String destinationAlias) throws DialogueConnectionException, DialogueException {
		if (getBytes(destinationAlias) == null)
			return null;
		return new String(getBytes(destinationAlias));
	}

	public String getString(String destinationAlias, String charSetName) throws DialogueConnectionException, DialogueException, UnsupportedEncodingException {
		if (getBytes(destinationAlias) == null)
			return null;
		return new String(getBytes(destinationAlias), charSetName);
	}

	public byte[] getBytes(String destinationAlias) throws DialogueConnectionException, DialogueException {
		return getResponse(destinationAlias).getBytes();

	}

	public void addResponse(String destinationAlias, DialogueResponse response) throws DialogueException {
		if (responseMap.containsKey(destinationAlias))
			throw new DialogueException("Response for destination alias: " + destinationAlias + " already added to response set");

		responseMap.put(destinationAlias, response);
	}

	public void removeResponse(String destinationAlias) {
		responseMap.remove(destinationAlias);
	}
}
