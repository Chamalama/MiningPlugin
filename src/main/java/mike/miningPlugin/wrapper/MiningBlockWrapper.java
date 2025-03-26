package mike.miningPlugin.wrapper;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Getter
@Setter
public class  MiningBlockWrapper {

    private BlockPos blockPos;
    private int id;
    private int stage;
    private boolean isInteracted;
    private long timeToMine;
    private Material material;
    private Set<String> effectsApplied;

    public MiningBlockWrapper(BlockPos blockPos, int stage, long timeToMine, Material material) {
        this.blockPos = blockPos;
        this.id = new Random().nextInt(Integer.MAX_VALUE) + 1;
        this.stage = stage;
        this.isInteracted = false;
        this.timeToMine = timeToMine;
        this.material = material;
        this.effectsApplied = new HashSet<>();
    }

    public boolean hasEffect(String effect) {
        return effectsApplied.contains(effect);
    }

    public void applyEffect(String effectName, boolean applied) {
        if(applied) {
            effectsApplied.add(effectName);
        }else{
            effectsApplied.remove(effectName);
        }
    }

}
