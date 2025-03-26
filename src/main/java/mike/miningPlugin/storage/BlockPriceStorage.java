package mike.miningPlugin.storage;

import lombok.Getter;
import mike.mLib.config.DE;
import mike.mLib.config.JsonConfig;
import mike.miningPlugin.MiningPlugin;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BlockPriceStorage extends JsonConfig {

    public Map<String, Double> cachedPrices = new HashMap<>();

    public BlockPriceStorage() {
        super(MiningPlugin.INSTANCE, "block-prices");
        if(getFileData() == null) {
            writeObjectsToFile(dataMap(
                    DE.of("coal-ore", 1),
                    DE.of("coal-block", 1),
                    DE.of("iron-ore", 1),
                    DE.of("iron-block", 1),
                    DE.of("lapis-ore", 1),
                    DE.of("lapis-block", 1),
                    DE.of("redstone-ore", 1),
                    DE.of("redstone-block", 1),
                    DE.of("gold-ore", 1),
                    DE.of("gold-block", 1),
                    DE.of("diamond-ore", 1),
                    DE.of("diamond-block", 1),
                    DE.of("emerald-ore", 1),
                    DE.of("emerald-block", 1)
            ));
        }
        updatedPrices();
    }

    public void updatedPrices() {
        for(Map.Entry<Object, Object> entries : dataMap().entrySet()) {
            String key = (String) entries.getKey();
            double value = (double) entries.getValue();
            cachedPrices.put(key, value);
        }
    }

}
