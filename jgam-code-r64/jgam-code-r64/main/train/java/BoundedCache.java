

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class BoundedCache<K,V> extends LinkedHashMap<K, V> {
    private final int maxEntries;

    public BoundedCache(final int maxEntries) {
        super(maxEntries + 1, 1.0f);
        this.maxEntries = maxEntries;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return super.size() > maxEntries;
    }

}
