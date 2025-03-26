package mike.miningPlugin.wrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;

@Getter
@Setter
@AllArgsConstructor
public class BrokenBlockWrapper {

    private Location blockLocation;
    private Material material;
    private long breakTime;
    private long respawnTime;

}
