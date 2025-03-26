package mike.miningPlugin.service;

import lombok.Getter;
import mike.miningPlugin.storage.BlockAmountStorage;
import mike.miningPlugin.storage.BlockPriceStorage;
import mike.miningPlugin.storage.BlockStorage;
import org.bukkit.plugin.Plugin;

@Getter
public class Services {

    private Plugin plugin;
    private final MiningService miningService;
    private final MiningAnimationService miningAnimationService;


    private final BlockStorage blockStorage;
    private final BlockAmountStorage blockAmountStorage;
    private final BlockPriceStorage blockPriceStorage;

    public Services(Plugin plugin) {
        this.plugin = plugin;
        this.blockStorage = new BlockStorage();
        this.blockAmountStorage = new BlockAmountStorage();
        this.blockPriceStorage = new BlockPriceStorage();
        this.miningAnimationService = new MiningAnimationService(this);
        this.miningService = new MiningService(this);
    }

}
