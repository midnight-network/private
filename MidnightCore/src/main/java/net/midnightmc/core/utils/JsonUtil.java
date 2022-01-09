package net.midnightmc.core.utils;

import com.google.gson.JsonElement;
import net.midnightmc.core.api.MidnightAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public final class JsonUtil {

    public static <T> T getJsonArray(@NotNull JsonElement json, @Nullable String path, @NotNull Class<T> classOfT) {
        return MidnightAPI.getInstance().getGson().fromJson(getJsonElement(json, path), classOfT);
    }

    public static <T> T getJsonArray(@NotNull JsonElement json, @Nullable String path, @NotNull Type typeOfT) {
        return MidnightAPI.getInstance().getGson().fromJson(getJsonElement(json, path), typeOfT);
    }

    public static JsonElement getJsonElement(@Nullable JsonElement json, @Nullable String path) {
        if (json == null) {
            return null;
        }
        JsonElement element = json;
        if (path != null) {
            for (String p : path.split("\\.")) {
                element = element.getAsJsonObject().get(p);
            }
        }
        return element;
    }

}
