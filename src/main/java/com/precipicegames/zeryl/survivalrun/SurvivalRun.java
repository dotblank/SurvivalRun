package com.precipicegames.zeryl.survivalrun;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;

import com.avaje.ebeaninternal.server.lib.sql.DataSourcePool.Status;

/**
 *
 * @author Zeryl
 * @author dotblank
 */
public class SurvivalRun extends JavaPlugin implements Listener {
	public enum GameState {
		RUNNING,
		PAUSED
	}
	public enum PlayerStatus {
		PREJOIN,
		PREPARED,
		DEAD
	}
	GameConfiguration game = new GameConfiguration();
	protected HashMap<String,PlayerStatus> participatingPlayers = new HashMap<String,PlayerStatus>();
	private GameState state = GameState.PAUSED;

	
	
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        PluginDescriptionFile pdf = this.getDescription();
        System.out.println(pdf.getName() + " is now enabled.");
    }

    public void onDisable() {
        PluginDescriptionFile pdf = this.getDescription();
        System.out.println(pdf.getName() + " is now disabled.");
    }
    
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
    	if(e.getPlayer().hasPermission("survivalrun.bypass")) {
    		return;
    	}
    	if(this.state == GameState.PAUSED) {
    		e.setCancelled(true);
    	} else {
	    	if(!game.allowBreak && !game.allowMined.contains(e.getBlock().getType())) {
	    		e.setCancelled(true);
	    	}
    	}
    }
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
    	if(e.getPlayer().hasPermission("survivalrun.bypass")) {
    		return;
    	}
    	if(this.state == GameState.PAUSED) {
    		e.setCancelled(true);
    	} else {
	    	if(!game.allowPlace && !participatingPlayers.containsKey(e.getPlayer().getName())) {
	    		e.setCancelled(true);
	    	}
    	}
    }
    
    public void startGame() {
    	
    }
    
    public void addPlayer(Player e) {
    	this.addPlayer(e, e.getName());
    }
    public void addPlayer(CommandSender s, String player) {
    	if(this.participatingPlayers.containsKey(player)) {
    		s.sendMessage("Player is already a participant");
    		return;
    	}
    	
    	if(state == GameState.RUNNING && game.allowDynamicJoin) {
    		Player p = this.getServer().getPlayerExact(player);
    		if(p != null) {
    			preparePlayer(p);
    		}
    	} else if (state == GameState.RUNNING) {
    		s.sendMessage("Players may not join an already running game");
    		return;
    	}
    	this.participatingPlayers.put(player, PlayerStatus.PREJOIN);
    	if(s != null) {
    		s.sendMessage(player + " has been added to the players list");
    	}
    }
    
    public void preparePlayer(Player p) {
		if(game.clearInventories) {
			p.getInventory().clear();
		}
		p.setTotalExperience(0);
		p.teleport(game.getSpawn(p));
		this.participatingPlayers.put(p.getName(), PlayerStatus.PREPARED);
    }
    
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
    	String name = e.getPlayer().getName();
    	PlayerStatus pstatus = this.participatingPlayers.get(name);
    	if(e.getPlayer().hasPermission("survivalrun.login")) {
    		return;
    	}
    	if(state == GameState.RUNNING && (pstatus == null || pstatus == PlayerStatus.DEAD)) {
    		e.disallow(Result.KICK_OTHER, "Game is in progress please wait until next event");
    	}
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
    	String name = e.getPlayer().getName();
    	PlayerStatus pstatus = this.participatingPlayers.get(name);
    	if(state == GameState.RUNNING && pstatus == PlayerStatus.PREJOIN) {
    		this.preparePlayer(e.getPlayer());
    	}
    }
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
    	if(e.getPlayer().hasPermission("survivalrun.bypass")) {
    		return;
    	}
    	String name = e.getPlayer().getName();
    	PlayerStatus pstatus = this.participatingPlayers.get(name);
    	if(pstatus != PlayerStatus.PREPARED || state == GameState.PAUSED) {
    		if(!e.getFrom().toVector().equals(e.getTo().toVector())) {
    			e.setCancelled(true);
    		}
    	}
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
    	if(state == GameState.PAUSED) {
    		e.setCancelled(true);
    	}
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {


        if (cmd.getName().equalsIgnoreCase("dnp")) {
            if (sender instanceof Player) {
            }
        }


        return true;
    }
}