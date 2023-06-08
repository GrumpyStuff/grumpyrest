package name.martingeisse.grumpyjson;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonEngineTest {

    private final JsonEngine engine = new JsonEngine();

    @Test
    public void testNullToJson() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> engine.stringify(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> engine.stringify(null, String.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> engine.stringify(null, new TypeToken<String>() {}));
    }
}
