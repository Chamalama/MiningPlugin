package mike.miningPlugin.service;

import lombok.Getter;
import mike.miningPlugin.MiningPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class MiningAnimationService {

    private final Services services;

    public Map<UUID, List<UUID>> playerToItemDisplay = new ConcurrentHashMap<>();

    public MiningAnimationService(Services services) {
        this.services = services;
        runAnimationTask();
    }

    //This should be packets, but I never made it like this since it was a test
    public void spawnDisplayItem(Player player, Location location, Material material) {
        Item item = location.getWorld().dropItem(location, new ItemStack(material));
        item.setPickupDelay(Integer.MAX_VALUE);
        Bukkit.getScheduler().runTaskLater(MiningPlugin.INSTANCE, () -> {
            item.setOwner(player.getUniqueId());
        }, 5L);
        Bukkit.getScheduler().runTaskLater(MiningPlugin.INSTANCE, item::remove, 30L);
        playerToItemDisplay.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(item.getUniqueId());
    }

    public void runAnimationTask() {
        Bukkit.getScheduler().runTaskTimer(MiningPlugin.INSTANCE, () -> {
            Set<UUID> toClear = new HashSet<>();
            if(playerToItemDisplay.isEmpty()) return;
            for(Map.Entry<UUID, List<UUID>> activeItems : playerToItemDisplay.entrySet()) {
                final Player player = Bukkit.getPlayer(activeItems.getKey());
                if (player == null) continue;

                for (UUID itemID : activeItems.getValue()) {
                    Entity ent = Bukkit.getEntity(itemID);
                    Item item = (Item) ent;
                    if (item == null || item.getOwner() == null) continue;
                    Vector dir = player.getLocation().clone().add(0, 0.5, 0).toVector().subtract(item.getLocation().toVector()).normalize();
                    if (item.getLocation().distanceSquared(player.getLocation()) > 1) {
                        item.setVelocity(dir);
                    } else {
                        item.remove();
                        toClear.add(item.getUniqueId());
                        player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
                    }
                }
            }

            for(UUID id : toClear) {
                for(Map.Entry<UUID, List<UUID>> entries : playerToItemDisplay.entrySet()) {
                    entries.getValue().removeIf(id::equals);
                }
            }

        }, 1L, 1L);
    }

}
