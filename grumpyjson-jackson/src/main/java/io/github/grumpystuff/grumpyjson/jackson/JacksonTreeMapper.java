package io.github.grumpystuff.grumpyjson.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializationException;
import io.github.grumpystuff.grumpyjson.json_model.*;
import io.github.grumpystuff.grumpyjson.util.Parameters;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class JacksonTreeMapper {

    // prevent instantiation
    private JacksonTreeMapper() {
    }

    static JsonElement mapFromJackson(JsonNode treeNode) throws JsonDeserializationException {
        Parameters.notNull(treeNode, "gsonElement");

        return switch (treeNode.getNodeType()) {
            case NULL -> JsonNull.INSTANCE;
            case BOOLEAN -> JsonBoolean.of(treeNode.booleanValue());
            case NUMBER -> JsonNumber.of(treeNode.numberValue());
            case STRING -> JsonString.of(treeNode.textValue());
            case ARRAY -> {
                List<JsonElement> outputChildren = new ArrayList<>();
                for (JsonNode inputChild : treeNode) {
                    outputChildren.add(mapFromJackson(inputChild));
                }
                yield JsonArray.of(outputChildren);
            }
            case OBJECT -> {
                Iterable<Map.Entry<String, JsonNode>> inputFields = treeNode::fields;
                Map<String, JsonElement> outputMap = new HashMap<>();
                for (Map.Entry<String, JsonNode> inputField : inputFields) {
                    outputMap.put(inputField.getKey(), mapFromJackson(inputField.getValue()));
                }
                yield JsonObject.of(outputMap);
            }
            default -> throw new JsonDeserializationException("unknown node type: " + treeNode.getNodeType());
        };
    }

    static JsonNode mapToJackson(JsonElement jsonElement) {
        Parameters.notNull(jsonElement, "jsonElement");

        if (jsonElement instanceof JsonNull) {
            return NullNode.instance;
        } else if (jsonElement instanceof JsonBoolean b) {
            return BooleanNode.valueOf(b.getValue());
        } else if (jsonElement instanceof JsonNumber n) {
            return createNumericNode(n.getValue());
        } else if (jsonElement instanceof JsonString s) {
            return TextNode.valueOf(s.getValue());
        } else if (jsonElement instanceof JsonArray a) {
            ArrayNode outputArray = new ArrayNode(JsonNodeFactory.instance);
            for (JsonElement child : a.getAsList()) {
                outputArray.add(mapToJackson(child));
            }
            return outputArray;
        } else if (jsonElement instanceof JsonObject o) {
            ObjectNode outputObject = new ObjectNode(JsonNodeFactory.instance);
            for (Map.Entry<String, JsonElement> entry : o.getAsMap().entrySet()) {
                outputObject.set(entry.getKey(), mapToJackson(entry.getValue()));
            }
            return outputObject;
        } else {
            throw new IllegalArgumentException("unknown element type: " + jsonElement);
        }
    }

    private static NumericNode createNumericNode(Number value) {
        if (value instanceof BigDecimal d) {
            return DecimalNode.valueOf(d);
        } else if (value instanceof BigInteger i) {
            return BigIntegerNode.valueOf(i);
        }
        double doubleValue = value.doubleValue();
        long longValue = value.longValue();
        return (doubleValue == longValue) ? LongNode.valueOf(longValue) : DoubleNode.valueOf(doubleValue);
    }

}
