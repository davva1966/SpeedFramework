// {{CopyrightNotice}}

package com.ss.framework.dialogue;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;

import com.ss.framework.dialogue.api.DialogueResponse;


public class DialogueResponseImpl implements DialogueResponse {

	protected byte[] data = null;

	protected DialogueException exception = null;

	public DialogueResponseImpl(byte[] data) {
		this.data = data;
	}

	public DialogueResponseImpl(Exception exception) {
		if (exception != null) {
			if (exception instanceof ConnectException)
				this.exception = new DialogueConnectionException(exception);
			else
				this.exception = new DialogueException(exception);
		}
	}

	public String getString() throws DialogueConnectionException, DialogueException {
		if (getBytes() == null)
			return null;
		return new String(getBytes());
	}

	public String getString(String charsetName) throws DialogueConnectionException, DialogueException, UnsupportedEncodingException {
		if (getBytes() == null)
			return null;
		return new String(getBytes(), charsetName);
	}

	public byte[] getBytes() throws DialogueConnectionException, DialogueException {
		if (exception != null)
			throw exception;

		return data;
	}

	public DialogueException getException() {
		return exception;
	}
}
