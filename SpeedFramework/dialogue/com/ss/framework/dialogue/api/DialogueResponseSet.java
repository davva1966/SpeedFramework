// {{CopyrightNotice}}

package com.ss.framework.dialogue.api;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import com.ss.framework.dialogue.DialogueConnectionException;
import com.ss.framework.dialogue.DialogueException;


public interface DialogueResponseSet {

	/**
	 * Get the set of destination aliases that this response set holds a response for.
	 * 
	 * @return the set of destination aliases that has a response in this response set.
	 */
	public Set<String> getDestinationAliases();

	/**
	 * Get the response from the specified destination alias.
	 * 
	 * @return the <code>DialogueResponseSet</code> received from the specified destination.
	 * @param destinationAlias
	 *            the alias name of the destination
	 * @exception DialogueException
	 *                if no response for the specified destination is contained in this response set.
	 */
	public DialogueResponse getResponse(String destinationAlias) throws DialogueException;

	/**
	 * Get the response data from the specified destination as a <code>String</code>. The <tt>String</tt> is constructed by decoding the received array of bytes using the platform's default charset.
	 * The length of the <tt>String</tt> is a function of the charset, and hence may not be equal to the number of bytes received.
	 * <p>
	 * The behavior of this method when the received bytes are not valid in the default charset is unspecified. The {@link java.nio.charset.CharsetDecoder} class should be used when more control over
	 * the decoding process is required.
	 * 
	 * @return the received response as a <code>String</code> or <code>null</code> if no response was received from the specified destination
	 * @param destinationAlias
	 *            the alias name of the destination
	 * @see #getString(String)
	 * @exception DialogueConnectionException
	 *                if an the connection with the destination dialogue could not be established
	 * @exception DialogueException
	 *                if no response for the specified destination is contained in this response set or an error occured during during the communication with the destination dialogue.
	 */
	public String getString(String destinationAlias) throws DialogueConnectionException, DialogueException;

	/**
	 * Get the response data from the specified destination as a <code>String</code>. The <tt>String</tt> is constructed by decoding the received array of bytes using the specified charset. The length
	 * of the <tt>String</tt> is a function of the charset, and hence may not be equal to the number of bytes received.
	 * <p>
	 * The behavior of this method when the received bytes are not valid in the given charset is unspecified. The {@link java.nio.charset.CharsetDecoder} class should be used when more control over
	 * the decoding process is required.
	 * 
	 * @return the received response as a <code>String</code> or <code>null</code> if no response was received from the specified destination
	 * @param destinationAlias
	 *            the alias name of the destination
	 * @param charsetName
	 *            the name of a supported {@link java.nio.charset.Charset </code>charset<code>}
	 * @exception DialogueConnectionException
	 *                if an the connection with the destination dialogue could not be established
	 * @exception DialogueException
	 *                if no response for the specified destination is contained in this response set or an error occured during during the communication with the destination dialogue.
	 * @exception UnsupportedEncodingException
	 *                If the named charset is not supported
	 */
	public String getString(String destinationAlias, String charSetName) throws DialogueConnectionException, DialogueException, UnsupportedEncodingException;

	/**
	 * Get the response data from the specified destination as an array of bytes.
	 * 
	 * @return the received response as an array of bytes or <code>null</code> if no response was received from the specified destination
	 * @param destinationAlias
	 *            the alias name of the destination
	 * @exception DialogueConnectionException
	 *                if an the connection with the destination dialogue could not be established
	 * @exception DialogueException
	 *                if no response for the specified destination is contained in this response set or an error occured during during the communication with the destination dialogue.
	 */
	public byte[] getBytes(String destinationAlias) throws DialogueConnectionException, DialogueException;

}
