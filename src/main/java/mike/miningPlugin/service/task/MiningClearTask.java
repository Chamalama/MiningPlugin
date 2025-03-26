package mike.miningPlugin.service.task;

import lombok.Getter;
import mike.miningPlugin.service.MiningService;
import mike.miningPlugin.service.Services;
import mike.miningPlugin.wrapper.MiningBlockWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class MiningClearTask implements Runnable {

    private final Services services;
    private final MiningService miningService;

    public MiningClearTask(Services services) {
        this.services = services;
        this.miningService = services.getMiningService();
    }

    @Override
    public void run() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            List<Long> toClear = new ArrayList<>();
            if(!miningService.getPlayerToBlock().containsKey(player.getUniqueId())) continue;
            Map<Long, MiningBlockWrapper> wrapperMap = miningService.getPlayerToBlock().get(player.getUniqueId());
            for(MiningBlockWrapper vals : wrapperMap.values()) {
                if(vals.isInteracted()) continue;
                toClear.add(vals.getBlockPos().asLong());
                miningService.sendResetPacket(player, vals);
            }
            for(long clear : toClear) {
                miningService.getPlayerToBlock().get(player.getUniqueId()).remove(clear);
            }
        }
    }
}
