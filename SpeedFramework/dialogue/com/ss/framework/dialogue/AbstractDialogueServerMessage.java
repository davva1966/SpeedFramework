// {{CopyrightNotice}}

package com.ss.framework.dialogue;

import java.io.UnsupportedEncodingException;

import com.ss.framework.dialogue.api.DialogueServerMessage;


public abstract class AbstractDialogueServerMessage implements DialogueServerMessage {

	protected byte[] data = null;

	public AbstractDialogueServerMessage(byte[] data) {
		this.data = data;
	}

	public String getString() {
		if (getBytes() == null)
			return null;
		return new String(getBytes());
	}

	public String getString(String charSetName) throws UnsupportedEncodingException {
		if (getBytes() == null)
			return null;
		return new String(getBytes(), charSetName);
	}

	public byte[] getBytes() {
		return data;
	}
}
