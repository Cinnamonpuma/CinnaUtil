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
        super(CinnaUtil.CATEGORY, "DupeSequences", "Manage dupe sequences.");
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        // Safety checks
        if (!isActive() || mc == null || mc.currentScreen != null) {
            return;
        }

        try {
            // Handle stop key for any running sequence
            if (handleStopKey(event)) {
                return; // Stop key was handled, don't process other keybinds
            }

            // Handle keybind for dupe sequences
            handleSequenceKeybinds(event);
            
        } catch (Exception e) {
            error("Error handling key event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean handleStopKey(KeyEvent event) {
        try {
            Keybind stopKeybind = stopKey.get();
            if (stopKeybind == null) {
                return false;
            }

            // Use proper keybind matching - check if this is the correct API for your Meteor version
            if (stopKeybind.isPressed()) {
                info("Stop key pressed.");
                if (DupeSystem.isRunningSequence) {
                    DupeSystem.stopCurrentSequence();
                    if (showStopMessage.get()) {
                        info("Stopped running sequence via stop-key");
                    }
                } else {
                    info("No sequence running to stop.");
                }
                return true;
            }
        } catch (Exception e) {
            error("Error handling stop key: " + e.getMessage());
        }
        return false;
    }

    private void handleSequenceKeybinds(KeyEvent event) {
        try {
            // Safely get the sequence collection
            if (DupeSystem.get() == null) {
                return;
            }

            for (com.example.addon.systems.DupeSequence sequence : DupeSystem.get()) {
                if (sequence == null) {
                    continue; // Skip null sequences
                }

                Keybind sequenceKeybind = sequence.getKeybind();
                if (sequenceKeybind == null) {
                    continue; // Skip sequences without keybinds
                }

                // Use proper keybind matching
                if (sequenceKeybind.isPressed()) {
                    info("Keybind pressed for sequence: " + sequence.getName());
                    
                    if (DupeSystem.isRunningSequence) {
                        handleRunningSequenceKeybind(sequence);
                    } else {
                        // No sequence running: start this one
                        startSequence(sequence);
                    }
                    return; // Process only the first matching sequence keybind
                }
            }
        } catch (Exception e) {
            error("Error handling sequence keybinds: " + e.getMessage());
        }
    }

    private void handleRunningSequenceKeybind(com.example.addon.systems.DupeSequence sequence) {
        try {
            com.example.addon.systems.DupeSequence currentSequence = DupeSystem.getCurrentSequence();
            
            if (currentSequence == sequence) {
                // Pressed keybind of currently running sequence: stop it
                DupeSystem.stopCurrentSequence();
                if (showStopMessage.get()) {
                    info("Stopped sequence: " + sequence.getName());
                }
            } else {
                // Pressed keybind of a different sequence: stop current, start new
                String currentName = currentSequence != null ? currentSequence.getName() : "Unknown";
                info("Stopping current sequence ('" + currentName + "') to start new sequence ('" + sequence.getName() + "').");
                
                DupeSystem.stopCurrentSequence();
                startSequence(sequence);
            }
        } catch (Exception e) {
            error("Error handling running sequence keybind: " + e.getMessage());
        }
    }

    private void startSequence(com.example.addon.systems.DupeSequence sequence) {
        try {
            if (sequence == null) {
                error("Cannot start null sequence");
                return;
            }

            int repeatCount = sequence.getRepeatCount();
            if (repeatCount < 0) {
                warning("Sequence '" + sequence.getName() + "' has invalid repeat count: " + repeatCount + ". Using 1 instead.");
                repeatCount = 1;
            }

            DupeSystem.executeSequence(sequence, repeatCount);
            info("Started sequence: " + sequence.getName());
        } catch (Exception e) {
            error("Failed to start sequence '" + sequence.getName() + "': " + e.getMessage());
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        try {
            WTable table = theme.table();

            // Always show the open button
            WButton openBtn = table.add(theme.button("Open Dupe Sequences")).expandX().widget();
            openBtn.action = () -> {
                try {
                    MinecraftClient.getInstance().setScreen(new DupeSequencesScreen(theme));
                } catch (Exception e) {
                    error("Failed to open Dupe Sequences screen: " + e.getMessage());
                }
            };

            // Conditionally show stop button if sequence is running
            if (DupeSystem.isRunningSequence) {
                WButton stopBtn = table.add(theme.button("Stop Sequence")).expandX().widget();
                stopBtn.action = () -> {
                    try {
                        DupeSystem.stopCurrentSequence();
                        if (showStopMessage.get()) {
                            info("Stopped running sequence");
                        }
                    } catch (Exception e) {
                        error("Failed to stop sequence: " + e.getMessage());
                    }
                };
            }

            return table;
        } catch (Exception e) {
            error("Error creating widget: " + e.getMessage());
            // Return a minimal widget in case of error
            return theme.table();
        }
    }
}