package com.example.addon.modules;

import com.example.addon.CinnaUtil;
import com.example.addon.gui.DupeSequencesScreen;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.modules.Module;
import com.example.addon.systems.DupeSystem;
import net.minecraft.client.MinecraftClient;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;


public class DupeSequencesModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Keybind> stopKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("stop-key")
        .description("Key to stop any running sequence.")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_ESCAPE))
        .build()
    );

    private final Setting<Boolean> showStopMessage = sgGeneral.add(new BoolSetting.Builder()
        .name("show-stop-message")
        .description("Shows a message when stopping a sequence.")
        .defaultValue(true)
        .build()
    );

    public DupeSequencesModule() {
        super(CinnaUtil.CATEGORY, "Dupe Sequences", "Manage dupe sequences.");
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (!isActive()) return;

        if (stopKey.get().matches(true, event.key, 0)) {
            if (DupeSystem.isRunningSequence) {
                DupeSystem.stopCurrentSequence();
                if (showStopMessage.get()) {
                    info("Stopped running sequence");
                }
            }
        }
    }


    @Override
    public WWidget getWidget(GuiTheme theme) {
        WTable table = theme.table();

        WButton openBtn = table.add(theme.button("Open Dupe Sequences")).expandX().widget();
        openBtn.action = () -> MinecraftClient.getInstance().setScreen(new DupeSequencesScreen(theme));

        if (DupeSystem.isRunningSequence) {
            WButton stopBtn = table.add(theme.button("Stop Sequence")).expandX().widget();
            stopBtn.action = () -> {
                DupeSystem.stopCurrentSequence();
                if (showStopMessage.get()) {
                    info("Stopped running sequence");
                }
            };
        }

        return table;
    }
}
