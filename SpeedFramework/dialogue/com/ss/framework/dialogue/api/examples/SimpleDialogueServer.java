// {{CopyrightNotice}}

package com.ss.framework.dialogue.api.examples;

import com.ss.framework.dialogue.DialogueException;
import com.ss.framework.dialogue.api.Dialogue;
import com.ss.framework.dialogue.api.DialogueFactory;
import com.ss.framework.dialogue.api.DialogueListener;
import com.ss.framework.dialogue.api.DialogueServerMessage;

public class SimpleDialogueServer {

	public static void main(String[] args) {
		Dialogue dialogue = null;

		try {
			// Create the dialogue. When a local port is specified the Dialogue will act as a server and will
			// accept incoming dialogue messages. We will accept incoming dialogue messages on port 8098
			dialogue = DialogueFactory.createIPDialogue(8098);

			// Create the dialogue listener. This is only needed when we service incoming messages. The
			// dialogue listener handles those messages. Here we will only print the incoming message and echo
			// it back to the client.
			DialogueListener listener = new DialogueListener() {

				public void messageRecieved(DialogueServerMessage dialogMessage) {

					// Get the message string
					String message = dialogMessage.getString();

					// Print the recieved message
					System.out.println(message);

					try {
						// If the message requested a response, respond to the message. An attempt to respond
						// to a message that did not request a response will result in an exception being
						// thrown.
						if (dialogMessage.responseRequested())
							dialogMessage.respond("Response to: " + message);
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
			// Stop the dialogue. The dialogue cannot receive or send messages until it is restarted.
			if (dialogue != null)
				dialogue.stop();
		}

	}

}
