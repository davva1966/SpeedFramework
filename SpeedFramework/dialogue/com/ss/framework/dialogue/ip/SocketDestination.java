// {{CopyrightNotice}}

package com.ss.framework.dialogue.ip;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;

import com.ss.framework.dialogue.AbstractDestination;
import com.ss.framework.dialogue.DialogueException;
import com.ss.framework.dialogue.api.Destination;


public class SocketDestination extends AbstractDestination implements Destination {

	public InetAddress hostAddress;

	public int port;

	public SocketChannel socketChannel;

	public SocketDestination(String alias, InetAddress hostAddress, int port) throws DialogueException {
		super();
		if (alias == null || alias.trim().length() == 0)
			throw new DialogueException("Alias name for the destination must be specified");
		if (port <= 0)
			throw new DialogueException("Remote port invalid");
		this.alias = alias;
		this.hostAddress = hostAddress;
		this.port = port;
	}

}
