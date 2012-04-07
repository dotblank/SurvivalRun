package com.precipicegames.zeryl.survivalrun;

import com.precipicegames.zeryl.survivalrun.GameEvent.action;

public class GameEvent implements Runnable {
	public enum action {
		START,
		PAUSE,
		STOP,
		RESUME,
	}
	private action act;
	private String msg;
	private int ticks;
	private long waittime;
	private SurvivalRun plugin;
	public GameEvent(SurvivalRun pl ,int ticks, long time, String message, action a) {
		this.act = a;
		this.msg = message;
		this.ticks = ticks;
		this.waittime = time;
		this.plugin = pl;
	}
	public void run() {
		if(ticks <= 0) {
			switch(act) {
			case START:
				plugin.startGame();
				break;
			case STOP:
				plugin.stopGame();
				break;
			case PAUSE:
				plugin.pauseGame();
				break;
			case RESUME:
				plugin.resumeGame();
				break;
			}
		} else {
			plugin.getServer().broadcastMessage(msg + " " + ((ticks*waittime)/20) + " seconds");
			ticks--;
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, this.waittime);
		}
	}
}
