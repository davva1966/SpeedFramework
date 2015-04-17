// {{CopyrightNotice}}

package com.ss.framework.dialogue.ip;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SocketChannelMonitor implements Runnable {

	protected boolean stop = false;

	// The socket timeout (if not used for this long the socket is closed)
	protected long idleTimeout = 20000;

	// How long the socket is allowed to live before being recreated (even if it's been used recently)
	protected long lifetime = 30000;

	// List of the socket channels we are watching
	protected Map<SocketChannel, MonitorEntry> monitorEntries = new HashMap<SocketChannel, MonitorEntry>();

	// True if debug messages whould be written
	protected boolean writeDebugMessages = false;

	protected class MonitorEntry {

		public SocketChannel socketChannel;

		// Time when the socket was created
		protected long creationTime = System.currentTimeMillis();

		// When the socket was last used
		protected long lastUsed = System.currentTimeMillis();

		public MonitorEntry(SocketChannel socketChannel) {
			this.socketChannel = socketChannel;
			this.creationTime = System.currentTimeMillis();
			this.lastUsed = creationTime;
		}

	}

	public SocketChannelMonitor() {

	}

	public void setDebugMode(boolean writeDebugMessages) {
		this.writeDebugMessages = writeDebugMessages;

	}

	public void setSocketIdleTimeout(int idleTimoutSeconds) {
		if (idleTimoutSeconds <= 0)
			idleTimoutSeconds = 1;
		this.idleTimeout = idleTimoutSeconds * 1000;

	}

	public void setSocketLifetime(int lifetimeSeconds) {
		if (lifetimeSeconds <= 0)
			lifetimeSeconds = 1;
		this.lifetime = lifetimeSeconds * 1000;

	}

	public void addSocketChannel(SocketChannel socketChannel) {
		monitorEntries.put(socketChannel, new MonitorEntry(socketChannel));
		if (writeDebugMessages)
			System.out.println("New Dialogue Client Socket channel added to monitor");

	}

	public void nudge(SocketChannel socketChannel) {
		MonitorEntry entry = monitorEntries.get(socketChannel);
		if (entry != null)
			entry.lastUsed = System.currentTimeMillis();
	}

	public void run() {
		while (stop == false) {
			try {
				synchronized (this) {
					try {
						wait(idleTimeout / 2);
					} catch (InterruptedException e) {
					}
				}

				Iterator<MonitorEntry> iterator = monitorEntries.values().iterator();
				while (iterator.hasNext()) {
					MonitorEntry entry = iterator.next();
					if (System.currentTimeMillis() - entry.lastUsed > idleTimeout) {
						entry.socketChannel.close();
						iterator.remove();
						if (writeDebugMessages)
							System.out.println("Dialogue Client Socket channel timout. Closing socket channel.");
					}
				}

			} catch (Exception e) {
				System.out.println(e);
			}
		}

	}

	public boolean socketChannelExpired(SocketChannel socketChannel) {
		MonitorEntry entry = monitorEntries.get(socketChannel);
		if (entry != null) {
			if (entry.socketChannel.isConnected()) {
				if (System.currentTimeMillis() - entry.creationTime > lifetime) {
					if (writeDebugMessages)
						System.out.println("Dialogue Client Socket channel reched the end of it's lifetime (" + lifetime * 1000 + " seconds)");
					return true;
				}
			}
		}

		return false;
	}

	public synchronized void stop() {
		stop = true;

		Iterator<MonitorEntry> iterator = monitorEntries.values().iterator();
		while (iterator.hasNext()) {
			MonitorEntry entry = iterator.next();
			try {
				if (entry.socketChannel.isConnected()) {
					entry.socketChannel.close();
					if (writeDebugMessages)
						System.out.println("Closing Dialogue Client socket channel.");
				}
			} catch (Exception e) {
			}
		}

		notify();
	}

	public void start() {
		stop = false;
		monitorEntries.clear();
	}

}
