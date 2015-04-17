// {{CopyrightNotice}}

package com.ss.framework.dialogue.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.ss.framework.dialogue.DialogueEngine;
import com.ss.framework.dialogue.DialogueException;
import com.ss.framework.dialogue.ip.SocketDestination;
import com.ss.framework.dialogue.ip.SocketDialogueEngine;


public class DialogueFactory {

	/**
	 * Create an IP based <code>Dialogue</code> that will act as a client only.<br>
	 * Before any messages can be sent using this dialogue, one or more destinations must be added to it. Destinations for this dialogue are created using the <code>createIPDestination</code> method.
	 * <p>
	 * To create a dialogue that can also receive incoming messages use the <code>createIPDialogue(port)</code> method instead.
	 * 
	 * @return a new (client only) <code>Dialogue</code>
	 * @exception DialogueException
	 *                if the dialogue could not be created.
	 * @see #createIPDialogue(int);
	 * @see #createIPDestination(String, int);
	 */
	public static Dialogue createIPDialogue() throws DialogueException {
		return createIPDialogue(0);
	}

	/**
	 * Create an IP based <code>Dialogue</code> capable of receiving incoming messages. The created <code>Dialogue</code> will listen for incoming messages on the specified port. Before any messages
	 * can be sent using this dialogue, one or more destinations must be added to it. Destinations for this dialogue are created using the <code>createIPDestination</code> method.
	 * <p>
	 * To create a dialogue that will act as a client only, specify port=0 or use the <code>createIPDialogue()</code> method instead.
	 * 
	 * @return a new <code>Dialogue</code>
	 * @param localPort
	 *            the port to listen to or <code>0</code> if client only mode.
	 * @see #createIPDialogue();
	 * @see #createIPDestination(String, int);
	 * @exception DialogueException
	 *                if the dialogue could not be created.
	 */
	public static Dialogue createIPDialogue(int localPort) throws DialogueException {
		try {
			return createIPDialogue(InetAddress.getLocalHost(), localPort);
		} catch (UnknownHostException e) {
			throw new DialogueException("Error during dialogue creation", e);
		}
	}

	/**
	 * Create an IP based <code>Dialogue</code> capable of receiving incoming messages. The created <code>Dialogue</code> will bind to the specified address and listen for incoming messages on the
	 * specified port. Before any messages can be sent using this dialogue, one or more destinations must be added to it. Destinations for this dialogue are created using the
	 * <code>createIPDestination</code> method.
	 * <p>
	 * To create a dialogue that will act as a client only use the <code>createIPDialogue()</code> method instead.
	 * 
	 * @return a new <code>Dialogue</code>
	 * @param localAddress
	 *            the IP address to bind to.
	 * @param localPort
	 *            the port to listen to.
	 * @see #createIPDialogue();
	 * @see #createIPDestination(String, int);
	 * @exception DialogueException
	 *                if the dialogue could not be created.
	 */
	public static Dialogue createIPDialogue(String localAddress, int localPort) throws DialogueException {
		try {
			return createIPDialogue(InetAddress.getByName(localAddress), localPort);
		} catch (UnknownHostException e) {
			throw new DialogueException("Error during dialogue creation", e);
		}
	}

	/**
	 * Create an IP based <code>Dialogue</code> capable of receiving incoming messages. The created <code>Dialogue</code> will bind to the specified address and listen for incoming messages on the
	 * specified port. Before any messages can be sent using this dialogue, one or more destinations must be added to it. Destinations for this dialogue are created using the
	 * <code>createIPDestination</code> method.
	 * <p>
	 * To create a dialogue that will act as a client only use the <code>createIPDialogue()</code> method instead.
	 * 
	 * @return a new <code>Dialogue</code>
	 * @param localAddress
	 *            the IP address to bind to.
	 * @param localPort
	 *            the port to listen to.
	 * @see #createIPDialogue();
	 * @see #createIPDestination(String, int);
	 * @exception DialogueException
	 *                if the dialogue could not be created.
	 */
	public static Dialogue createIPDialogue(InetAddress localAdress, int localPort) throws DialogueException {
		try {
			DialogueEngine engine = new SocketDialogueEngine(localAdress, localPort);
			return engine.getDialogue();
		} catch (IOException e) {
			throw new DialogueException("Error during dialogue creation", e);
		}
	}

	/**
	 * Create a <code>Destination</code> for an IP based <code>Dialogue</code>. The destination address will become the local host.
	 * 
	 * @return a new <code>Destination</code> for an IP based dialogue
	 * @param destinationAlias
	 *            the alias for the destination. The alias is used when communicating with the destination through <code>Dialogue</code>.
	 * @param remotePort
	 *            the port that the destination dialogue is listening to.
	 * @exception DialogueException
	 *                if the destination could not be created.
	 */
	public static Destination createIPDestination(String destinationAlias, int remotePort) throws DialogueException {
		try {
			return createIPDestination(destinationAlias, InetAddress.getLocalHost(), remotePort);
		} catch (UnknownHostException e) {
			throw new DialogueException("Error during destination creation", e);
		}
	}

	/**
	 * Create a <code>Destination</code> for an IP based <code>Dialogue</code>.
	 * 
	 * @return a new <code>Destination</code> for an IP based dialogue
	 * @param destinationAlias
	 *            the alias for the destination. The alias is used when communicating with the destination through <code>Dialogue</code>.
	 * @param remoteAddress
	 *            the IP address of the destination dialogue.
	 * @param port
	 *            the port that the destination dialogue is listening to.
	 * @exception DialogueException
	 *                if the destination could not be created.
	 */
	public static Destination createIPDestination(String destinationAlias, String remoteAddress, int remotePort) throws DialogueException {
		try {
			return createIPDestination(destinationAlias, InetAddress.getByName(remoteAddress), remotePort);
		} catch (UnknownHostException e) {
			throw new DialogueException("Error during destination creation", e);
		}
	}

	/**
	 * Create a <code>Destination</code> for an IP based <code>Dialogue</code>.
	 * 
	 * @return a new <code>Destination</code> for an IP based dialogue
	 * @param destinationAlias
	 *            the alias for the destination. The alias is used when communicating with the destination through <code>Dialogue</code>.
	 * @param remoteAddress
	 *            the IP address of the destination dialogue.
	 * @param port
	 *            the port that the destination dialogue is listening to.
	 * @exception DialogueException
	 *                if the destination could not be created.
	 */
	public static Destination createIPDestination(String destinationAlias, InetAddress address, int port) throws DialogueException {
		return new SocketDestination(destinationAlias, address, port);

	}
}
