package name.martingeisse.grumpyjson;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class JsonRegistry {

    // This list is not thread-safe, but adding type adapters after starting to serve requests would mess up
    // things anyway.
    private final List<JsonTypeAdapter<?>> adapterList = new ArrayList<>();
    private final ConcurrentMap<TypeToken<?>, JsonTypeAdapter<?>> adapterMap = new ConcurrentHashMap<>();

    // ----------------------------------------------------------------------------------------------------------------
    // configuration-time methods
    // ----------------------------------------------------------------------------------------------------------------

    public void clearTypeAdapters() {
        adapterList.clear();
    }

    public <T> void addTypeAdapter(JsonTypeAdapter<T> adapter) {
        adapterList.add(Objects.requireNonNull(adapter, "adapter"));
    }

    // ----------------------------------------------------------------------------------------------------------------
    // run-time methods
    // ----------------------------------------------------------------------------------------------------------------

    public boolean supportsType(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        return supportsType(TypeToken.get(clazz));
    }

    public boolean supportsType(TypeToken<?> type) {
        Objects.requireNonNull(type, "type");
        if (supportsAdapterAutoGeneration(type)) {
            return true;
        }
        if (adapterMap.containsKey(type)) {
            return true;
        }
        for (var adapter : adapterList) {
            if (adapter.supportsType(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean supportsAdapterAutoGeneration(TypeToken<?> type) {
        Objects.requireNonNull(type, "type");
        var rawType = type.getRawType();
        return (rawType.isRecord() && TypeToken.get(rawType).equals(type));
    }

    public <T> JsonTypeAdapter<T> getTypeAdapter(TypeToken<T> type) {
        Objects.requireNonNull(type, "type");

        // computeIfAbsent() cannot be used, if it behaves as it should, because recursively adding recognized types
        // would cause a ConcurrentModificationException. Note that thread safety is not a concern here because,
        // while two threads might *both* decide to create a missing adapter, we just end up with either one of them
        // and they should be equivalent.
        JsonTypeAdapter<?> adapter = adapterMap.get(type);

        // check if one of the registered adapters supports this type
        if (adapter == null) {
            for (JsonTypeAdapter<?> adapterFromList : adapterList) {
                if (adapterFromList.supportsType(type)) {
                    adapter = adapterFromList;
                    adapterMap.put(type, adapter);
                    break;
                }
            }
        }

        // check if we can auto-generate an adapter
        if (adapter == null && supportsAdapterAutoGeneration(type)) {

            // Next, install a proxy, so that recursive types don't crash the registry. Note that we don't put the
            // adapter/proxy into the adapterList because we already put it into the adapterMap, and it cannot handle
            // any types other than the exact type it gets generated for.
            var proxy = new AdapterProxy<T>();
            adapterMap.put(type, proxy);

            // finally, create the actual adapter and set it as the proxy's target
            adapter = new RecordAdapter<>(type.getRawType(), this);
            //noinspection unchecked
            proxy.setTarget((JsonTypeAdapter<T>)adapter);

        }

        // first, check if we can auto-generate an adapter for this type at all
        if (adapter == null) {
            throw new RuntimeException("no JSON type adapter found and can only auto-generate them for non-generic record types, found type: " + type);
        }

        //noinspection unchecked
        return (JsonTypeAdapter<T>)adapter;
    }

}
