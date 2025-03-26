package mike.miningPlugin.storage;

import com.google.gson.internal.LinkedTreeMap;
import lombok.Getter;
import mike.mLib.config.DE;
import mike.mLib.config.JsonConfig;
import mike.miningPlugin.MiningPlugin;

import java.util.Map;

@Getter
public class BlockStorage extends JsonConfig {

    public LinkedTreeMap<String, Long> updatedTimes = new LinkedTreeMap<>();

    public BlockStorage() {
        super(MiningPlugin.INSTANCE, "blocks", "timings", false);
        if(getFileData() == null) {
            writeObjectsToFile(dataMap(
                    DE.of("COAL", 1000),
                    DE.of("IRON", 1100),
                    DE.of("LAPIS", 1200),
                    DE.of("REDSTONE", 1300),
                    DE.of("GOLD", 1400),
                    DE.of("DIAMOND", 1500),
                    DE.of("EMERALD", 1600)
            ));
        }
        updatedTimes();
    }

    public void updatedTimes() {
        if(getFileData() == null) return;
        LinkedTreeMap<Object, Object> times = (LinkedTreeMap) getFileData().get(0);
        if(times == null) return;
        for(Map.Entry<Object, Object> entry : times.entrySet()) {
            String key = (String) entry.getKey();
            double time = (double) entry.getValue();
            updatedTimes.put(key, (long)time);
        }
    }

    public void reload() {
        updatedTimes.clear();
        updatedTimes();
    }

}
