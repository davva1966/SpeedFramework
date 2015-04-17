// {{CopyrightNotice}}

package com.ss.framework.dialogue.ip;

import java.util.HashMap;
import java.util.Map;

import com.ss.framework.dialogue.AbstractDialogue;
import com.ss.framework.dialogue.DialogueEngine;
import com.ss.framework.dialogue.DialogueUnsupportedOptionException;


public class SocketDialogue extends AbstractDialogue {

	// Socket idle timeout option. Determines how long (in seconds) a socket is allowed to be idle (no
	// activity through it) before it is automatically closed. The next time a communication is requested to
	// the destination a new socket will be created and opened. Keeping this value high will reduce the number
	// of sockets used by the dialogue, but will make it more prone to failures due to network errors. The
	// default value for this property is 30 seconds.
	public static final String SOCKET_IDLE_TIMEOUT_SECONDS = "SOCKET_IDLE_TIMEOUT_SECONDS";

	// Socket lifetime option. Determines how long (in seconds) a socket is allowed to live before it is
	// automatically closed. The next time a communication is requested to the destination a new socket will
	// be created and opened. Keeping this value high will reduce the number of sockets used by the dialogue,
	// but will make it more prone to failures due to network errors. The default value for this property is
	// 120 seconds.
	public static final String SOCKET_LIFETIME_SECONDS = "SOCKET_LIFETIME_SECONDS";

	// Debug option. When this option is set to true additional debug messages are written to the system log.
	// The default value for this property is false.
	public static final String DEBUG = "DEBUG";

	protected static Map<String, Class> supportedOptions = new HashMap<String, Class>();

	static {
		supportedOptions.put(SOCKET_IDLE_TIMEOUT_SECONDS, Integer.class);
		supportedOptions.put(SOCKET_LIFETIME_SECONDS, Integer.class);
		supportedOptions.put(DEBUG, Boolean.class);
	}

	public SocketDialogue(DialogueEngine engine) {
		super(engine);

		// Set default options
		try {
			Map<String, Object> options = new HashMap<String, Object>();
			options.put(SOCKET_IDLE_TIMEOUT_SECONDS, 30);
			options.put(SOCKET_LIFETIME_SECONDS, 120);
			options.put(DEBUG, false);
			setOptions(options);
		} catch (DialogueUnsupportedOptionException e) {
			System.out.println(e);
		}
	}

	public void setOptions(Map<String, Object> options) throws DialogueUnsupportedOptionException {
		for (String option : options.keySet()) {
			if (supportedOptions.containsKey(option) == false)
				throw new DialogueUnsupportedOptionException("Option: " + option + " not supported. Use getSupportedOptions() to list the supported options.");

			if (options.get(option).getClass().equals(supportedOptions.get(option)) == false)
				throw new DialogueUnsupportedOptionException("Invalid type for option: " + option + ". Valid type is: " + supportedOptions.get(option));
		}
		super.setOptions(options);
	}

	public Map<String, Class> getSupportedOptions() {
		return supportedOptions;

	}
}
