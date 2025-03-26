package mike.miningPlugin.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import mike.miningPlugin.wrapper.MiningBlockWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class MiningBlockDamageEvent extends Event implements Cancellable {

    public static HandlerList HANDLER_LIST = new HandlerList();

    private Player player;
    private MiningBlockWrapper wrapper;

    public MiningBlockDamageEvent(Player player, MiningBlockWrapper wrapper) {
        super(true);
        this.player = player;
        this.wrapper = wrapper;
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
