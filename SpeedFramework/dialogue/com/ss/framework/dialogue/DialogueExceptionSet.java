// {{CopyrightNotice}}

package com.ss.framework.dialogue;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DialogueExceptionSet extends DialogueException {

	static final long serialVersionUID = -3387516993124229948L;

	protected Map<String, DialogueException> exceptionMap = new HashMap<String, DialogueException>();

	public DialogueExceptionSet(String message) {
		super(message);
	}

	public void addException(String detinationAlias, Throwable exception) {
		if (exception instanceof DialogueException == false)
			exception = new DialogueException(exception);

		if (exception.getCause() instanceof ConnectException)
			exception = new DialogueConnectionException(exception.getCause());

		exceptionMap.put(detinationAlias, (DialogueException) exception);
	}

	public DialogueException getException(String detinationAlias) {
		return exceptionMap.get(detinationAlias);
	}

	public Set<String> getDetinationAliases() {
		return exceptionMap.keySet();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		for (String destinationAlias : exceptionMap.keySet()) {
			DialogueException e = exceptionMap.get(destinationAlias);
			sb.append(" ; ");
			sb.append("Destination alias: " + destinationAlias);
			sb.append(" Exception: " + e.toString());
		}
		return sb.toString();
	}

}
