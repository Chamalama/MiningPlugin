package mike.miningPlugin.service.task;

import mike.miningPlugin.MiningPlugin;
import mike.miningPlugin.service.MiningService;
import mike.miningPlugin.service.Services;
import mike.miningPlugin.wrapper.BrokenBlockWrapper;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class MiningBlockReplaceTask implements Runnable {

    private final Services services;
    private final MiningService miningService;

    public MiningBlockReplaceTask(Services services) {
        this.services = services;
        this.miningService = services.getMiningService();
    }

    @Override
    public void run() {

        if (miningService.getBrokenBlocks().isEmpty()) {
            return;
        }

        final long currentTime = System.currentTimeMillis();
        List<BrokenBlockWrapper> toClear = new ArrayList<>();
        for(BrokenBlockWrapper brokenBlockWrapper : miningService.getBrokenBlocks()) {
            final long breakTime = brokenBlockWrapper.getBreakTime();
            final long respawnTime = brokenBlockWrapper.getRespawnTime();
            if(currentTime - breakTime >= respawnTime) {
                toClear.add(brokenBlockWrapper);
            }
        }

        Bukkit.getScheduler().runTask(MiningPlugin.INSTANCE, () -> {
            miningService.getBrokenBlocks().removeAll(toClear);
            for(BrokenBlockWrapper brokenBlockWrapper : toClear) {
               brokenBlockWrapper.getBlockLocation().getBlock().setType(brokenBlockWrapper.getMaterial(), false);
            }
        });

    }
}
