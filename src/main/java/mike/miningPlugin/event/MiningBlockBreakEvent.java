package mike.miningPlugin.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import mike.miningPlugin.wrapper.MiningBlockWrapper;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class MiningBlockBreakEvent extends Event implements Cancellable {

    public static HandlerList HANDLER_LIST = new HandlerList();

    private final Player breakingPlayer;
    private final MiningBlockWrapper wrapper;
    private final Block block;

    public MiningBlockBreakEvent(Player breakingPlayer, MiningBlockWrapper wrapper, Block block) {
        super(true);
        this.breakingPlayer = breakingPlayer;
        this.wrapper = wrapper;
        this.block = block;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {

    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
