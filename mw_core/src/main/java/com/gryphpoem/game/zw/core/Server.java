package com.gryphpoem.game.zw.core;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class Server implements Runnable {
	private static Logger log = LogManager.getLogger(Server.class);

	public static final String DEFAULT_MAIN_THREAD = "Main";
	protected String name;

	protected Server(String name) {
		this.name = name;
	}

	public abstract String getGameType();

	protected abstract void stop();

	private class CloseByExit implements Runnable {
		private String serverName;

		public CloseByExit(String serverName) {
			this.serverName = serverName;
		}

		public void run() {
			Server.this.stop();
			log.error(this.serverName + " Stop!!");
		}
	}

	public void run() {
		Runtime.getRuntime().addShutdownHook(new Thread(new CloseByExit(name)));
	}
}
