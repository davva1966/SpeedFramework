package com.ss.framework.dialogue.api.examples;

import com.ss.framework.dialogue.DialogueException;
import com.ss.framework.dialogue.api.Dialogue;
import com.ss.framework.dialogue.api.DialogueFactory;
import com.ss.framework.dialogue.api.DialogueResponse;

public class SimpleDialogueClient {

	// To run this example the SimpleDialogueServer must be running.

	public static void main(String[] args) {
		Dialogue dialogue = null;

		try {
			// Create the dialogue. When no port is specified the Dialogue will act as a client only and
			// cannot receive incoming dialogue messages.
			dialogue = DialogueFactory.createIPDialogue();

			// Add the remote destination. Enables this dialogue to send messages to remote destinations. In
			// this case we only add one remote destination. Here, the remote destination is on the local
			// host on port 8098. The specified destination alias "MyDestination1" is used in subsequent
			// dialogue communication with this destination. Destinations can be added to or removed from the
			// dialogue at any point
			dialogue.addDestination(DialogueFactory.createIPDestination("MyDestination1", 8098));

			// Start the dialogue. Once created, dialogues can be started and stopped at any time
			dialogue.start();

			// Send a message and request a response. Wait for 1 second for the response before giving up
			DialogueResponse resp = dialogue.sendResponseMessage("MyDestination1", "Message1", 1000);
			// Print the response
			try {
				System.out.println(resp.getString() == null ? "No response received" : resp.getString());
			} catch (DialogueException e) {
				System.out.println(e);
			}

			// Get some data
			String s = "Test data to send";

			// Send file data and request a response. Wait for 5 seconds for the response before giving up.
			resp = dialogue.sendResponseMessage("MyDestination1", s.getBytes(), 5000);
			// Print the response
			try {
				System.out.println(resp.getString() == null ? "No response received" : resp.getString());
			} catch (DialogueException e) {
				System.out.println(e);
			}

			// Send 100 messages and don't request a response
			for (int i = 0; i < 100; i++) {
				dialogue.sendMessage("MyDestination1", "Msg" + i);
			}

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			// Stop the dialogue. The dialogue cannot receive or send messages until it is restarted.
			if (dialogue != null)
				dialogue.stop();
		}

	}

}
