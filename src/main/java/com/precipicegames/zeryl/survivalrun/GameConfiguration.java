package com.precipicegames.zeryl.survivalrun;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GameConfiguration {
	protected Vector<Material> allowMined = new Vector<Material>();
	protected ArrayList<Location> spawnlocations = new ArrayList<Location>();
	protected boolean uniqueSpawn = true;
	protected boolean allowBreak = false;
	protected boolean allowPlace = true;
	protected boolean allowDynamicJoin = false;
	protected boolean clearInventories;
	protected int maxplayers;
	protected boolean retrySpawn;
}
