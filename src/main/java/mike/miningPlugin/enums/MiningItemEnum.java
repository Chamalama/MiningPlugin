package mike.miningPlugin.enums;

import lombok.Getter;
import mike.mLib.util.Item;
import mike.mLib.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
public enum MiningItemEnum {

    COAL_ORE(Item.build(Material.COAL_ORE, StringUtil.fromString("<dark_gray><b>Coal Ore"), List.of())),
    COAL_BLOCK(Item.build(Material.COAL_BLOCK, StringUtil.fromString("<dark_gray><b>Coal Block"), List.of())),
    IRON_ORE(Item.build(Material.IRON_ORE, StringUtil.fromString("<b><color:#ff984f>Iron Ore</color></b>>"), List.of())),
    IRON_BLOCK(Item.build(Material.IRON_BLOCK, StringUtil.fromString("<b><color:#ff984f>Iron Block</color></b>"), List.of())),
    LAPIS_ORE(Item.build(Material.LAPIS_ORE, StringUtil.fromString("<color:#403dff><b>Lapis Ore</b></color>"), List.of())),
    LAPIS_BLOCK(Item.build(Material.LAPIS_BLOCK, StringUtil.fromString("<color:#403dff><b>Lapis Block</b></color>"), List.of())),
    REDSTONE_ORE(Item.build(Material.REDSTONE_ORE, StringUtil.fromString("<dark_red><b>Redstone Ore"), List.of())),
    REDSTONE_BLOCK(Item.build(Material.REDSTONE_BLOCK, StringUtil.fromString("<dark_red><b>Redstone Block"), List.of())),
    GOLD_ORE(Item.build(Material.GOLD_ORE, StringUtil.fromString("<yellow><b>Gold Ore"), List.of())),
    GOLD_BLOCK(Item.build(Material.GOLD_BLOCK, StringUtil.fromString("<yellow><b>Gold Block"), List.of())),
    DIAMOND_ORE(Item.build(Material.DIAMOND_ORE, StringUtil.fromString("<aqua><b>Diamond Ore"), List.of())),
    DIAMOND_BLOCK(Item.build(Material.DIAMOND_BLOCK, StringUtil.fromString("<aqua><b>Diamond Block"), List.of())),
    EMERALD_ORE(Item.build(Material.EMERALD, StringUtil.fromString("<green><b>Emerald Ore"), List.of())),
    EMERALD_BLOCK(Item.build(Material.EMERALD, StringUtil.fromString("<green><b>Emerald Block"), List.of()))
    ;

    private final ItemStack miningItem;

    MiningItemEnum(ItemStack miningItem) {
        this.miningItem = miningItem;
    }

}
