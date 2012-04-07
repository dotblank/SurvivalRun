package com.precipicegames.zeryl.survivalrun;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author Zeryl
 * @author dotblank
 */
public class SurvivalRun extends JavaPlugin implements Listener {
	public enum GameState {
		RUNNING,
		PAUSED,
		STOPPED,
	}
	public enum PlayerStatus {
		PREJOIN,
		PREPARED,
		PLAYING,
		DEAD
	}
	GameConfiguration game = new GameConfiguration();
	protected HashMap<String,PlayerStatus> participatingPlayers = new HashMap<String,PlayerStatus>();
	private GameState state = GameState.PAUSED;
	private ArrayList<Location> availablespawns;
	private Random rand = new Random(System.currentTimeMillis());;

	
	
    public void onEnable() {
    	this.getDataFolder().mkdirs();
    	File file = new File(this.getDataFolder(),"config.yml");
    	YamlConfiguration config = new YamlConfiguration();
    	try {
			config.load(file);
		} catch (Exception e) {
			game.spawnlocations.add(this.getServer().getWorlds().get(0).getSpawnLocation());
		}
		
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this);
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
    	if(this.state == GameState.PAUSED || state == GameState.STOPPED) {
    		e.setCancelled(true);
    	} else {
	    	if(!game.allowBreak && !game.allowMined.contains(e.getBlock().getType()) 
	    			&& !(participatingPlayers.get(e.getPlayer().getName()) == PlayerStatus.PLAYING)) {
	    		e.setCancelled(true);
	    	}
    	}
    }
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
    	if(e.getPlayer().hasPermission("survivalrun.bypass")) {
    		return;
    	}
    	if(state == GameState.PAUSED || state == GameState.STOPPED) {
    		e.setCancelled(true);
    	} else {
	    	if(!game.allowPlace && !(participatingPlayers.get(e.getPlayer().getName()) == PlayerStatus.PLAYING)) {
	    		e.setCancelled(true);
	    	}
    	}
    }

    
    public void addPlayer(Player e) {
    	this.addPlayer(e, e.getName());
    }
    public void addPlayer(CommandSender s, String player) {
    	if(this.participatingPlayers.containsKey(player)) {
    		s.sendMessage("Player is already a participant");
    		return;
    	}
    	if(game.maxplayers > 0) {
    		if(this.participatingPlayers.size() >= game.maxplayers) {
    			s.sendMessage("Currently too many players");
    			return;
    		}
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
    	PlayerStatus status = this.participatingPlayers.get(p.getName());
    	if(status == PlayerStatus.PREPARED || status == PlayerStatus.PLAYING) {
    		return;
    	}
		if(game.clearInventories) {
			p.getInventory().clear();
		}
		p.setTotalExperience(0);
		try {
			p.teleport(getSpawn(p));
		} catch (NoSpawn e) {
			if(game.retrySpawn) {
				this.getServer().getScheduler().scheduleSyncDelayedTask(this, new RetryPrepareEvent(this,p), 20*10);
			}
			return;
		}
		this.participatingPlayers.put(p.getName(), PlayerStatus.PREPARED);
    }
    
    private Location getSpawn(Player p) throws NoSpawn {
    	if(this.game.uniqueSpawn == true) {
    		if(this.availablespawns.size() <= 0) {
    			throw new NoSpawn();
    		}
    		int index = rand.nextInt(this.availablespawns.size());
    		Location l = this.availablespawns.get(index);
    		if(l == null) {
    			throw new NoSpawn();
    		}
    		this.availablespawns.remove(index);
    		return l;
    	} else {
    		return this.availablespawns.get(rand.nextInt(this.availablespawns.size()));
    	}
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
    		this.startPlayer(e.getPlayer());
    	}
    }
    private void startPlayer(Player player) {
    	this.participatingPlayers.put(player.getName(), PlayerStatus.PLAYING);
	}

	@EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
    	if(e.getPlayer().hasPermission("survivalrun.bypass")) {
    		return;
    	}
    	String name = e.getPlayer().getName();
    	PlayerStatus pstatus = this.participatingPlayers.get(name);
    	if(pstatus != PlayerStatus.PLAYING || state == GameState.PAUSED) {
    		if(!e.getFrom().toVector().equals(e.getTo().toVector())) {
    			e.setCancelled(true);
    		}
    	}
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
    	if(state == GameState.PAUSED || state == GameState.STOPPED) {
    		e.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
    	if(e.getPlayer().hasPermission("survivalrun.bypass")) {
    		return;
    	}
    	if(state == GameState.PAUSED || state == GameState.STOPPED) {
    		e.setCancelled(true);
    	}
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {


        if (cmd.getName().equalsIgnoreCase("game")) {
        	if(args.length == 0) {
        		displayHelp(sender);
        	}
        	if(args.length >= 1) {
        		if(args[0].equalsIgnoreCase("start")) {
        			if(state == GameState.RUNNING) {
        				sender.sendMessage("Game is already running");
        				return true;
        			}
        			if(state == GameState.STOPPED) {
        				sender.sendMessage("Starting game");
        				startGame(5,20, "Starting the game in");
        			}
        			if(state == GameState.PAUSED) {
        				sender.sendMessage("Resuming game");
        				resumeGame(5,20, "Resuming the game in");
        			}
        		}
        		if(args[0].equalsIgnoreCase("stop")) {
        			if(state == GameState.STOPPED) {
        				sender.sendMessage("Game has already been stopped");
        				return true;
        			}
        			stopGame();
        		}
        		if(args[0].equalsIgnoreCase("prepare")) {
        			if(state != GameState.STOPPED) {
        				sender.sendMessage("You can only prepare players when the server is stopped");
        				return true;
        			}
        			prepareGame();
        		}
        		if(args[0].equalsIgnoreCase("pause")) {
        			if(state == GameState.STOPPED || state == GameState.PAUSED) {
        				sender.sendMessage("Game is not running!");
        				return true;
        			}
        			pauseGame();
        		}
        		if(args[0].equalsIgnoreCase("add")) {
        			int count = args.length - 1;
        			for(int i = 0; i < count; i++) {
        				Player p = this.getServer().getPlayer(args[i + 1]);
        				if(p != null) {
        					this.addPlayer(sender, p.getName());
        				} else {
        					this.addPlayer(sender, args[i+1]);
        					sender.sendMessage("WARN: " + args[i+1] + "is not online adding offline player");
        				}
        			}
        			if(count == 0) {
        				sender.sendMessage("You need to specify at least one player");
        			}
        		}
        	}
        }
        if (cmd.getName().equalsIgnoreCase("join") && sender instanceof Player) {
        	Player p = (Player)sender;
        	if(args.length != 0) {
        	
        	}
        	this.addPlayer(p);
        }
        return true;
    }

	public void pauseGame() {
		if(state == GameState.RUNNING) {
			state = GameState.PAUSED;
			this.getServer().broadcastMessage("Game has paused!");
		}
	}

	public void resumeGame() {
		if(state == GameState.PAUSED) {
			state = GameState.RUNNING;
			this.getServer().broadcastMessage("Game has resumed!");
		}
	}
	
	public void stopGame() {
		this.state = GameState.STOPPED;
		//refresh the available spawn locations
		this.availablespawns = new ArrayList<Location>();
		this.availablespawns.addAll(game.spawnlocations);
		
		//Remove the player list
		this.participatingPlayers.clear();
	}
	public void prepareGame() {
		for(String player : this.participatingPlayers.keySet()) {
			Player p = this.getServer().getPlayerExact(player);
			if(p != null) {
				this.preparePlayer(p);
			}
		}
	}
	public void startGame() {

		prepareGame();
		for(String player : this.participatingPlayers.keySet()) {
			Player p = this.getServer().getPlayerExact(player);
			if(p != null) {
				this.startPlayer(p);
			}
		}
		this.state = GameState.RUNNING;
		this.getServer().broadcastMessage("Game has started!");
	}
	
    public void startGame(int i,long wait, String message) {
    	this.getServer().getScheduler().scheduleSyncDelayedTask(this, new GameEvent(this,i,wait,message,GameEvent.action.START));
    }
    public void stopGame(int i,long wait, String message) {
    	this.getServer().getScheduler().scheduleSyncDelayedTask(this, new GameEvent(this,i,wait,message,GameEvent.action.STOP));
    }
    public void pauseGame(int i,long wait, String message) {
    	this.getServer().getScheduler().scheduleSyncDelayedTask(this, new GameEvent(this,i,wait,message,GameEvent.action.PAUSE));
    }
    public void resumeGame(int i,long wait, String message) {
    	this.getServer().getScheduler().scheduleSyncDelayedTask(this, new GameEvent(this,i,wait,message,GameEvent.action.RESUME));
    }

	private void displayHelp(CommandSender sender) {
		// TODO Auto-generated method stub
		
	}

}