// {{CopyrightNotice}}

package com.ss.framework.dialogue.api.examples.multipoint;

import java.util.ArrayList;
import java.util.List;

import com.ss.framework.dialogue.DialogueException;
import com.ss.framework.dialogue.DialogueExceptionSet;
import com.ss.framework.dialogue.api.Dialogue;
import com.ss.framework.dialogue.api.DialogueFactory;
import com.ss.framework.dialogue.api.DialogueResponse;
import com.ss.framework.dialogue.api.DialogueResponseSet;


public class DialogueMultiPointClient {

	// To run this example you also need to run DialogueServer simultaneously.

	public static void main(String[] args) {

		// Start three servers (Port 8097,8098 and 8099)
		new Thread(new DialogueServer("127.0.0.1", 8097)).start();
		new Thread(new DialogueServer("127.0.0.1", 8098)).start();
		new Thread(new DialogueServer("127.0.0.1", 8099)).start();

		Dialogue dialogue = null;

		try {
			// Create the dialogue. When no local port is specified the Dialogue will
			// act as a client only and cannot receive incoming dialogue messages.
			dialogue = DialogueFactory.createIPDialogue();

			// Add the remote destinations
			dialogue.addDestination(DialogueFactory.createIPDestination("Destination1", "127.0.0.1", 8097));
			dialogue.addDestination(DialogueFactory.createIPDestination("Destination2", "127.0.0.1", 8098));
			dialogue.addDestination(DialogueFactory.createIPDestination("Destination3", "127.0.0.1", 8099));

			// Start the dialogue. Once created, dialogues can be started and stopped at any time
			dialogue.start();

			// Send a message to destination 1 and request a response. Wait for 5 seconds for the response
			// before giving up
			DialogueResponse resp = dialogue.sendResponseMessage("Destination1", "Message1", 5000);
			// Print the response
			try {
				System.out.println(resp.getString() == null ? "No response received" : resp.getString());
			} catch (DialogueException e) {
				System.out.println("Destintation1: " + e);
			}

			// Send a message to destination 2 and request a response. Wait for 1 second for the response
			// before giving up
			resp = dialogue.sendResponseMessage("Destination2", "Message2", 5000);
			// Print the response
			try {
				System.out.println(resp.getString() == null ? "No response received" : resp.getString());
			} catch (DialogueException e) {
				System.out.println("Destintation2: " + e);
			}

			// Send a message to destination 3 and request a response. Wait for 5 seconds for the response
			// before giving up
			resp = dialogue.sendResponseMessage("Destination3", "Message3", 5000);
			// Print the response
			try {
				System.out.println(resp.getString() == null ? "No response received" : resp.getString());
			} catch (DialogueException e) {
				System.out.println("Destintation3: " + e);
			}

			// Send a message to destination 1 and 3 and request a response. Wait for 5 seconds for all
			// responses to be received before giving up
			List<String> destiniations = new ArrayList<String>();
			destiniations.add("Destination1");
			destiniations.add("Destination3");
			DialogueResponseSet respSet = dialogue.sendResponseMessage(destiniations, "Message4", 5000);
			// Print the response
			for (String dest : respSet.getDestinationAliases()) {
				try {
					System.out.println(respSet.getString(dest) == null ? "No response received" : respSet.getString(dest));
				} catch (DialogueException e) {
					System.out.println(dest + ": " + e);
				}
			}

			// Send a message to all destinations request a response. Wait for 5 seconds for all responses to
			// be received before giving up
			respSet = dialogue.sendResponseMessageToAll("Message5", 5000);
			// Print the response
			for (String dest : respSet.getDestinationAliases()) {
				try {
					System.out.println(respSet.getString(dest) == null ? "No response received" : respSet.getString(dest));
				} catch (DialogueException e) {
					System.out.println(dest + ": " + e);
				}
			}

			// Send a message to all destinations (Don't request a response)
			try {
				dialogue.sendMessageToAll("Message6");
			} catch (DialogueExceptionSet e) {
				System.out.println("Error during send:");
				for (String alias : e.getDetinationAliases())
					System.out.println(alias + ": " + e.getException(alias));
			}

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			// Stop the dialogue. The dialogue cannot send messages until it is restarted.
			dialogue.stop();
			System.exit(0);
		}

	}

}
