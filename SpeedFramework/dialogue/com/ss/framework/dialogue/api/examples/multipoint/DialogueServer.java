// {{CopyrightNotice}}

package com.ss.framework.dialogue.api.examples.multipoint;

import com.ss.framework.dialogue.DialogueException;
import com.ss.framework.dialogue.api.Dialogue;
import com.ss.framework.dialogue.api.DialogueFactory;
import com.ss.framework.dialogue.api.DialogueListener;
import com.ss.framework.dialogue.api.DialogueServerMessage;

public class DialogueServer implements Runnable {

	protected String address;

	protected int port;

	public DialogueServer(String address, int port) {
		this.address = address;
		this.port = port;
	}

	public void run() {

		Dialogue dialogue = null;

		try {
			// Create the dialogue. When only local port is specified the Dialogue will
			// act as a server only and cannot be used to transmit outgoing dialogue messages.
			dialogue = DialogueFactory.createIPDialogue(address, port);

			// Create the dialogue listener. This is only needed when we service incoming messages.
			// The dialogue listener handles thoose messages. Here we will only echo the incoming
			// message back to the client.
			DialogueListener listener = new DialogueListener() {

				public void messageRecieved(DialogueServerMessage dialogMessage) {
					// Get the message string
					String message = dialogMessage.getString();
					// If the message requested a response, respond to the message
					try {
						if (dialogMessage.responseRequested())
							dialogMessage.respond("Response from server (" + address + ":" + port + ") to: " + message);
					} catch (DialogueException e) {
						System.out.println(e);
					}
				}
			};

			// Add the listener to our dialogue
			dialogue.addListener(listener);

			// Start the dialogue. Once created, dialogues can be started and stopped at any time
			dialogue.start();

			// Wait for 1 minute before ending the dialogue
			Thread.sleep(60000);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			// Stop the dialogue. The dialogue cannot receive messages until it is restarted.
			dialogue.stop();
		}

	}

}
