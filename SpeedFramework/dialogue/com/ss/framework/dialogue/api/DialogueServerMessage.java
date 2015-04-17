// {{CopyrightNotice}}

package com.ss.framework.dialogue.api;

import java.io.UnsupportedEncodingException;

import com.ss.framework.dialogue.DialogueException;


public interface DialogueServerMessage {

	/**
	 * Get the message data as a <code>String</code>. The <tt>String</tt> is constructed by decoding the message bytes using the platform's default charset. The length of the <tt>String</tt> is a
	 * function of the charset, and hence may not be equal to the number of bytes received.
	 * <p>
	 * The behavior of this method when the received bytes are not valid in the default charset is unspecified. The {@link java.nio.charset.CharsetDecoder} class should be used when more control over
	 * the decoding process is required.
	 * 
	 * @return the message data as a <code>String</code>
	 * @see #getString(String)
	 */
	public String getString();

	/**
	 * Get the message data as a <code>String</code>. The <tt>String</tt> is constructed by decoding the message bytes using the specified charset. The length of the <tt>String</tt> is a function of
	 * the charset, and hence may not be equal to the number of bytes received.
	 * <p>
	 * The behavior of this method when the received bytes are not valid in the given charset is unspecified. The {@link java.nio.charset.CharsetDecoder} class should be used when more control over
	 * the decoding process is required.
	 * 
	 * @return the message data as a <code>String</code>
	 * @param charsetName
	 *            the name of a supported {@link java.nio.charset.Charset </code>charset<code>}
	 * @exception UnsupportedEncodingException
	 *                If the named charset is not supported
	 */
	public String getString(String charSetName) throws UnsupportedEncodingException;

	/**
	 * Get the message data as an array of bytes.
	 * 
	 * @return the message data an array of bytes
	 */
	public byte[] getBytes();

	/**
	 * Get the response requested flag. This method returns <code>true</code> if the client requested a response to the message, otherwise <code>false</code>.
	 * 
	 * @return <code>true</code> if a response twas requested, <code>false</code> otherwiswe
	 */
	public boolean responseRequested();

	/**
	 * Send a <code>String</code> response to this message. A response can only be sent if the message requested a response. An attempt to send a response to a message where no response was requested
	 * will result in an exception being thrown.
	 * <p>
	 * If a response is sent to a remote dialogue that has stopped waiting for the resoponse, the response is discarded.
	 * 
	 * @param response
	 *            the response string
	 * @exception DialogueException
	 *                If no response to this message was requested
	 */
	public void respond(String response) throws DialogueException;

	/**
	 * Send a response to this message as an array of bytes. A response can only be sent if the message requested a response. An attempt to send a response to a message where no response was requested
	 * will result in an exception being thrown.
	 * <p>
	 * If a response is sent to a remote dialogue that has stopped waiting for the resoponse, the response is discarded.
	 * 
	 * @param response
	 *            the array of bytes that is the response
	 * @exception DialogueException
	 *                If no response to this message was requested
	 */
	public void respond(byte[] response) throws DialogueException;

}
