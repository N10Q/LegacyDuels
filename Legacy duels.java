package me.xenpai.legacyduels;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class LegacyDuels extends JavaPlugin implements Listener, TabExecutor {

    private final Map<UUID, UUID> duelRequests = new HashMap<>();
    private final Set<UUID> inDuel = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("duel").setExecutor(this);
        getLogger().info("LegacyDuels enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LegacyDuels disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("legacyduels.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /duel <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }
        if (player.equals(target)) {
            player.sendMessage(ChatColor.RED + "You can't duel yourself!");
            return true;
        }

        if (inDuel.contains(player.getUniqueId()) || inDuel.contains(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Either you or that player is already in a duel!");
            return true;
        }

        if (duelRequests.containsKey(target.getUniqueId()) && duelRequests.get(target.getUniqueId()).equals(player.getUniqueId())) {
            startDuel(player, target);
            duelRequests.remove(target.getUniqueId());
            return true;
        }

        duelRequests.put(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "You sent a duel request to " + target.getName());
        target.sendMessage(ChatColor.AQUA + player.getName() + " has challenged you! Type /duel " + player.getName() + " to accept.");
        return true;
    }

    private void startDuel(Player p1, Player p2) {
        inDuel.add(p1.getUniqueId());
        inDuel.add(p2.getUniqueId());

        Location arena = new Location(p1.getWorld(), 0, 100, 0); // Change to your arena coords
        p1.teleport(arena.clone().add(5, 0, 0));
        p2.teleport(arena.clone().add(-5, 0, 0));

        giveKit(p1);
        giveKit(p2);

        Bukkit.broadcastMessage(ChatColor.GOLD + p1.getName() + " and " + p2.getName() + " have started a duel!");
    }

    private void giveKit(Player player) {
        player.getInventory().clear();
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Duel Sword");
            sword.setItemMeta(meta);
        }
        player.getInventory().addItem(sword);
        player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        player.updateInventory();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Player killer = dead.getKiller();
        if (killer != null && inDuel.contains(dead.getUniqueId()) && inDuel.contains(killer.getUniqueId())) {
            inDuel.remove(dead.getUniqueId());
            inDuel.remove(killer.getUniqueId());
            Bukkit.broadcastMessage(ChatColor.GOLD + killer.getName() + " won the duel against " + dead.getName() + "!");
        }
    }
}
name: LegacyDuels
main: me.xenpai.legacyduels.LegacyDuels
version: 1.0
api-version: 1.21
commands:
  duel:
    description: Send a duel request
    usage: /duel <player>
permissions:
  legacyduels.use:
    description: Allows using duel commands
    default: true
