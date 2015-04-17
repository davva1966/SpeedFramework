// {{CopyrightNotice}}

package com.ss.framework.dialogue.api;

import java.util.List;
import java.util.Map;

import com.ss.framework.dialogue.DialogueConnectionException;
import com.ss.framework.dialogue.DialogueException;
import com.ss.framework.dialogue.DialogueExceptionSet;
import com.ss.framework.dialogue.DialogueUnsupportedOptionException;


/**
 * A <code>Dialogue</code> represents a conversation between a client and one or more servers. A Dialogue act as a client only or as server only. It can also act as both a server and a client:
 * <p>
 * To create a Dialogue use one of the <code>create</code> methods in the <code>DialogueFactory</code> class.<br>
 * Once a dialogue has been created you might want to add one or more <code>Destinations</code> to it to communicate with remote dialogue servers. Destinations are added to a dialogue via the
 * <code>addDestination</code> method.
 * <p>
 * In order for a Dialogue that accepts incoming messages (acts as a server) to be meaningful, you need to add a <code>DialogueListener</code> to it. The dialogue listener will receive the incoming
 * messages. A dialogue can have an unlimited number of listeners. The listeners will be notified of the incoming messages in the order they are added to the Dialogue. <code>DialogueListener</code>
 * are added to a dialogue using it's <code>addListener</code> method.
 * <p>
 * Before a dialogue can be used, it must be started using it's <code>start()</code> method.
 * <p>
 * When the application is done using the dialogue, it should be stopped using the <code>stop()</code> method. A stopped dialogue can not be used until it is started again.
 * <p>
 * Below is an example of how to create a dialogue that acts as a server only.<br>
 * <code>
 * Dialogue dialogue = DialogueFactory.createIPDialogue(8099);<br>
 * DialogueListener listener = new DialogueListener(){<br>
 * 			public void messageRecieved(DialogueServerMessage dialogMessage){<br>
 * 				System.out.println(dialogMessage.getString());<br>
 * 			}<br>
 * 		};<br>
 * dialogue.addListener(listener);<br>
 * dialogue.start();<br>
 * </code>
 * <p>
 * Below is an example of how to create a dialogue that acts as a client only. We are sending a response message through the dialogue and are waiting for 2 seconds to receive a reply.<br>
 * <code>
 * 	Dialogue dialogue = DialogueFactory.createIPDialogue();<br>
 * 	Destination destination = DialogueFactory.createIPDestination("MyDestination", 8099);<br>
 * 	dialogue.addDestination(destination);<br>
 * 	DialogueResponse response = dialogue.sendResponseMessage("MyDestination", "MyMessage", 2000);<br>
 * 	System.out.println(response.getString());<br>
 * 	dialogue.start();<br>
 * </code>
 * 
 * @see net.ibs.dialogue.api.DialogueFactory
 * @see net.ibs.dialogue.api.Destination
 * @see net.ibs.dialogue.api.DialogueListener
 */

public interface Dialogue {

	/**
	 * Add a <code>DialogueListener</code> to this dialogue. Dialogue listeners are notified of incoming dialog messages. A listener is only meaningful if the dialogue is accepting incoming messages
	 * (is acting as a server).
	 * <p>
	 * Any number of listeners can be added to a dialogue. They will be notified of incoming messages in the order they are added.
	 * <p>
	 * 
	 * @param listener
	 *            the <code>DialogueListener</code> to add to this dialoge
	 * @see net.ibs.dialogue.api.DialogueListener
	 */
	public void addListener(DialogueListener listener);

	/**
	 * Remove the specified dialogue listener from this dialogue. The removed dialogue listener will no longer be notifed of incoming dialog messages.
	 * 
	 * @param listener
	 *            the <code>DialogueListener</code> to remove from this dialoge
	 */
	public void removeListener(DialogueListener listener);

	/**
	 * Get the list of <code>DialogueListener</code> that are currently being notified of incoming messages in this dialogue. If no listeners are added to this dilogue, an empty list is returned.
	 * 
	 * @return the current list of <code>DialogueListener</code> on this dialogue
	 */
	public List<DialogueListener> getListeners();

	/**
	 * Add a <code>Destination</code> to this dialogue. Before you can communicate with a remote dialogue you need to add a destination that defines te properties of the remote location. Destinations
	 * are created by the <code>DialogueFactory</code> and their properties are implementation specific.
	 * <p>
	 * Destinations can be added to or removed from a dialogue at any point in time without restarting the dialogue. An unlimited number of destinations can be added to a dialogue.
	 * 
	 * @param destination
	 *            the <code>Destination</code> to add to this dialoge
	 * @see net.ibs.dialogue.api.Destination
	 * @exception DialogueException
	 *                if a destination with the same alias as the specified one is already active in this dialogue.
	 */
	public void addDestination(Destination destination) throws DialogueException;

	/**
	 * Remove the specified destination with the specified alias from this dialogue. Messages can no longer be sent to the destination.
	 * 
	 * @param destinationAlias
	 *            the <code>Destination</code> to remove from this dialoge
	 */
	public void removeDestination(String destinationAlias);

	/**
	 * Get a list of destination aliases that are currently active in this dialogue. If no destinations are active in this dilogue, an empty list is returned.
	 * 
	 * @return the list of destination aliases currently active in this dialogue.
	 */
	public List<String> getDestinationAliases();

	/**
	 * Get a list of <code>Destination</code> that are currently active in this dialogue. If no destinations are active in this dilogue, an empty list is returned.
	 * 
	 * @return the list of <code>Destination</code> currently active in this dialogue.
	 */
	public List<Destination> getDestinations();

	/**
	 * Send a <code>String</code> message to the destination with the specified alias. Before a message can be sent to a destination, the destination must have been added to this dialog.
	 * 
	 * @param destinationAlias
	 *            the alias of the destination to which the message is sent
	 * @param message
	 *            the message to send
	 * @see #addDestination(Destination);
	 * @exception DialogueConnectionException
	 *                if the connection to the destination could not be established
	 * @exception DialogueException
	 *                if an error occured during the send operation
	 */
	public void sendMessage(String destinationAlias, String message) throws DialogueConnectionException, DialogueException;

	/**
	 * Send a <code>String</code> message to the destinations with the specified aliases. Before a message can be sent to a destination, the destination must have been added to this dialog. The
	 * message might be succesfully sent to some destinations and not to others. If this occures a <code>DialogueExceptionSet</code> exception is thrown.
	 * 
	 * @param destinationAliases
	 *            the list of destination aliases to which the message is sent
	 * @param message
	 *            the message to send
	 * @see #addDestination(Destination);
	 * @exception DialogueExceptionSet
	 *                if the message could not be delivered to one or more of the specified destinations.
	 */
	public void sendMessage(List<String> destinationAliases, String message) throws DialogueExceptionSet;

	/**
	 * Send a <code>byte</code> message to the destination with the specified alias. Before a message can be sent to a destination, the destination must have been added to this dialog.
	 * 
	 * @param destinationAlias
	 *            the alias of the destination to which the message is sent
	 * @param message
	 *            the message to send
	 * @see #addDestination(Destination);
	 * @exception DialogueConnectionException
	 *                if the connection to the destination could not be established
	 * @exception DialogueException
	 *                if an error occured during the send operation
	 */
	public void sendMessage(String destinationAlias, byte[] message) throws DialogueConnectionException, DialogueException;

	/**
	 * Send a <code>byte</code> message to the destinations with the specified aliases. Before a message can be sent to a destination, the destination must have been added to this dialog. The message
	 * might be succesfully sent to some destinations and not to others. If this occures a <code>DialogueExceptionSet</code> exception is thrown.
	 * 
	 * @param destinationAliases
	 *            the list of destination aliases to which the message is sent
	 * @param message
	 *            the message to send
	 * @see #addDestination(Destination);
	 * @exception DialogueExceptionSet
	 *                if the message could not be delivered to one or more of the specified destinations.
	 */
	public void sendMessage(List<String> destinationAliases, byte[] message) throws DialogueExceptionSet;

	/**
	 * Send a <code>String</code> message to all active destinations. The message might be succesfully sent to some destinations and not to others. If this occures a <code>DialogueExceptionSet</code>
	 * exception is thrown.
	 * 
	 * @param message
	 *            the message to send
	 * @exception DialogueExceptionSet
	 *                if the message could not be delivered to one or more of the active destinations.
	 */
	public void sendMessageToAll(String message) throws DialogueExceptionSet;

	/**
	 * Send a <code>byte</code> message to all active destinations. The message might be succesfully sent to some destinations and not to others. If this occures a <code>DialogueExceptionSet</code>
	 * exception is thrown.
	 * 
	 * @param message
	 *            the message to send
	 * @exception DialogueExceptionSet
	 *                if the message could not be delivered to one or more of the active destinations.
	 */
	public void sendMessageToAll(byte[] message) throws DialogueExceptionSet;

	/**
	 * Send a <code>String</code> message to the destination with the specified alias and wait for the specified time for a response to be returned. Before a message can be sent to a destination, the
	 * destination must have been added to this dialog.
	 * <p>
	 * This method will block until one of two things happen:<br>
	 * &nbsp;&nbsp; - The specified timeout has elapsed.<br>
	 * &nbsp;&nbsp; - A response or an exception has been returned from the specified detination.<br>
	 * <p>
	 * This method will not throw an exception if a connection with the destination could not be established, instead exceptions will be thrown when an attempt is made to access the
	 * <code>DialogueResponse</code> returned from this method.
	 * <p>
	 * Unless an exception is thrown, a <code>DialogueResponse</code> is always returned. If no response was received within the specified timeout however, the <code>DialogueResponse</code> will
	 * contain no data.
	 * 
	 * @return a <code>DialogueResponse</code> holding the response.
	 * @param destinationAlias
	 *            the alias of the destination to which the message is sent
	 * @param message
	 *            the message to send
	 * @param timeoutMillis
	 *            the number of milliseconds to wait for a response before giving up.
	 * @see #addDestination(Destination);
	 * @see DialogueResponse;
	 * @exception DialogueException
	 *                if an error occured during the operation
	 */
	public DialogueResponse sendResponseMessage(String destinationAlias, String message, int timeoutMillis) throws DialogueException;

	/**
	 * Send a <code>byte</code> message to the destination with the specified alias and wait for the specified time for a response to be returned. Before a message can be sent to a destination, the
	 * destination must have been added to this dialog.
	 * <p>
	 * This method will block until one of two things happen:<br>
	 * &nbsp;&nbsp; - The specified timeout has elapsed.<br>
	 * &nbsp;&nbsp; - A response or an exception has been returned from the specified detination.<br>
	 * <p>
	 * This method will not throw an exception if a connection with the destination could not be established, instead exceptions will be thrown when an attempt is made to access the
	 * <code>DialogueResponse</code> returned from this method.
	 * <p>
	 * Unless an exception is thrown, a <code>DialogueResponse</code> is always returned. If no response was received within the specified timeout however, the <code>DialogueResponse</code> will
	 * contain no data.
	 * 
	 * @return a <code>DialogueResponse</code> holding the response.
	 * @param destinationAlias
	 *            the alias of the destination to which the message is sent
	 * @param message
	 *            the message to send
	 * @param timeoutMillis
	 *            the number of milliseconds to wait for a response before giving up.
	 * @see #addDestination(Destination);
	 * @see DialogueResponse;
	 * @exception DialogueException
	 *                if an error occured during the operation
	 */
	public DialogueResponse sendResponseMessage(String destinationAlias, byte[] message, int timeoutMillis) throws DialogueException;

	/**
	 * Send a <code>String</code> message to all the destination with the specified aliases and wait for the specified time for the responses to be returned. Before a message can be sent to a
	 * destination, the destination must have been added to this dialog.
	 * <p>
	 * This method will block until one of two things happen:<br>
	 * &nbsp;&nbsp; - The specified timeout has elapsed.<br>
	 * &nbsp;&nbsp; - A response or an exception has been returned from all specified detinations.<br>
	 * <p>
	 * This method will not throw an exception if a connection to one or more of the specfied destinations could not be established, instead exceptions will be thrown when an attempt is made to access
	 * the <code>DialogueResponseSet</code> returned from this method.
	 * <p>
	 * Unless an exception is thrown, a <code>DialogueResponseSet</code> is always returned.<br>
	 * <p>
	 * It is possible for this operation to receive a response from some but not all of the specified detinations. The returned <code>DialogueResponseSet</code> will then hold the response from some
	 * of the destinations while attempt to access other responses might result in an exception being thrown or a value of <code>null</code> being returned. For all destinations to which the message
	 * was succesfully sent but no response was received within the specified timeout, the <code>DialogueResponseSet</code> will return a <code>null</code> value when an attempt is made to access the
	 * response.
	 * 
	 * @return a <code>DialogueResponseSet</code> holding the responses or the exceptions from the specified destinations.
	 * @param destinationAliases
	 *            the list of destination aliases to which the message is sent
	 * @param message
	 *            the message to send
	 * @param timeoutMillis
	 *            the number of milliseconds to wait for a the responses to arrive before giving up.
	 * @see #addDestination(Destination);
	 * @see DialogueResponseSet;
	 * @exception DialogueException
	 *                if an error occured during the operation
	 */
	public DialogueResponseSet sendResponseMessage(List<String> destinationAliases, String message, int timeoutMillis) throws DialogueException;

	/**
	 * Send a <code>byte</code> message to all the destination with the specified aliases and wait for the specified time for the responses to be returned. Before a message can be sent to a
	 * destination, the destination must have been added to this dialog.
	 * <p>
	 * This method will block until one of two things happen:<br>
	 * &nbsp;&nbsp; - The specified timeout has elapsed.<br>
	 * &nbsp;&nbsp; - A response or an exception has been returned from all specified detinations.<br>
	 * <p>
	 * This method will not throw an exception if a connection to one or more of the specfied destinations could not be established, instead exceptions will be thrown when an attempt is made to access
	 * the <code>DialogueResponseSet</code> returned from this method.
	 * <p>
	 * Unless an exception is thrown, a <code>DialogueResponseSet</code> is always returned.<br>
	 * <p>
	 * It is possible for this operation to receive a response from some but not all of the specified detinations. The returned <code>DialogueResponseSet</code> will then hold the response from some
	 * of the destinations while attempt to access other responses might result in an exception being thrown or a value of <code>null</code> being returned. For all destinations to which the message
	 * was succesfully sent but no response was received within the specified timeout, the <code>DialogueResponseSet</code> will return a <code>null</code> value when an attempt is made to access the
	 * response.
	 * 
	 * @return a <code>DialogueResponseSet</code> holding the responses or the exceptions from the specified destinations.
	 * @param destinationAliases
	 *            the list of destination aliases to which the message is sent
	 * @param message
	 *            the message to send
	 * @param timeoutMillis
	 *            the number of milliseconds to wait for a the responses to arrive before giving up.
	 * @see #addDestination(Destination);
	 * @see DialogueResponseSet;
	 * @exception DialogueException
	 *                if an error occured during the operation
	 */
	public DialogueResponseSet sendResponseMessage(List<String> destinationAliases, byte[] message, int timeoutMillis) throws DialogueException;

	/**
	 * Send a <code>String</code> message to all active destinations and wait for the specified time for the responses to be returned.
	 * <p>
	 * This method will block until one of two things happen:<br>
	 * &nbsp;&nbsp; - The specified timeout has elapsed.<br>
	 * &nbsp;&nbsp; - A response or an exception has been returned from all active detinations.<br>
	 * <p>
	 * This method will not throw an exception if a connection to one or more of the destinations could not be established, instead exceptions will be thrown when an attempt is made to access the
	 * <code>DialogueResponseSet</code> returned from this method.
	 * <p>
	 * Unless an exception is thrown, a <code>DialogueResponseSet</code> is always returned.<br>
	 * <p>
	 * It is possible for this operation to receive a response from some but not all of the active detinations. The returned <code>DialogueResponseSet</code> will then hold the response from some of
	 * the destinations while attempt to access other responses might result in an exception being thrown or a value of <code>null</code> being returned. For all destinations to which the message was
	 * succesfully sent but no response was received within the specified timeout, the <code>DialogueResponseSet</code> will return a <code>null</code> value when an attempt is made to access the
	 * response.
	 * 
	 * @return a <code>DialogueResponseSet</code> holding the responses or the exceptions from the active destinations.
	 * @param message
	 *            the message to send
	 * @param timeoutMillis
	 *            the number of milliseconds to wait for a the responses to arrive before giving up.
	 * @see DialogueResponseSet;
	 * @exception DialogueException
	 *                if an error occured during the operation
	 */
	public DialogueResponseSet sendResponseMessageToAll(String message, int timeoutMillis) throws DialogueException;

	/**
	 * Send a <code>byte</code> message to all active destinations and wait for the specified time for the responses to be returned.
	 * <p>
	 * This method will block until one of two things happen:<br>
	 * &nbsp;&nbsp; - The specified timeout has elapsed.<br>
	 * &nbsp;&nbsp; - A response or an exception has been returned from all active detinations.<br>
	 * <p>
	 * This method will not throw an exception if a connection to one or more of the destinations could not be established, instead exceptions will be thrown when an attempt is made to access the
	 * <code>DialogueResponseSet</code> returned from this method.
	 * <p>
	 * Unless an exception is thrown, a <code>DialogueResponseSet</code> is always returned.<br>
	 * <p>
	 * It is possible for this operation to receive a response from some but not all of the active detinations. The returned <code>DialogueResponseSet</code> will then hold the response from some of
	 * the destinations while attempt to access other responses might result in an exception being thrown or a value of <code>null</code> being returned. For all destinations to which the message was
	 * succesfully sent but no response was received within the specified timeout, the <code>DialogueResponseSet</code> will return a <code>null</code> value when an attempt is made to access the
	 * response.
	 * 
	 * @return a <code>DialogueResponseSet</code> holding the responses or the exceptions from the active destinations.
	 * @param message
	 *            the message to send
	 * @param timeoutMillis
	 *            the number of milliseconds to wait for a the responses to arrive before giving up.
	 * @see DialogueResponseSet;
	 * @exception DialogueException
	 *                if an error occured during the operation
	 */
	public DialogueResponseSet sendResponseMessageToAll(byte[] message, int timeoutMillis) throws DialogueException;

	/**
	 * Set the configuration options to use in this dialogue. For options to become effective, they must be set prior to starting the dialogue. If options are set after the dialogue is started they
	 * will not be applied until the dialogue is restarted.
	 * <p>
	 * The options must be passed to this method in the form of a Map. The of the map must be the name of a supported option and the value must be of the correct type for the option.
	 * 
	 * @param options
	 *            the new option map to set.
	 * @see #getSupportedOptions();
	 * @exception DialogueUnsupportedOptionException
	 *                if an attempt is made to set an unsupported option or if the option value is of the wrong type.
	 */
	public void setOptions(Map<String, Object> options) throws DialogueUnsupportedOptionException;

	/**
	 * Return a map with the current option names and their values. The returned Map may include options and values that have been set, but have not yet applied to this dialogue.
	 * 
	 * @return a <code>Map</code> holding the current option names and their values.
	 * @param options
	 *            the new option map to set.
	 * @see #setOptions(Map);
	 */
	public Map<String, Object> getOptions();

	/**
	 * Return a map with all currently supported options and their data types.
	 * 
	 * @return a <code>Map</code> holding the supported option names and their data type.
	 */
	public Map<String, Class> getSupportedOptions();

	/**
	 * Start this dialogue. A dialogue can not be used to send or receive messages until it is started. A dialogue can be started or stopped at any time.
	 * <p>
	 * When the dialogue is no longer used, it should be stopped using the <code>stop()</code> method to free it's resources.
	 * 
	 * @exception DialogueException
	 *                if the dialogue could not be started.
	 * @see #stop();
	 */
	public void start() throws DialogueException;

	/**
	 * Stop this dialogue and free it's resources. A stopped dialogue can not be used to send or receive messages until it is started again. A dialogue can be started or stopped at any time.
	 * <p>
	 * When the dialogue is no longer used, it should be stopped to free it's resources.
	 * 
	 * @see #start();
	 */
	public void stop();

}
