// {{CopyrightNotice}}

package com.ss.framework.dialogue.api.examples;

import com.ss.framework.dialogue.DialogueException;
import com.ss.framework.dialogue.api.Dialogue;
import com.ss.framework.dialogue.api.DialogueFactory;
import com.ss.framework.dialogue.api.DialogueListener;
import com.ss.framework.dialogue.api.DialogueResponse;
import com.ss.framework.dialogue.api.DialogueServerMessage;

public class DialogueClientAndServer2 {

	// To run this example DialogueClientAndServer1 must be running.

	public static void main(String[] args) {
		Dialogue dialogue = null;

		try {
			// Create the dialogue and start listen for messages on port 8099
			dialogue = DialogueFactory.createIPDialogue(8099);

			// Add a remote destination. Enables this dialogue to send messages to the remote destination. The
			// specified alias is used when communicating with this destination through the dialogue.
			dialogue.addDestination(DialogueFactory.createIPDestination("Dest1", 8098));

			// Create the dialogue listener. This listener will only the incoming message back to the client.
			DialogueListener listener = new DialogueListener() {

				public void messageRecieved(DialogueServerMessage dialogMessage) {
					try {
						// If the message requested a response, respond to the message
						if (dialogMessage.responseRequested())
							dialogMessage.respond("Response from destination 2: " + dialogMessage.getString());
					} catch (DialogueException e) {
						System.out.println(e);
					}

				}
			};

			// Add the listener to our dialogue
			dialogue.addListener(listener);

			// Start the dialogue. Once created, dialogues can be started and stopped at any time
			dialogue.start();

			// Allow remote service to start
			Thread.sleep(5000);

			// Send a message and wait for the response. Wait for 5 seconds for the response before giving up
			DialogueResponse resp = dialogue.sendResponseMessage("Dest1", "Message1 from destination 2", 5000);
			// Print the response
			try {
				System.out.println(resp.getString() == null ? "No response received" : resp.getString());
			} catch (Exception e) {
				System.out.println(e);
			}

			// Send a second message and wait for the response. Wait for 2 seconds for the response before
			// giving up
			resp = dialogue.sendResponseMessage("Dest1", "Message2 from destination 2", 2000);
			// Print the response
			try {
				System.out.println(resp.getString() == null ? "No response received" : resp.getString());
			} catch (Exception e) {
				System.out.println(e);
			}

			// Wait for 10 seconds before ending the dialogue
			Thread.sleep(10000);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			// Stop the dialogue. The dialogue cannot send or receive messages until it is restarted.
			if (dialogue != null)
				dialogue.stop();
		}

	}

}
