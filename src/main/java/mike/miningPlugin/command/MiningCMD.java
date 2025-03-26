package mike.miningPlugin.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import mike.mLib.util.MessageUtil;
import mike.miningPlugin.service.Services;
import mike.miningPlugin.storage.BlockAmountStorage;
import mike.miningPlugin.storage.BlockPriceStorage;
import mike.miningPlugin.storage.BlockStorage;
import org.bukkit.entity.Player;

@CommandPermission("admin.mining.cmd")
@CommandAlias("miningplugin|mp")
public class MiningCMD extends BaseCommand {

    private final Services services;
    private final BlockStorage blockStorage;
    private final BlockAmountStorage blockAmountStorage;
    private final BlockPriceStorage blockPriceStorage;

    public MiningCMD(Services services) {
        this.services = services;
        this.blockStorage = services.getBlockStorage();
        this.blockAmountStorage = services.getBlockAmountStorage();
        this.blockPriceStorage = services.getBlockPriceStorage();
    }

    @Subcommand("reload")
    public void onReload(Player sender) {
        blockStorage.reload();
        blockAmountStorage.updateCache();
        blockPriceStorage.updatedPrices();
        MessageUtil.sendNormalMessage(sender, "Reloaded mining configs!");
    }

}
