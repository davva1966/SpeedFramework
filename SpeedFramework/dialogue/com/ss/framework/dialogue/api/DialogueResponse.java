// {{CopyrightNotice}}

package com.ss.framework.dialogue.api;

/**
 * A <code>DialogueResponse</code> is returned from a dialogue when a response message requested is issued. Dialogue
 * responses hold the data that represents the received response. If an exception occured during the
 * send/receive phase the <code>DialogueResponse</code> will hold the exception. If an exception did occur
 * any attempt of accessing the received data will result in that exception being thrown.
 * <p>
 * If communication was successfull but no response was recieved within the specified timeout, the
 * <code>DialogueResponse</code> will return <code>null</code> wehn an attempt is made to access the
 * response data.
 */

import java.io.UnsupportedEncodingException;

import com.ss.framework.dialogue.DialogueConnectionException;
import com.ss.framework.dialogue.DialogueException;


public interface DialogueResponse {

	/**
	 * Get the response data as a <code>String</code>. The <tt>String</tt> is constructed by decoding the received array of bytes using the platform's default charset. The length of the
	 * <tt>String</tt> is a function of the charset, and hence may not be equal to the number of bytes received.
	 * <p>
	 * The behavior of this method when the received bytes are not valid in the default charset is unspecified. The {@link java.nio.charset.CharsetDecoder} class should be used when more control over
	 * the decoding process is required.
	 * 
	 * @return the received response as a <code>String</code> or <code>null</code> if no response was received
	 * @see #getString(String)
	 * @exception DialogueConnectionException
	 *                if an the connection with the destination dialogue could not be established
	 * @exception DialogueException
	 *                if an error occurred during the communication.
	 */
	public String getString() throws DialogueConnectionException, DialogueException;

	/**
	 * Get the response data as a <code>String</code>. The <tt>String</tt> is constructed by decoding the received array of bytes using the specified charset. The length of the <tt>String</tt> is a
	 * function of the charset, and hence may not be equal to the number of bytes received.
	 * <p>
	 * The behavior of this method when the received bytes are not valid in the given charset is unspecified. The {@link java.nio.charset.CharsetDecoder} class should be used when more control over
	 * the decoding process is required.
	 * 
	 * @return the received response as a <code>String</code> or <code>null</code> if no response was received
	 * @param charsetName
	 *            the name of a supported {@link java.nio.charset.Charset </code>charset<code>}
	 * @exception DialogueConnectionException
	 *                if an the connection with the destination dialogue could not be established
	 * @exception DialogueException
	 *                if an error occurred during the communication.
	 * @exception UnsupportedEncodingException
	 *                If the named charset is not supported
	 */
	public String getString(String charsetName) throws DialogueConnectionException, DialogueException, UnsupportedEncodingException;

	/**
	 * Get the response data as an array of bytes.
	 * 
	 * @return the received response as an array of bytes or <code>null</code> if no response was received
	 * @exception DialogueConnectionException
	 *                if an the connection with the destination dialogue could not be established
	 * @exception DialogueException
	 *                if an error occurred during the communication.
	 */
	public byte[] getBytes() throws DialogueConnectionException, DialogueException;

	/**
	 * Get the exception that occured while trying to communicate with the remote dialogue. If the communication was normal, this method will return <code>null</code>.
	 * 
	 * @return the encountered exception or <code>null</code> if no exception was thrown during the communication with the remote dialoge.
	 */
	public DialogueException getException();

}
