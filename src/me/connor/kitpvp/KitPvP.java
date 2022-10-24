package me.connor.kitpvp;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import me.connor.kitpvp.TimeLock;

public class KitPvP extends JavaPlugin implements Listener, CommandExecutor
{
  public void onEnable()
  {
    getLogger().info("KitPvP has been enabled!");
    getServer().getPluginManager().registerEvents(this, this);
  }

  public void onDisable() {
    getLogger().info("KitPvP has been disabled!");
  }
}