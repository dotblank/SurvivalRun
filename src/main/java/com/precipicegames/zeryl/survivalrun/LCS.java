package com.precipicegames.zeryl.survivalrun;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

public class LCS {
  public static Location fromConfig(ConfigurationSection cs, Server s) {
    double x = cs.getDouble("x", 0.0);
    double y = cs.getDouble("y", 128.0);
    double z = cs.getDouble("z", 0.0);
    float yaw = (float) cs.getDouble("yaw", 0.0);
    float pitch = (float) cs.getDouble("pitch", 0.0);
    String world = cs.getString("world", s.getWorlds().get(0).getName());
    return new Location(s.getWorld(world), x, y, z, yaw, pitch);
  }

  public static ConfigurationSection toConfig(Location l) {
    MemoryConfiguration config = new MemoryConfiguration();
    if (l != null) {
      config.set("x", l.getX());
      config.set("y", l.getY());
      config.set("z", l.getZ());
      config.set("yaw", l.getYaw());
      config.set("pitch", l.getPitch());
      config.set("world", l.getWorld().getName());
    }
    return config;
  }
}
