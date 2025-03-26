package mike.miningPlugin.storage;

import lombok.Getter;
import mike.mLib.config.DE;
import mike.mLib.config.JsonConfig;
import mike.miningPlugin.MiningPlugin;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BlockAmountStorage extends JsonConfig {

    private Map<String, Double> cachedBlockAmounts = new HashMap<>();

    public BlockAmountStorage() {
        super(MiningPlugin.INSTANCE, "block-amounts");
        if(getFileData() == null) {
            writeObjectsToFile(dataMap(
                    DE.of("coal-min", 1),
                    DE.of("coal-max", 3),
                    DE.of("iron-min", 2),
                    DE.of("iron-max", 4),
                    DE.of("lapis-min", 3),
                    DE.of("lapis-max", 5),
                    DE.of("redstone-min", 3),
                    DE.of("redstone-max", 5),
                    DE.of("gold-min", 3),
                    DE.of("gold-max", 5),
                    DE.of("diamond-min", 3),
                    DE.of("diamond-max", 5),
                    DE.of("emerald-min", 4),
                    DE.of("emerald-max", 6)
            ));
        }
        updateCache();
    }

    public void updateCache() {
        for(Map.Entry<Object, Object> entries : dataMap().entrySet()) {
            String key = (String) entries.getKey();
            double value = (double) entries.getValue();
            cachedBlockAmounts.put(key, value);
        }
    }

}
