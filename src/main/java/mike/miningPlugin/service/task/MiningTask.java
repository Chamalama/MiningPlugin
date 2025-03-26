package mike.miningPlugin.service.task;

import lombok.Getter;
import mike.miningPlugin.MiningPlugin;
import mike.miningPlugin.service.MiningService;
import mike.miningPlugin.service.Services;
import mike.miningPlugin.wrapper.MiningBlockWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class MiningTask implements Runnable {

    private final Services services;
    private final MiningService miningService;

    public MiningTask(Services services) {
        this.services = services;
        this.miningService = services.getMiningService();
    }

    @Override
    public void run() {
        if(miningService.getPlayerToBlock().isEmpty()) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        for(Map.Entry<UUID, Map<Long, MiningBlockWrapper>> blocks : miningService.getPlayerToBlock().entrySet()) {
            final Player player = Bukkit.getPlayer(blocks.getKey());
            if(player == null || !miningService.getPlayerTime().containsKey(player.getUniqueId())) continue;

            long playerTime = miningService.getPlayerTime().get(player.getUniqueId());
            Map<Long, MiningBlockWrapper> activeBlocks = blocks.getValue();

            for(MiningBlockWrapper blockWrapper : activeBlocks.values()) {
                if(!blockWrapper.isInteracted()) continue;
                if(!miningService.canMine(miningService.fromBlockPos(player.getWorld(), blockWrapper.getBlockPos()))) continue;
                if(!miningService.getCurrentMiningBlock().get(player.getUniqueId()).isInteracted()) continue;
                if(currentTime - playerTime >= blockWrapper.getTimeToMine()) {
                    miningService.incrementMiningBlock(player, miningService.fromBlockPos(player.getWorld(), blockWrapper.getBlockPos()));
                }
            }
        }
    }
}
