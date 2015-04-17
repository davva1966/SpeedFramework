// {{CopyrightNotice}}

package com.ss.framework.dialogue.api.examples;

import com.ss.framework.dialogue.api.Dialogue;
import com.ss.framework.dialogue.api.DialogueFactory;
import com.ss.framework.dialogue.api.DialogueListener;
import com.ss.framework.dialogue.api.DialogueServerMessage;

public class ProcessListener1 {

	// This example listens and reports on the progress of a another process. In this example the observed
	// process notifies the observer of it's progress. I.e the observer is the server and the observed process
	// is the client.

	protected static boolean exit = false;

	public static void main(String[] args) {

		Dialogue dialogue = null;

		try {
			// Create the dialogue. When a local port is specified the Dialogue will act as a server and will
			// accept incoming dialogue messages. We will accept incoming dialogue messages on port 8098
			dialogue = DialogueFactory.createIPDialogue("127.0.0.1", 8098);

			// Create the dialogue listener. This is only needed when we service incoming messages. The
			// dialogue listener handles those messages. Here we will print the incoming status messages. If
			// message "EXIT" is received the program will exit.
			DialogueListener listener = new DialogueListener() {

				public void messageRecieved(DialogueServerMessage dialogMessage) {
					// Get the message as a String
					String message = dialogMessage.getString();

					// Print the message
					System.out.println(message);

					// If exit message received, the process has ended so we can stop listening
					if (message.equals("EXIT")) {
						synchronized (this) {
							exit = true;
							notify();
						}
					}

				}
			};

			// Add the listener to our dialogue
			dialogue.addListener(listener);

			// Start the dialogue. Once created, dialogues can be started and stopped at any time
			dialogue.start();

			// Start the observed process.
			startOtherProcess();

			// Run until observed process has completed.
			while (exit == false) {
				synchronized (listener) {
					listener.wait();
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

			public void run() {
				Dialogue dialogue = null;
				try {
					// Create the dialogue. When no port is specified the Dialogue will act as a client only
					// and cannot receive incoming dialogue messages.
					dialogue = DialogueFactory.createIPDialogue();

					// Add the remote destination. Enables this dialogue to send messages to remote
					// destinations. In this case we only add one remote destination (The observer of this
					// process). The specified destination alias "Observer" is used in subsequent dialogue
					// communication with this destination. Destinations can be added to or removed from the
					// dialogue at any point.
					dialogue.addDestination(DialogueFactory.createIPDestination("Observer", "127.0.0.1", 8098));

					// Start the dialogue. Once created, dialogues can be started and stopped at any time
					dialogue.start();

					// Send started message
					dialogue.sendMessage("Observer", "Process started");

					// Simulate some work
					Thread.sleep(2000);

					// Send status message 1
					dialogue.sendMessage("Observer", "Process step 1 completed");

					// Simulate some more work
					Thread.sleep(3000);

					// Send status message 2
					dialogue.sendMessage("Observer", "Process step 2 completed");

					// Simulate work
					Thread.sleep(1000);

					// Send completion message
					dialogue.sendMessage("Observer", "Process completed");

					// Send exit message.
					dialogue.sendMessage("Observer", "EXIT");

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
