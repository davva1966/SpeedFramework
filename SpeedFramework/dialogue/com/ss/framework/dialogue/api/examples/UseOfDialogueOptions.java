// {{CopyrightNotice}}

package com.ss.framework.dialogue.api.examples;

import java.util.HashMap;
import java.util.Map;

import com.ss.framework.dialogue.api.Dialogue;
import com.ss.framework.dialogue.api.DialogueFactory;


public class UseOfDialogueOptions {

	// This example illustrates how to use the options of a dialogue. The supported options are implementation
	// specific.

	public static void main(String[] args) {

		try {
			// Create the dialogue
			Dialogue dialogue = DialogueFactory.createIPDialogue();

			// List the supported options
			Map<String, Class> supportedOptions = dialogue.getSupportedOptions();
			for (String optionName : supportedOptions.keySet()) {
				System.out.println("Option: " + optionName + " Value type: " + supportedOptions.get(optionName).getName());
			}

			// Set options. Options must be set before the dialogue is started. If options are changed when
			// the dialogue is started, it must be restarted before the new options take effect.
			Map<String, Object> newOptions = new HashMap<String, Object>();
			newOptions.put("SOCKET_IDLE_TIMEOUT_SECONDS", 120);
			newOptions.put("SOCKET_LIFETIME_SECONDS", 60);
			dialogue.setOptions(newOptions);

		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
