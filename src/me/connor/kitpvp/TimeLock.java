package me.connor.kitpvp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class TimeLock extends JavaPlugin
{
  private static YamlConfiguration myConfig;
  private static File configFile;
  private static boolean loaded = false;
  public long refreshRate = 100L;

  public YamlConfiguration getConfig() {
    if (!loaded) {
      loadConfig();
    }
    return myConfig;
  }
  public static File getConfigFile() {
    return configFile;
  }
  public static void loadConfig() {
    configFile = new File(Bukkit.getServer().getPluginManager().getPlugin("TimeLock").getDataFolder(), "timelock.yml");
    if (configFile.exists()) {
      myConfig = new YamlConfiguration();
      try {
        myConfig.load(configFile);
      } catch (FileNotFoundException localFileNotFoundException) {
      } catch (IOException localIOException) {
      } catch (InvalidConfigurationException localInvalidConfigurationException) {
      }
      loaded = true;
    } else {
      try {
        Bukkit.getServer().getPluginManager().getPlugin("TimeLock").getDataFolder().mkdir();
        InputStream jarURL = TimeLock.class.getResourceAsStream("/timelock.yml");
        copyFile(jarURL, configFile);
        myConfig = new YamlConfiguration();
        myConfig.load(configFile);
        loaded = true; } catch (Exception localException) {
      }
    }
  }

  private static void copyFile(InputStream in, File out) throws Exception {
    InputStream fis = in;
    FileOutputStream fos = new FileOutputStream(out);
    try {
      byte[] buf = new byte[1024];
      int i = 0;
      while ((i = fis.read(buf)) != -1)
        fos.write(buf, 0, i);
    }
    catch (Exception e) {
      throw e;
    } finally {
      if (fis != null) {
        fis.close();
      }
      if (fos != null)
        fos.close();
    }
  }

  public void onDisable()
  {
    getServer().getScheduler().cancelTasks(this);
    System.out.println("[TimeLock] Disabled");
  }
  public void onEnable() {
    System.out.println("[TimeLock] Enabled");
    beginLock(false);
  }

  public boolean isInt(String input)
  {
    try
    {
      Integer.parseInt(input);
      return true;
    }
    catch (Exception ex) {
    }
    return false;
  }

  public boolean isLong(String input)
  {
    try
    {
      Long.parseLong(input);
      return true;
    }
    catch (Exception ex) {
    }
    return false;
  }

  public void beginLock(boolean refresh)
  {
    if (refresh) {
      getServer().getScheduler().cancelTasks(this);
    }

    String rTime = getConfig().getString("Refresh-Rate");
    if (isLong(rTime))
      this.refreshRate = (Long.parseLong(rTime) * 20L);
    if (this.refreshRate == 0L) {
      this.refreshRate = 100L;
    }
    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        for (World world : TimeLock.this.getServer().getWorlds()) {
          String info = TimeLock.this.getConfig().getString("World." + world.getName());
          if ("day".equals(info)) {
            world.setTime(6000L);
          }
          else if ("night".equals(info)) {
            world.setTime(18000L);
          }
          else if (TimeLock.this.isInt(info))
            world.setTime(Integer.parseInt(info));
        }
      }
    }
    , 0L, this.refreshRate);
  }

  public void lockTime(String world, String time, CommandSender sender) {
    if (Bukkit.getServer().getWorld(world) != null) {
      if (isInt(time)) {
        if ((Integer.parseInt(time) > -1) && (Integer.parseInt(time) < 24001)) {
          getConfig().set("World." + world, time);
          saveConfig();
          sender.sendMessage("[TimeLock] " + ChatColor.GREEN + world + ChatColor.AQUA + " locked to " + ChatColor.GREEN + time + " Ticks");
        }
        else {
          sender.sendMessage("[TimeLock] " + ChatColor.RED + "Please use a number between 0-24000");
        }

      }
      else if ((time.equals("day")) || (time.equals("night")) || (time.equals("normal"))) {
        getConfig().set("World." + world, time);
        saveConfig();
        sender.sendMessage("[TimeLock] " + ChatColor.GREEN + world + ChatColor.AQUA + " locked to " + ChatColor.GREEN + time);
      }
      else {
        sender.sendMessage("[TimeLock] " + ChatColor.RED + "Invalid time listed. Use day, night, or normal.");
      }
    }
    else
    {
      sender.sendMessage("[TimeLock] " + ChatColor.RED + "Invalid world: " + world);
    }
  }

  public void setRefresh(String rate, CommandSender sender)
  {
    if (isInt(rate))
    {
      if (Integer.parseInt(rate) > 0)
      {
        getConfig().set("Refresh-Rate", rate);
        saveConfig();
        beginLock(true);
        sender.sendMessage("[TimeLock] " + ChatColor.GREEN + "Refresh Rate set to " + ChatColor.AQUA + rate + ChatColor.GREEN + " seconds.");
      }
      else {
        sender.sendMessage("[TimeLock] " + ChatColor.RED + "Invalid rate given. Refresh rate must be greater than 0.");
      }
    }
    else
      sender.sendMessage("[TimeLock] " + ChatColor.RED + "Invalid rate given. Refresh rate must be an integer.");
  }

  public void deny(CommandSender sender)
  {
    sender.sendMessage("[TimeLock] " + ChatColor.RED + "This command can not be run from the console. Use /tl set [world] [time]");
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    if (commandLabel.equalsIgnoreCase("TimeLock")) {
      sender.sendMessage(ChatColor.AQUA + "-------------TimeLock-------------");
      sender.sendMessage(ChatColor.DARK_GREEN + "Time freezing plugin by ThatBritishGuy");
      sender.sendMessage(ChatColor.AQUA + "Made for Quantekk KitPvP");
      return true;
    }
    if (commandLabel.equalsIgnoreCase("tl"))
    {
      Player p = null;
      String worldName = null;
      World current = null;
      if ((sender instanceof Player)) {
        p = (Player)sender;
        worldName = p.getWorld().getName();
        current = p.getWorld();
      }

      if (args.length == 1)
      {
        if (args[0].equalsIgnoreCase("reload")) {
          if ((sender instanceof Player)) {
            if ((p.hasPermission("timelock.admin")) || (p.isOp())) {
              beginLock(true);
              sender.sendMessage(ChatColor.GREEN + "TimeLock Reloaded");
              return true;
            }

            p.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            return true;
          }

          beginLock(true);
          sender.sendMessage(ChatColor.GREEN + "TimeLock Reloaded");
          return true;
        }

        if (args[0].equalsIgnoreCase("day")) {
          if ((sender instanceof Player)) {
            if ((p.hasPermission("timelock.admin")) || (p.isOp())) {
              lockTime(worldName, "day", p);
              current.setTime(6000L);
              return true;
            }

            p.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            return true;
          }

          deny(sender);
          return true;
        }

        if (args[0].equalsIgnoreCase("night")) {
          if ((sender instanceof Player)) {
            if ((p.hasPermission("timelock.admin")) || (p.isOp())) {
              lockTime(worldName, "night", p);
              current.setTime(18000L);
              return true;
            }

            p.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            return true;
          }

          deny(sender);
          return true;
        }

        if (isInt(args[0])) {
          if ((sender instanceof Player)) {
            if ((p.hasPermission("timelock.admin")) || (p.isOp())) {
              lockTime(worldName, args[0], p);
              int time = Integer.parseInt(args[0]);
              current.setTime(time);
              return true;
            }

            p.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            return true;
          }

          deny(sender);
          return true;
        }

        if (args[0].equalsIgnoreCase("normal")) {
          if ((sender instanceof Player)) {
            if ((p.hasPermission("timelock.admin")) || (p.isOp())) {
              lockTime(worldName, "normal", p);
              return true;
            }

            p.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            return true;
          }

          deny(sender);
          return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
          if ((sender instanceof Player)) {
            if ((p.hasPermission("timelock.admin")) || (p.isOp())) {
              p.sendMessage(ChatColor.AQUA + "/tl set [world] [time]");
              return true;
            }

            p.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            return true;
          }

          sender.sendMessage(ChatColor.AQUA + "/tl set [world] [time]");
          return true;
        }

      }
      else if (args.length == 2)
      {
        if ((args[0].equalsIgnoreCase("refresh")) && (args[1] != null)) {
          if ((sender instanceof Player)) {
            if ((p.hasPermission("timelock.admin")) || (p.isOp())) {
              setRefresh(args[1], sender);
              return true;
            }

            p.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            return true;
          }

          setRefresh(args[1], sender);
          return true;
        }

      }
      else if ((args.length == 3) && 
        (args[0].equals("set")) && 
        (args[1] != null) && (args[2] != null)) {
        worldName = args[1].toString();
        String time = args[2].toString();
        if ((sender instanceof Player)) {
          if ((p.hasPermission("timelock.admin")) || (p.isOp())) {
            lockTime(worldName, time, sender);
            return true;
          }

          p.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
          return true;
        }

        lockTime(worldName, time, sender);
        return true;
      }

    }

    return false;
  }
}