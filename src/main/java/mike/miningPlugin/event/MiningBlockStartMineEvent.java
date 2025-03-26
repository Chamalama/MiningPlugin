package mike.miningPlugin.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class MiningBlockStartMineEvent extends Event {

    public static HandlerList HANDLER_LIST = new HandlerList();

    private Player player;
    private long startTime;
    private long modifiedTime;

    public MiningBlockStartMineEvent(Player player, long startTime, long modifiedTime) {
        super(true);
        this.player = player;
        this.startTime = startTime;
        this.modifiedTime = modifiedTime;
    }


    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
