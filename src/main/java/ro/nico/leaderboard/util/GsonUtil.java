package ro.nico.leaderboard.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GsonUtil {
    private static final Gson gson = new Gson();

    public static String convertMapToJson(Map<String, String> map) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            jsonObject.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
        }
        return gson.toJson(jsonObject);
    }

    public static Map<String, String> convertJsonToMap(String json) {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (entry.getValue().isJsonNull())
                continue;
            if (!entry.getValue().isJsonPrimitive())
                continue;
            JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();
            if (!primitive.isString())
                continue;
            map.put(entry.getKey(), primitive.getAsString());
        }
        return map;
    }

    public static String toBase64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes());
    }

    public static String fromBase64(String base64) {
        return new String(Base64.getDecoder().decode(base64));
    }
}
