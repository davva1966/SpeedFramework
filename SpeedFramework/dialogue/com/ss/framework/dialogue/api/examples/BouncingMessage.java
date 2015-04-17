// {{CopyrightNotice}}

package com.ss.framework.dialogue.api.examples;

import com.ss.framework.dialogue.api.Dialogue;
import com.ss.framework.dialogue.api.DialogueFactory;
import com.ss.framework.dialogue.api.DialogueListener;
import com.ss.framework.dialogue.api.DialogueServerMessage;

public class BouncingMessage {

	// This example bounces a message back and forth between two processes. The message will bounce 50 times.

	public static void main(String[] args) {

		final Dialogue dialogue;

		try {

			// Create the dialogue. This dialogue will act as both server and client. We will listen for
			// incoming messages on port 8098.
			dialogue = DialogueFactory.createIPDialogue(8098);

			// Add the remote bouncer destination.
			dialogue.addDestination(DialogueFactory.createIPDestination("Bouncer2", 8099));

			// Create the dialogue listener. This listener adds a period to the end of the message and
			// bounces it back
			DialogueListener listener = new DialogueListener() {

				int count = 0;

				public void messageRecieved(DialogueServerMessage dialogMessage) {
					try {
						// Print message
						System.out.println(dialogMessage.getString());

						// Short delay
						Thread.sleep(50);

						// Bounce the message back
						if (count < 50)
							dialogue.sendMessage("Bouncer2", dialogMessage.getString() + ".");
						else
							dialogue.sendMessage("Bouncer2", "STOP");
						count++;
					} catch (Exception e) {
						System.out.println(e);
					}
				}

			};

			// Add the listener to our dialogue
			dialogue.addListener(listener);

			// Start the dialogue. Once created, dialogues can be started and stopped at any time
			dialogue.start();

			// Start the remote bouncer process.
			startOtherBouncer();

			// Kick off
			dialogue.sendMessage("Bouncer2", "Bouncing message");

			// Allow some time to complete
			Thread.sleep(10000);

			// Stop the dialogue. The dialogue cannot receive or send messages until it is restarted.
			if (dialogue != null)
				dialogue.stop();

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			System.exit(0);
		}

	}

	protected static void startOtherBouncer() {

		Runnable process = new Runnable() {

			protected boolean stop = false;

			public void run() {
				final Dialogue dialogue;
				try {
					// Create the dialogue. This dialogue will act as both server and client. We will listen
					// for
					// incoming messages on port 8099.
					dialogue = DialogueFactory.createIPDialogue(8099);

					// Add the remote bouncer destination.
					dialogue.addDestination(DialogueFactory.createIPDestination("Bouncer1", 8098));

					// Create the dialogue listener. This listener add a period to the end of the message and
					// bounce it back
					DialogueListener listener = new DialogueListener() {

						public void messageRecieved(DialogueServerMessage dialogMessage) {

							// Check if STOP request
							if (dialogMessage.getString().equals("STOP")) {
								synchronized (this) {
									stop = true;
									notify();
									return;
								}
							}

							try {
								// Bounce the message back
								dialogue.sendMessage("Bouncer1", dialogMessage.getString() + ".");
							} catch (Exception e) {
								System.out.println(e);
							}

						}
					};

					// Add the listener to our dialogue
					dialogue.addListener(listener);

					// Start the dialogue. Once created, dialogues can be started and stopped at any time
					dialogue.start();

					// Run until stop was requested
					synchronized (listener) {
						while (stop == false)
							listener.wait();
					}

					// Stop the dialogue. The dialogue cannot receive or send messages until it is restarted.
					if (dialogue != null)
						dialogue.stop();

				} catch (Exception e) {
					System.out.println(e);
				}

			}
		};

		// Create and start the process thread
		Thread processThread = new Thread(process);
		processThread.start();

	}
}
