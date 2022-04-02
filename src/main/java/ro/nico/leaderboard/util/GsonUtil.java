package ro.nico.leaderboard.util;

import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GsonUtil {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static <T> T fromOrToJson(T object, Class<T> clazz, File file) throws IOException {
        if (file.exists()) {
            return load(clazz, file);
        } else {
            save(object, file);
            return object;
        }
    }

    public static <T> T load(Class<T> clazz, File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            try (InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                try (BufferedReader br = new BufferedReader(isr)) {
                    return gson.fromJson(br, clazz);
                }
            }
        }
    }

    public static <T> void save(T object, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            try (OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                try (BufferedWriter bw = new BufferedWriter(osw)) {
                    gson.toJson(object, bw);
                }
            }
        }
    }

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
