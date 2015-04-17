// {{CopyrightNotice}}

package com.ss.framework.dialogue.api.examples;

import com.ss.framework.dialogue.DialogueConnectionException;
import com.ss.framework.dialogue.api.Dialogue;
import com.ss.framework.dialogue.api.DialogueFactory;
import com.ss.framework.dialogue.api.DialogueListener;
import com.ss.framework.dialogue.api.DialogueResponse;
import com.ss.framework.dialogue.api.DialogueServerMessage;

public class ProcessListener2 {

	// This example reports on the progress of a another process by sending query messages to the process. The
	// process then responds with it's current progress. I.e the observer is the client and the observed
	// process is the server.

	public static void main(String[] args) {

		Dialogue dialogue = null;

		try {
			// Create the dialogue. When no port is specified the Dialogue will act as a client only
			// and cannot receive incoming dialogue messages.
			dialogue = DialogueFactory.createIPDialogue();

			// Add the remote destination. Enables this dialogue to send messages to the process. We will use
			// this to send query messages at regular intervals to request a progress update from the
			// process.
			dialogue.addDestination(DialogueFactory.createIPDestination("Process", 8099));

			// Start the dialogue. Once created, dialogues can be started and stopped at any time
			dialogue.start();

			// Start the observed process.
			startOtherProcess();

			// Run until observed process has completed. Wake-up every second and request a progress
			// update.
			while (true) {
				// Wait one second
				Thread.sleep(1000);
				try {
					// Send a response message and wait for 5 seconds to receive the response. If the response
					// is not received within 5 seconds we will assume the process has died and leave the
					// loop.
					DialogueResponse response = dialogue.sendResponseMessage("Process", "REPORT", 5000);
					if (response.getString() == null)
						break;
					else
						// Print process progress report
						System.out.println(response.getString());
				} catch (DialogueConnectionException e) {
					// Could not connect. We will assume the process is no longer alive and leave the
					// loop.
					System.out.println("Process died prematurely");
					break;
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			// Stop the dialogue. The dialogue cannot receive or send messages until it is restarted.
			if (dialogue != null)
				dialogue.stop();
			System.exit(0);
		}

	}

	protected static void startOtherProcess() {

		Runnable process = new Runnable() {

			protected String currentProgress = "Idle";

			public void run() {
				Dialogue dialogue = null;
				try {
					// Create the dialogue. Listen for messages on port 8099
					dialogue = DialogueFactory.createIPDialogue(8099);

					// Create the dialogue listener. This listener will respond to progress queries from the
					// observer.
					DialogueListener listener = new DialogueListener() {

						public void messageRecieved(DialogueServerMessage dialogMessage) {
							// A response was requested
							if (dialogMessage.responseRequested()) {
								// Report progress
								if (dialogMessage.getString().equals("REPORT")) {
									try {
										dialogMessage.respond(currentProgress);
									} catch (Exception e) {
										System.out.println(e);
									}
								}
							}

						}
					};

					// Add the listener to our dialogue
					dialogue.addListener(listener);

					// Start the dialogue. Once created, dialogues can be started and stopped at any time
					dialogue.start();

					// Set current progress
					currentProgress = "Process started";

					// Simulate some work
					Thread.sleep(2000);

					// Set current progress
					currentProgress = "Process step 1 completed";

					// Simulate some more work
					Thread.sleep(3000);

					// Set current progress
					currentProgress = "Process step 2 completed";

					// Simulate work
					Thread.sleep(1000);

					// Set current progress
					currentProgress = "Process completed";

					Thread.sleep(1000);

				} catch (Exception e) {
					System.out.println(e);
				} finally {
					// Stop the dialogue. The dialogue cannot receive or send messages until it is restarted.
					if (dialogue != null)
						dialogue.stop();
				}

			}
		};

		// Create and start the process thread
		Thread processThread = new Thread(process);
		processThread.start();

	}
}
