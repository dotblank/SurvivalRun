package com.precipicegames.zeryl.survivalrun;

import java.lang.ref.WeakReference;

import org.bukkit.entity.Player;

public class RetryPrepareEvent implements Runnable {
	WeakReference<Player> weakplayer;
	private SurvivalRun plugin;
	public RetryPrepareEvent(SurvivalRun survivalRun, Player p) {
		weakplayer =  new WeakReference<Player>(p);
		plugin = survivalRun;
	}

	public void run() {
		Player p = weakplayer.get();
		if(p != null) {
			plugin.preparePlayer(p);
		}
	}

}
