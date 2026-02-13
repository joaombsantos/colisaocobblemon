package me.marcronte.colisaocobblemon.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class SkinUrlHelper {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String MINESKIN_API = "https://api.mineskin.org/generate/url";

    public static void applySkinFromUrlAsync(Entity npc, String url) {

        CompletableFuture.runAsync(() -> {
            try {
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("url", url);
                jsonBody.addProperty("visibility", 0);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(MINESKIN_API))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    if (npc.getServer() != null) {
                        npc.getServer().execute(() -> applySkinFromNick(npc, "Steve"));
                    }
                    return;
                }

                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                if (!json.has("data")) {
                    return;
                }

                JsonObject data = json.getAsJsonObject("data");
                String textureUrl = data.getAsJsonObject("texture").get("url").getAsString();

                boolean isSlim = false;
                if (data.has("model")) {
                    String modelStr = data.get("model").getAsString();
                    isSlim = "slim".equalsIgnoreCase(modelStr);
                }

                if (npc.getServer() != null) {
                    boolean finalIsSlim = isSlim;
                    npc.getServer().execute(() -> callNativeLoadTexture(npc, textureUrl, finalIsSlim));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void callNativeLoadTexture(Entity npc, String cleanUrl, boolean isSlim) {
        try {
            URI textureUri = URI.create(cleanUrl);

            Class<?> enumClass = Class.forName("com.cobblemon.mod.common.entity.npc.NPCPlayerModelType");
            Object modelType = null;
            Object[] constants = enumClass.getEnumConstants();

            for (Object obj : constants) {
                String name = obj.toString().toUpperCase();
                if (isSlim) {
                    if (name.equals("SLIM") || name.equals("ALEX")) {
                        modelType = obj; break;
                    }
                } else {
                    if (name.equals("DEFAULT") || name.equals("STEVE") || name.equals("WIDE")) {
                        modelType = obj; break;
                    }
                }
            }
            if (modelType == null) modelType = constants[0];

            Method loadMethod = npc.getClass().getMethod("loadTexture", URI.class, enumClass);
            loadMethod.invoke(npc, textureUri, modelType);

            npc.setInvisible(true);
            npc.setInvisible(false);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void applySkinFromNick(Entity npc, String nick) {
        try {
            Method method = npc.getClass().getMethod("loadTextureFromGameProfileName", String.class);
            method.invoke(npc, nick);
            npc.setInvisible(true);
            npc.setInvisible(false);
        } catch (Exception e) {
            try {
                net.minecraft.nbt.CompoundTag nbt = new net.minecraft.nbt.CompoundTag();
                npc.saveWithoutId(nbt);
                nbt.putString("Texture", "player:" + nick);
                npc.load(nbt);
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}