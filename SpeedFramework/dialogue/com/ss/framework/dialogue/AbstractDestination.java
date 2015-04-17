package com.ss.framework.dialogue;

import com.ss.framework.dialogue.api.Destination;

public abstract class AbstractDestination implements Destination {

	protected String alias = null;

	public String getAlias() {
		return alias;
	}

}
