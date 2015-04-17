// {{CopyrightNotice}}

package com.ss.framework.dialogue;

public class DialogueUnsupportedOptionException extends DialogueException {

	static final long serialVersionUID = -3387516993124229948L;

	public DialogueUnsupportedOptionException(String message) {
		super(message);
	}

	public DialogueUnsupportedOptionException(String message, Throwable cause) {
		super(message, cause);
	}

	public DialogueUnsupportedOptionException(Throwable cause) {
		super(cause);
	}

}
