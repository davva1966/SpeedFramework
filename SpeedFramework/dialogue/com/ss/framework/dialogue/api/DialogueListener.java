// {{CopyrightNotice}}

package com.ss.framework.dialogue.api;

/**
 * The interface <code>DialogueListener</code> defines the interface for a Dialogue listener. Dialogue listeners are used when the <code>Dialogue</code> receives incoming messages.
 */

public interface DialogueListener {

	/**
	 * Handle the received message.
	 * 
	 * @param dialogMessage
	 *            the received <code>DialogueServerMessage</code> that was received.
	 * @see Dialogue.#addListener(DialogueListener)
	 */
	public void messageRecieved(DialogueServerMessage dialogMessage);

}
