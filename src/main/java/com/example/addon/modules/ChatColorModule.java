package com.example.addon.modules;

import com.example.addon.CinnaUtil;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.awt.Color;
import java.util.regex.Pattern;

public class ChatColorModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFormatting = settings.createGroup("Formatting");

    private final Setting<String> startColor = sgGeneral.add(new StringSetting.Builder()
        .name("start-color")
        .description("Starting hex color (e.g. #ff0000)")
        .defaultValue("#B67C4F")
        .build()
    );

    private final Setting<String> endColor = sgGeneral.add(new StringSetting.Builder()
        .name("end-color")
        .description("Ending hex color (e.g. #0000ff)")
        .defaultValue("#9543BC")
        .build()
    );

    private final Setting<Integer> charsPerColor = sgGeneral.add(new IntSetting.Builder()
        .name("chars-per-color")
        .description("How many characters should share the same color")
        .defaultValue(1)
        .min(1)
        .max(10)
        .build()
    );

    private final Setting<Boolean> bold = sgFormatting.add(new BoolSetting.Builder()
        .name("bold")
        .description("Makes the text bold.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> italic = sgFormatting.add(new BoolSetting.Builder()
        .name("italic")
        .description("Makes the text italic.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> underline = sgFormatting.add(new BoolSetting.Builder()
        .name("underline")
        .description("Makes the text underlined.")
        .defaultValue(false)
        .build()
    );

    public ChatColorModule() {
        super(CinnaUtil.CATEGORY, "chat-colors", "Applies gradient colors to outgoing chat messages.");
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        String message = event.message;
        if (message.startsWith("/") || message.startsWith(".")) return;

        try {
            Color start = Color.decode(validateHex(startColor.get()));
            Color end = Color.decode(validateHex(endColor.get()));
            event.message = applyGradient(message, start, end);
        } catch (NumberFormatException e) {
            error("Invalid hex color format!");
        }
    }

    private String validateHex(String hex) {
        if (!hex.startsWith("#")) hex = "#" + hex;
        if (!Pattern.matches("#[0-9A-Fa-f]{6}", hex)) {
            throw new NumberFormatException("Invalid hex color: " + hex);
        }
        return hex;
    }

    private String applyGradient(String text, Color start, Color end) {
        StringBuilder result = new StringBuilder();
        int length = text.length();
        int colorSteps = (int) Math.ceil((double) length / charsPerColor.get());

        String formatting = getFormatting();

        for (int i = 0; i < length; i++) {
            if (i % charsPerColor.get() == 0) {
                float ratio = (float) (i / charsPerColor.get()) / (colorSteps - 1);
                Color current = interpolateColor(start, end, ratio);
                result.append("&#").append(String.format("%06X", current.getRGB() & 0xFFFFFF));
                if (!formatting.isEmpty()) result.append(formatting);
            }
            result.append(text.charAt(i));
        }

        return result.toString();
    }

    private String getFormatting() {
        StringBuilder format = new StringBuilder();
        if (bold.get()) format.append("&l");
        if (italic.get()) format.append("&o");
        if (underline.get()) format.append("&n");
        return format.toString();
    }

    private Color interpolateColor(Color start, Color end, float ratio) {
        int red = (int) (start.getRed() * (1 - ratio) + end.getRed() * ratio);
        int green = (int) (start.getGreen() * (1 - ratio) + end.getGreen() * ratio);
        int blue = (int) (start.getBlue() * (1 - ratio) + end.getBlue() * ratio);
        return new Color(red, green, blue);
    }
}
