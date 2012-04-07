package com.precipicegames.zeryl.survivalrun;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;

public class GameConfiguration {
	protected Vector<Material> allowMined = new Vector<Material>();
	protected ArrayList<Location> spawnlocations = new ArrayList<Location>();
	protected boolean uniqueSpawn = false;
	protected boolean allowBreak = false;
	protected boolean allowPlace = true;
	protected boolean allowDynamicJoin = false;
	protected boolean clearInventories = true;
	protected int maxplayers = 10;
	protected boolean retrySpawn = false;
	protected void load(ConfigurationSection config, Server s) {
		uniqueSpawn = config.getBoolean("unique-spawn", uniqueSpawn);
		allowBreak = config.getBoolean("allow-break", allowBreak);
		allowPlace = config.getBoolean("allow-place", allowPlace);
		allowDynamicJoin = config.getBoolean("allow-DynamicJoin", allowDynamicJoin);
		clearInventories = config.getBoolean("clear-inv", clearInventories);
		maxplayers = config.getInt("max-players", maxplayers);
		
		spawnlocations.clear();
		{
			ConfigurationSection CS = config.getConfigurationSection("spawn-locations");
			if(CS != null) {
				for(String key : CS.getKeys(false)) {
					ConfigurationSection cl = CS.getConfigurationSection(key);
					if(cl != null) {
						spawnlocations.add(LCS.fromConfig(cl, s));
					}
				}
			}
		}
		
		this.allowMined.clear();
		{
			List<Integer> ls = config.getIntegerList("allowed-mined");
			if(ls != null) {
				for(Integer i : ls) {
					this.allowMined.add(Material.getMaterial(i));
				}
			}
		}
	}
}
