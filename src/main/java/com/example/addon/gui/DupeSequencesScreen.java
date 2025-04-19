package com.example.addon.gui;

import com.example.addon.systems.ActionType;
import com.example.addon.systems.DupeSequence;
import com.example.addon.systems.DupeSystem;
import com.example.addon.systems.SequenceAction;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import net.minecraft.client.MinecraftClient;

public class DupeSequencesScreen extends WindowScreen {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public DupeSequencesScreen(GuiTheme theme) {
        super(theme, "Dupe Sequences");
    }

    public void initWidgets() {
        WTable table = add(theme.table()).expandX().widget();

        // Add button to create new sequence
        WButton newBtn = table.add(theme.button("New Sequence")).expandX().widget();
        newBtn.action = () -> {
            DupeSequence sequence = new DupeSequence("New Sequence");
            DupeSystem.get().add(sequence);
            mc.setScreen(new EditDupeSequenceScreen(theme, sequence));
        };

        // Add stop button if a sequence is running
        if (DupeSystem.isRunningSequence) {
            WButton stopBtn = table.add(theme.button("Stop Sequence")).expandX().widget();
            stopBtn.action = () -> {
                DupeSystem.stopCurrentSequence();
                clear();
                initWidgets();
            };
        }

        table.row();

        // List all existing sequences
        fillTable(table);
    }

    private void fillTable(WTable table) {
        for (DupeSequence sequence : DupeSystem.get()) {
            table.add(theme.label(sequence.getName())).expandX();

            WButton editBtn = table.add(theme.button("Edit")).widget();
            editBtn.action = () -> mc.setScreen(new EditDupeSequenceScreen(theme, sequence));

            WButton runBtn = table.add(theme.button("Run")).widget();
            runBtn.action = () -> runSequence(sequence);

            WButton deleteBtn = table.add(theme.button("Delete")).widget();
            deleteBtn.action = () -> {
                DupeSystem.get().remove(sequence);
                clear();
                initWidgets();
            };

            table.row();
        }
    }

    private void runSequence(DupeSequence sequence) {
        // Stop any currently running sequence
        DupeSystem.stopCurrentSequence();

        // Start a new sequence
        DupeSystem.isRunningSequence = true;
        Thread sequenceThread = new Thread(() -> {
            try {
                for (int i = 0; i < sequence.getRepeatCount() && DupeSystem.isRunningSequence; i++) {
                    if (sequence.isAllAtOnce()) {
                        // Run all actions at once (skip wait actions)
                        for (SequenceAction action : sequence.getActions()) {
                            if (!DupeSystem.isRunningSequence) break;

                            if (action.getType() != ActionType.WAIT) {
                                executeAction(action);
                            }
                        }
                    } else {
                        // Run actions with delay, respecting wait actions
                        for (SequenceAction action : sequence.getActions()) {
                            if (!DupeSystem.isRunningSequence) break;

                            try {
                                if (action.getType() == ActionType.WAIT) {
                                    // For wait actions, just sleep for the specified time
                                    int waitTime = Integer.parseInt(action.getData());
                                    Thread.sleep(waitTime);
                                } else {
                                    // For other actions, execute them and then wait the sequence delay
                                    MinecraftClient.getInstance().execute(() -> executeAction(action));
                                    Thread.sleep(sequence.getDelayBetweenActions());
                                }
                            } catch (InterruptedException e) {
                                // Sequence was interrupted, just exit
                                break;
                            } catch (NumberFormatException e) {
                                // Invalid wait time, use default delay
                                try {
                                    Thread.sleep(sequence.getDelayBetweenActions());
                                } catch (InterruptedException ex) {
                                    break;
                                }
                            }
                        }
                    }

                    // Add a small delay between sequence repeats
                    if (DupeSystem.isRunningSequence && i < sequence.getRepeatCount() - 1) {
                        Thread.sleep(500);
                    }
                }
            } catch (InterruptedException e) {
                // Sequence was interrupted, just exit
            } finally {
                DupeSystem.isRunningSequence = false;
                DupeSystem.currentSequenceThread = null;
            }
        });

        DupeSystem.currentSequenceThread = sequenceThread;
        sequenceThread.start();
    }


    private void executeAction(SequenceAction action) {
        if (action.getType() == ActionType.COMMAND) {
            // Execute command
            if (mc.player != null) {
                String cmd = action.getData();
                if (cmd.startsWith("/")) cmd = cmd.substring(1);
                mc.player.networkHandler.sendCommand(cmd);
            }
        } else if (action.getType() == ActionType.PACKET) {
            // Send packet
            if (mc.player != null && mc.player.currentScreenHandler != null) {
                int slot = action.getSlot();

                // Repeat the packet action the specified number of times
                for (int i = 0; i < action.getRepeatCount(); i++) {
                    mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        slot,
                        0,  // Button (0 = left click)
                        action.getSlotActionType(),
                        mc.player
                    );
                }
            }
        } else if (action.getType() == ActionType.WAIT) {
            // For wait actions, we don't need to do anything in this method
            // The actual waiting is handled in the runSequence method
        }
    }
}
