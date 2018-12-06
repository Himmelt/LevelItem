package me.cilio.lvlitem;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.soraworld.violet.yaml.IYamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LevelItem extends JavaPlugin implements Listener {

    private String levelString = "Required Level";
    private final IYamlConfiguration config = new IYamlConfiguration();
    private static final Pattern LEVEL = Pattern.compile("\\d+");

    @Override
    public void onEnable() {
        File file = new File(getDataFolder(), "config.yml");
        Bukkit.getPluginManager().registerEvents(this, this);
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException ignored) {
        }
        levelString = config.getString("needLevel", "Required Level");
        config.set("needLevel", levelString);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        if (human instanceof Player) checkEquipments((Player) human);
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (player != null && item != null) {
            int level = player.getLevel();
            if (cantUseStack(level, item)) event.setCancelled(true);
            checkEquipments(player);
        }
    }

    @EventHandler
    void onPlayerAttack(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            ItemStack item = ((Player) entity).getItemInHand();
            if (item != null) {
                if (cantUseStack(player.getLevel(), item)) event.setCancelled(true);
            }
        }
    }

    private void checkEquipments(Player player) {
        int level = player.getLevel();
        World world = player.getWorld();
        Location location = player.getLocation();
        PlayerInventory inv = player.getInventory();

        ItemStack stack = inv.getHelmet();
        if (stack != null && cantUseStack(level, stack)) {
            inv.setHelmet(null);
            world.dropItemNaturally(location, stack);
        }

        stack = inv.getChestplate();
        if (stack != null && cantUseStack(level, stack)) {
            inv.setChestplate(null);
            world.dropItemNaturally(location, stack);
        }

        stack = inv.getLeggings();
        if (stack != null && cantUseStack(level, stack)) {
            inv.setLeggings(null);
            world.dropItemNaturally(location, stack);
        }

        stack = inv.getBoots();
        if (stack != null && cantUseStack(level, stack)) {
            inv.setBoots(null);
            world.dropItemNaturally(location, stack);
        }
    }

    private boolean cantUseStack(int level, ItemStack stack) {
        int needLevel = 0;
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (String line : lore) {
                    if (line.contains(levelString)) {
                        needLevel = parseLevel(line);
                    }
                }
            }
        }
        return level < needLevel;
    }

    private int parseLevel(String line) {
        int lvl = -1;
        Matcher matcher = LEVEL.matcher(line);
        if (matcher.find()) {
            try {
                lvl = Integer.parseInt(matcher.group());
            } catch (Throwable ignored) {
            }
        }
        return lvl;
    }
}
