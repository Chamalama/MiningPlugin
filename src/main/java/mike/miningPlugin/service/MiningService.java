package mike.miningPlugin.service;

import lombok.Getter;
import mike.miningPlugin.enums.MiningItemEnum;
import mike.miningPlugin.event.MiningBlockBreakEvent;
import mike.miningPlugin.event.MiningBlockDamageEvent;
import mike.miningPlugin.event.MiningBlockStartMineEvent;
import mike.miningPlugin.storage.BlockAmountStorage;
import mike.miningPlugin.storage.BlockStorage;
import mike.miningPlugin.wrapper.BrokenBlockWrapper;
import mike.miningPlugin.wrapper.MiningBlockWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.*;

@Getter
public class MiningService {

    public Map<UUID, Map<Long, MiningBlockWrapper>> playerToBlock = new ConcurrentHashMap<>(8, 0.9f, 1);

    public Map<UUID, MiningBlockWrapper> currentMiningBlock = new ConcurrentHashMap<>();

    public Map<UUID, Long> playerTime = new ConcurrentHashMap<>(8, 0.9f, 1);

    public List<BrokenBlockWrapper> brokenBlocks = new ArrayList<>();

    private final Services services;
    private final BlockStorage blockStorage;
    private final BlockAmountStorage blockAmountStorage;
    private final MiningAnimationService miningAnimationService;

    public MiningService(Services services) {
        this.services = services;
        this.blockStorage = services.getBlockStorage();
        this.blockAmountStorage = services.getBlockAmountStorage();
        this.miningAnimationService = services.getMiningAnimationService();
    }

    public void setCurrentBlock(Player player, Block block) {
        MiningBlockWrapper miningBlockWrapper = fromPos(player, posFromBlock(block));
        if(miningBlockWrapper == null) return;
        currentMiningBlock.put(player.getUniqueId(), miningBlockWrapper);
    }

    public void startMiningBlock(Player player, Block block) {
        final World world = player.getWorld();
        if (!world.getName().equalsIgnoreCase("CAVERNS")) return;
        if (!canMine(block)) return;
        playerTime.put(player.getUniqueId(), System.currentTimeMillis());
        final ItemStack stack = player.getInventory().getItem(EquipmentSlot.HAND);
        final BlockPos pos = posFromBlock(block);

        long startMiningTime = (getDefaultMiningTime(block.getType()) - getTimeReduction(stack.getType()));

        MiningBlockStartMineEvent miningBlockStartMineEvent = new MiningBlockStartMineEvent(player, startMiningTime, startMiningTime);
        Bukkit.getPluginManager().callEvent(miningBlockStartMineEvent);

        long adjustedMiningTime = miningBlockStartMineEvent.getModifiedTime();

        MiningBlockWrapper fromPos = fromPos(player, pos);

        if (fromPos == null) {

            MiningBlockWrapper wrapper = createMiningBlockWrapper(pos, block.getType(), adjustedMiningTime);

            wrapper.setInteracted(true);

            playerToBlock.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                    .putIfAbsent(pos.asLong(), wrapper);

        } else {

            fromPos.setTimeToMine(miningBlockStartMineEvent.getModifiedTime());

            fromPos.setInteracted(true);

            if (adjustedMiningTime <= 0) {
                fromPos.setStage(10);
            }
        }
    }

    private MiningBlockWrapper createMiningBlockWrapper(BlockPos pos, Material type, long adjustedTime) {
        if (adjustedTime > 0) {
            return new MiningBlockWrapper(pos, 0, adjustedTime, type);
        } else {
            return new MiningBlockWrapper(pos, 10, 0, type);
        }
    }

    public void stopMiningBlock(Player player, Block block) {
        if(!playerToBlock.containsKey(player.getUniqueId())) return;
        BlockPos pos = posFromBlock(block);
        if(pos == null) return;
        MiningBlockWrapper fromPos = fromPos(player, pos);
        if(fromPos == null) return;
        fromPos.getEffectsApplied().clear();
        Map<Long, MiningBlockWrapper> wrappers = playerToBlock.get(player.getUniqueId());
        if(wrappers == null) return;

        MiningBlockWrapper fromPlayer = fromPlayer(player);


        for(MiningBlockWrapper miningBlockWrapper : wrappers.values()) {
            if(fromPlayer != null) {
                if (miningBlockWrapper.getBlockPos().asLong() == fromPlayer.getBlockPos().asLong()) continue;
            }
            miningBlockWrapper.setInteracted(false);
        }


        wrappers.remove(pos.asLong());
        wrappers.put(pos.asLong(), fromPos);

    }

    public BlockPos posFromBlock(Block block) {
        Location loc = block.getLocation();
        return new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public MiningBlockWrapper fromPos(Player player, BlockPos pos) {
        if(!playerToBlock.containsKey(player.getUniqueId())) return null;
        Map<Long, MiningBlockWrapper> activeBlocks = playerToBlock.get(player.getUniqueId());
        if(activeBlocks == null || activeBlocks.isEmpty()) return null;
        return activeBlocks.get(pos.asLong());
    }

    public MiningBlockWrapper fromPlayer(Player player) {
        if(!currentMiningBlock.containsKey(player.getUniqueId())) return null;
        return currentMiningBlock.getOrDefault(player.getUniqueId(), null);
    }

    public Block fromBlockPos(World world, BlockPos pos) {
        Location location = new Location(world, pos.getX(), pos.getY(), pos.getZ());
        return location.getBlock();
    }

    public void incrementMiningBlock(Player player, Block block) {
        MiningBlockWrapper from = fromPos(player, posFromBlock(block));
        if(!canMine(block)) return;
        if(from == null) return;
        if(!from.isInteracted()) return;
        MiningBlockDamageEvent event = new MiningBlockDamageEvent(player, from);
        Bukkit.getPluginManager().callEvent(event);
        playerTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void tryBreakBlock(Player player, Block block) {
        MiningBlockWrapper from = fromPos(player, posFromBlock(block));
        if (from == null) return;
        sendResetPacket(player, from);
        from.getEffectsApplied().clear();
        MiningBlockBreakEvent event = new MiningBlockBreakEvent(player, from, block);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void sendPacket(Player player, Block block) {
        if(player == null) return;
        MiningBlockWrapper wrapper = fromPos(player, posFromBlock(block));
        final ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        final ServerGamePacketListenerImpl impl = serverPlayer.connection;

        int id = wrapper.getId();
        BlockPos pos = wrapper.getBlockPos();
        int stage = wrapper.getStage();

        ClientboundBlockDestructionPacket destructionPacket = new ClientboundBlockDestructionPacket(id, pos, stage);
        impl.send(destructionPacket);
    }


    public void sendResetPacket(Player player, MiningBlockWrapper wrapper) {
        if(player == null) return;
        final ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        final ServerGamePacketListenerImpl impl = serverPlayer.connection;

        int id = wrapper.getId();
        BlockPos pos = wrapper.getBlockPos();

        ClientboundBlockDestructionPacket destructionPacket = new ClientboundBlockDestructionPacket(id, pos, -1);
        impl.send(destructionPacket);
    }

    public long getDefaultMiningTime(Material material) {
        switch (material) {
            case COAL_ORE, COAL_BLOCK -> {
                return blockStorage.getUpdatedTimes().get("COAL");
            }
            case IRON_ORE, IRON_BLOCK -> {
                return blockStorage.getUpdatedTimes().get("IRON");
            }
            case LAPIS_ORE, LAPIS_BLOCK -> {
                return blockStorage.getUpdatedTimes().get("LAPIS");
            }
            case REDSTONE_ORE, REDSTONE_BLOCK -> {
                return blockStorage.getUpdatedTimes().get("REDSTONE");
            }
            case GOLD_ORE, GOLD_BLOCK -> {
                return blockStorage.getUpdatedTimes().get("GOLD");
            }
            case DIAMOND_ORE, DIAMOND_BLOCK -> {
                return blockStorage.getUpdatedTimes().get("DIAMOND");
            }
            case EMERALD_ORE, EMERALD_BLOCK -> {
                return blockStorage.getUpdatedTimes().get("EMERALD");
            }
            default -> {
                return 1005000L;
            }
        }
    }

    public long getTimeReduction(Material material) {
        switch (material) {
            case WOODEN_PICKAXE -> {
                return 100L;
            }
            case STONE_PICKAXE -> {
                return 200L;
            }
            case GOLDEN_PICKAXE -> {
                return 300L;
            }
            case IRON_PICKAXE -> {
                return 500L;
            }
            case DIAMOND_PICKAXE -> {
                return 800L;
            }
            default -> {
                return 0L;
            }
        }
    }

    public int getBlockAmount(String keyMin, String keyMax) {
        double min = blockAmountStorage.getCachedBlockAmounts().get(keyMin);
        double max = blockAmountStorage.getCachedBlockAmounts().get(keyMax);
        return new Random().nextInt((int)min, (int)max);
    }

    public MiningItemEnum getItem(Material material) {
        switch (material) {
            case COAL_ORE -> {
                return MiningItemEnum.COAL_ORE;
            }
            case COAL_BLOCK -> {
                return MiningItemEnum.COAL_BLOCK;
            }
            case IRON_ORE -> {
                return MiningItemEnum.IRON_ORE;
            }
            case IRON_BLOCK -> {
                return MiningItemEnum.IRON_BLOCK;
            }
            case LAPIS_ORE -> {
                return MiningItemEnum.LAPIS_ORE;
            }
            case LAPIS_BLOCK -> {
                return MiningItemEnum.LAPIS_BLOCK;
            }
            case REDSTONE_ORE -> {
                return MiningItemEnum.REDSTONE_ORE;
            }
            case REDSTONE_BLOCK -> {
                return MiningItemEnum.REDSTONE_BLOCK;
            }
            case GOLD_ORE -> {
                return MiningItemEnum.GOLD_ORE;
            }
            case GOLD_BLOCK -> {
                return MiningItemEnum.GOLD_BLOCK;
            }
            case DIAMOND_ORE -> {
                return MiningItemEnum.DIAMOND_ORE;
            }
            case DIAMOND_BLOCK -> {
                return MiningItemEnum.DIAMOND_BLOCK;
            }
            case EMERALD_ORE -> {
                return MiningItemEnum.EMERALD_ORE;
            }
            case EMERALD_BLOCK -> {
                return MiningItemEnum.EMERALD_BLOCK;
            }
        }
        return null;
    }

    public String[] getItemAmountKey(Material material) {
        switch (material) {
            case COAL_ORE, COAL_BLOCK -> {
                return new String[]{"coal-min", "coal-max"};
            }
            case IRON_ORE, IRON_BLOCK -> {
                return new String[]{"iron-min", "iron-max"};
            }
            case LAPIS_ORE, LAPIS_BLOCK -> {
                return new String[]{"lapis-min", "lapis-max"};
            }
            case REDSTONE_ORE, REDSTONE_BLOCK -> {
                return new String[]{"redstone-min", "redstone-max"};
            }
            case GOLD_ORE, GOLD_BLOCK -> {
                return new String[]{"gold-min", "gold-max"};
            }
            case DIAMOND_ORE, DIAMOND_BLOCK -> {
                return new String[]{"diamond-min", "diamond-max"};
            }
            case EMERALD_ORE, EMERALD_BLOCK -> {
                return new String[]{"emerald-min", "emerald-max"};
            }
        }
        return null;
    }

    public boolean canMine(Block block) {
        return block.getType() != Material.STONE;
    }

    public void addBrokenBlock(BrokenBlockWrapper brokenBlockWrapper) {
        brokenBlocks.add(brokenBlockWrapper);
    }


}
