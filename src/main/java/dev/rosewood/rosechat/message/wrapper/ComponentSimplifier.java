package dev.rosewood.rosechat.message.wrapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;

public class ComponentSimplifier {

    private ComponentSimplifier() {

    }

    public static BaseComponent[] simplify(BaseComponent[] components) {
        components = removeUselessBits(components);
        components = compress(components);
        return components;
    }

    private static BaseComponent[] removeUselessBits(BaseComponent[] components) {
        for (BaseComponent component : components) {
            if (component.getFont() != null && component.getFont().equalsIgnoreCase("default")) component.setFont(null);
            if (!component.isBold()) component.setBold(null);
            if (!component.isItalic()) component.setItalic(null);
            if (!component.isUnderlined()) component.setUnderlined(null);
            if (!component.isStrikethrough()) component.setStrikethrough(null);
            if (!component.isObfuscated()) component.setObfuscated(null);
        }

        return components;
    }

    private static BaseComponent[] compress(BaseComponent[] components) {
        String json = ComponentSerializer.toString(components);
        JsonObject jsonObject = ComponentColorizer.JSON_PARSER.parse(json).getAsJsonObject();

        if (jsonObject.has("extra")) {
            JsonArray extraArray = jsonObject.getAsJsonArray("extra");
            extraArray = compressText(extraArray);
            jsonObject.add("extra", extraArray);
        }

        String finalJson = jsonObject.toString();
        return ComponentSerializer.parse(finalJson);
    }

    private static JsonArray compressText(JsonArray textArray) {
        JsonArray compressedArray = new JsonArray();
        JsonObject previous = null;
        int index = 0;
        for (JsonElement textElement : textArray) {
            JsonObject textObject = textElement.getAsJsonObject();

            if (previous != null && previous.has("text") && textObject.has("text")) {
                if (isSimilar(previous, textObject)) {
                    String text = textObject.get("text").getAsString();
                    String previousText = previous.get("text").getAsString();
                    previous.addProperty("text", previousText + text);

                    if (textObject.has("extra")) {
                        JsonArray extra = textObject.getAsJsonArray("extra");
                        JsonArray previousExtra = previous.get("extra").getAsJsonArray();
                        previousExtra.addAll(extra);
                    }
                } else {
                    compressedArray.add(previous);
                    previous = textObject;
                }
            } else {
                previous = textObject;
            }

            index++;
            if (index == textArray.size()) {
                 if (textObject.has("color")) previous.addProperty("color", textObject.get("color").getAsString());
                compressedArray.add(previous);
            }
        }
        return compressedArray;
    }

    private static boolean isSimilar(JsonObject one, JsonObject two) {
        String colorOne = one.has("color") ? one.get("color").getAsString() : null;
        String colorTwo = two.has("color") ? two.get("color").getAsString() : null;
        boolean shareColor = (colorOne == null && colorTwo == null) || ((colorOne != null && colorTwo != null) && colorOne.equals(colorTwo));
        String hoverOne = one.has("hoverEvent") ? one.get("hoverEvent").toString() : null;
        String hoverTwo = two.has("hoverEvent") ? two.get("hoverEvent").toString() : null;
        boolean shareHover = (hoverOne == null && hoverTwo == null) || ((hoverOne != null && hoverTwo != null) && hoverOne.equals(hoverTwo));
        String clickOne = one.has("clickEvent") ? one.get("clickEvent").toString() : null;
        String clickTwo = two.has("clickEvent") ? two.get("clickEvent").toString() : null;
        boolean shareClick = (clickOne == null && clickTwo == null) || ((clickOne != null && clickTwo != null) && clickOne.equals(clickTwo));
        boolean shareBold = (one.has("bold") && one.get("bold").getAsBoolean()) == (two.has("bold") && two.get("bold").getAsBoolean());
        boolean shareItalic = (one.has("italic") && one.get("italic").getAsBoolean()) == (two.has("italic") && two.get("italic").getAsBoolean());
        boolean shareUnderlined = (one.has("underlined") && one.get("underlined").getAsBoolean()) == (two.has("underlined") && two.get("underlined").getAsBoolean());
        boolean shareStrikethrough = (one.has("strikethrough") && one.get("strikethrough").getAsBoolean()) == (two.has("strikethrough") && two.get("strikethrough").getAsBoolean());
        boolean shareObfuscated = (one.has("obfuscated") && one.get("obfuscated").getAsBoolean()) == (two.has("obfuscated") && two.get("obfuscated").getAsBoolean());
        return shareColor && shareHover && shareClick && shareBold && shareItalic && shareUnderlined && shareStrikethrough && shareObfuscated;
    }
}
