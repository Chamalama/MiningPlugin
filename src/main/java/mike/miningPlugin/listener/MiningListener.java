package mike.miningPlugin.listener;

import lombok.Getter;
import mike.miningPlugin.MiningPlugin;
import mike.miningPlugin.enums.MiningItemEnum;
import mike.miningPlugin.event.MiningBlockBreakEvent;
import mike.miningPlugin.event.MiningBlockDamageEvent;
import mike.miningPlugin.service.MiningAnimationService;
import mike.miningPlugin.service.MiningService;
import mike.miningPlugin.wrapper.BrokenBlockWrapper;
import mike.miningPlugin.wrapper.MiningBlockWrapper;
import net.minecraft.core.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.Future;

@Getter
public class MiningListener implements Listener {

    private final MiningService miningService;
    private final MiningAnimationService miningAnimationService;
    private final MiningPlugin plugin;

    public MiningListener(MiningPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.miningService = plugin.getServices().getMiningService();
        this.miningAnimationService = plugin.getServices().getMiningAnimationService();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(!event.getPlayer().getWorld().getName().equalsIgnoreCase("Caverns")) {
            event.getPlayer().getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(1.0);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        World world = event.getTo().getWorld();
        if(world.getName().equalsIgnoreCase("Caverns")) {
            event.getPlayer().getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0.0);
        }else{
            event.getPlayer().getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(1.0);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if(!player.getWorld().getName().equalsIgnoreCase("Caverns")) return;

        MiningPlugin.INSTANCE.getCachedThread().submit(() -> {
            miningService.startMiningBlock(player, block);
            miningService.setCurrentBlock(player, block);
        });

    }

    @EventHandler
    public void onBlockStop(BlockDamageAbortEvent event) {
        Block block = event.getBlock();
        final Player player = event.getPlayer();
        if(!player.getWorld().getName().equalsIgnoreCase("Caverns")) return;
        MiningBlockWrapper fromPlayer = miningService.fromPlayer(player);
        if(fromPlayer != null) {
            fromPlayer.setInteracted(false);
        }
        miningService.stopMiningBlock(event.getPlayer(), block);
    }

    @EventHandler
    public void onMiningDamage(MiningBlockDamageEvent event) {
        MiningBlockWrapper miningBlockWrapper = event.getWrapper();
        miningBlockWrapper.setStage(miningBlockWrapper.getStage() + 1);
        final Block block = miningService.fromBlockPos(event.getPlayer().getWorld(), miningBlockWrapper.getBlockPos());
        miningService.sendPacket(event.getPlayer(), block);
        if(miningBlockWrapper.getStage() >= 9) {
            miningService.tryBreakBlock(event.getPlayer(), block);
        }
    }

    @EventHandler
    public void onMiningBreak(MiningBlockBreakEvent event) {
        final Player player = event.getBreakingPlayer();
        player.playSound(player, Sound.BLOCK_STONE_BREAK, 1.0F, 1.0F);
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.5F);
        final MiningBlockWrapper wrapper = event.getWrapper();
        final BlockPos pos = wrapper.getBlockPos();
        final Block block = event.getBlock();

        //Generate the new mining item

        final MiningItemEnum miningItem = miningService.getItem(event.getWrapper().getMaterial());
        ItemStack stack = miningItem.getMiningItem();

        String[] keys = miningService.getItemAmountKey(event.getWrapper().getMaterial());

        stack.setAmount(miningService.getBlockAmount(keys[0], keys[1]));

        player.getInventory().addItem(stack);

        MiningPlugin.INSTANCE.getBlockExecutor().execute(() -> {
            BrokenBlockWrapper brokenBlockWrapper = new BrokenBlockWrapper(block.getLocation(), block.getType(), System.currentTimeMillis(), 5000);
            miningService.addBrokenBlock(brokenBlockWrapper);
        });


        if(!miningService.getPlayerToBlock().containsKey(player.getUniqueId())) return;
        miningService.stopMiningBlock(player, block);

        Future<?> breakFuture = MiningPlugin.INSTANCE.getExecutor().submit(() -> {
            miningService.getPlayerToBlock().computeIfPresent(player.getUniqueId(), ((uuid, map) -> {
                map.remove(pos.asLong());
                return map.isEmpty() ? null : map;
            }));
        });

        try {
            breakFuture.get();
        }catch (Exception e) {
            MiningPlugin.INSTANCE.getLogger().info("Error loading break future...");
        }

        Bukkit.getScheduler().runTask(MiningPlugin.INSTANCE, () -> {
            miningAnimationService.spawnDisplayItem(player, block.getLocation().toCenterLocation().add(0, 0.5, 0), block.getType());
            block.setType(Material.STONE, false);
        });

    }

}
