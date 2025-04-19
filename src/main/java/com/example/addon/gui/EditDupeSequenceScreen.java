package com.example.addon.gui;

import com.example.addon.systems.ActionType;
import com.example.addon.systems.DupeSequence;
import com.example.addon.systems.DupeSystem;
import com.example.addon.systems.SequenceAction;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.slot.SlotActionType;

public class EditDupeSequenceScreen extends WindowScreen {
    private final DupeSequence sequence;
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public EditDupeSequenceScreen(GuiTheme theme, DupeSequence sequence) {
        super(theme, "Edit Sequence");
        this.sequence = sequence;
    }

    @Override
    public void initWidgets() {
        // Main table
        WTable table = add(theme.table()).expandX().widget();

        // Name field
        table.add(theme.label("Name:"));
        var nameBox = table.add(theme.textBox(sequence.getName())).minWidth(400).expandX().widget();
        nameBox.action = () -> {
            sequence.setName(nameBox.get());
            DupeSystem.get().save();
        };
        table.row();

        // Delay field
        table.add(theme.label("Delay (ms):"));
        var delayEdit = table.add(theme.textBox(Integer.toString(sequence.getDelayBetweenActions()))).expandX().widget();
        delayEdit.action = () -> {
            try {
                int delay = Integer.parseInt(delayEdit.get());
                if (delay >= 0) {
                    sequence.setDelayBetweenActions(delay);
                    DupeSystem.get().save();
                }
            } catch (NumberFormatException e) {
                // Invalid number, do nothing
            }
        };
        table.row();

        // All at once checkbox - fixed layout
        table.add(theme.label("Execute all at once:"));
        var allAtOnceCheckbox = table.add(theme.checkbox(sequence.isAllAtOnce())).widget(); // Remove expandX()
        allAtOnceCheckbox.action = () -> {
            sequence.setAllAtOnce(allAtOnceCheckbox.checked);
            DupeSystem.get().save();
        };
        table.row();

        // Repeat count field
        table.add(theme.label("Repeat Count:"));
        var repeatCountEdit = table.add(theme.textBox(Integer.toString(sequence.getRepeatCount()))).expandX().widget();
        repeatCountEdit.action = () -> {
            try {
                int count = Integer.parseInt(repeatCountEdit.get());
                if (count > 0) {
                    sequence.setRepeatCount(count);
                    DupeSystem.get().save();
                }
            } catch (NumberFormatException e) {
                // Invalid number, do nothing
            }
        };
        table.row();

        // Actions section
        var actionsSection = table.add(theme.section("Actions")).expandX().widget();
        var actionsTable = actionsSection.add(theme.table()).expandX().widget();

        // List all actions
        for (int i = 0; i < sequence.getActions().size(); i++) {
            final int index = i;
            SequenceAction action = sequence.getActions().get(i);

            // Action row
            WTable actionRow = actionsTable.add(theme.table()).expandX().widget();

            // Action type indicator
            String actionLabel;
            if (action.getType() == ActionType.COMMAND) {
                actionLabel = "Command: " + action.getData();
            } else if (action.getType() == ActionType.PACKET) {
                actionLabel = "Packet: Slot " + action.getSlot() + (action.getRepeatCount() > 1 ? " (x" + action.getRepeatCount() + ")" : "");
            } else if (action.getType() == ActionType.WAIT) {
                actionLabel = "Wait: " + action.getData() + "ms";
            } else if (action.getType() == ActionType.CLOSE_GUI) {
                actionLabel = "Close GUI: " + (action.getData().equals("desync") ? "Desync" : "Normal");
            } else {
                actionLabel = "Unknown action type";
            }

            actionRow.add(theme.label(actionLabel)).expandX();

            // Edit button
            WButton editBtn = actionRow.add(theme.button("Edit")).widget();
            editBtn.action = () -> {
                mc.setScreen(new EditActionScreen(theme, sequence, action, this));
            };

            // Move up button
            if (i > 0) {
                WButton upBtn = actionRow.add(theme.button("up")).widget();
                upBtn.action = () -> {
                    SequenceAction temp = sequence.getActions().get(index);
                    sequence.getActions().set(index, sequence.getActions().get(index - 1));
                    sequence.getActions().set(index - 1, temp);
                    DupeSystem.get().save();
                    clear();
                    initWidgets();
                };
            }

            // Move down button
            if (i < sequence.getActions().size() - 1) {
                WButton downBtn = actionRow.add(theme.button("down")).widget();
                downBtn.action = () -> {
                    SequenceAction temp = sequence.getActions().get(index);
                    sequence.getActions().set(index, sequence.getActions().get(index + 1));
                    sequence.getActions().set(index + 1, temp);
                    DupeSystem.get().save();
                    clear();
                    initWidgets();
                };
            }

            // Remove button
            WButton removeBtn = actionRow.add(theme.button("Remove")).widget();
            removeBtn.action = () -> {
                sequence.removeAction(index);
                DupeSystem.get().save();
                clear();
                initWidgets();
            };

            actionsTable.row();
        }

        // Add action buttons
        WTable addButtonsTable = actionsTable.add(theme.table()).expandX().widget();

        WButton addCmdBtn = addButtonsTable.add(theme.button("Add Command")).expandX().widget();
        addCmdBtn.action = () -> {
            sequence.addAction(new SequenceAction(ActionType.COMMAND, ""));
            DupeSystem.get().save();
            clear();
            initWidgets();
        };

        WButton addPacketBtn = addButtonsTable.add(theme.button("Add Packet")).expandX().widget();
        addPacketBtn.action = () -> {
            SequenceAction action = new SequenceAction(ActionType.PACKET, "");
            action.setSlot(0);
            action.setCount(1);
            action.setRepeatCount(1);
            sequence.addAction(action);
            DupeSystem.get().save();
            clear();
            initWidgets();
        };

        // Add the Wait button
        WButton addWaitBtn = addButtonsTable.add(theme.button("Add Wait")).expandX().widget();
        addWaitBtn.action = () -> {
            SequenceAction action = new SequenceAction(ActionType.WAIT, "");
            action.setData("1000"); // Default to 1000ms (1 second)
            sequence.addAction(action);
            DupeSystem.get().save();
            clear();
            initWidgets();
        };

        // Add the Close GUI button
        WButton addCloseGuiBtn = addButtonsTable.add(theme.button("Add Close GUI")).expandX().widget();
        addCloseGuiBtn.action = () -> {
            SequenceAction action = new SequenceAction(ActionType.CLOSE_GUI, "normal"); // Default to normal close
            sequence.addAction(action);
            DupeSystem.get().save();
            clear();
            initWidgets();
        };
        table.row();

        // Test sequence button
        WButton testBtn = table.add(theme.button("Start Sequence")).expandX().widget();
        testBtn.action = () -> {
            runSequenceWithRepeat(sequence, sequence.getRepeatCount());
        };

        // Back button
        WButton backBtn = table.add(theme.button("Back")).expandX().widget();
        backBtn.action = () -> mc.setScreen(new DupeSequencesScreen(theme));
    }

    private void runSequenceWithRepeat(DupeSequence sequence, int repeatCount) {
        // Stop any currently running sequence
        DupeSystem.stopCurrentSequence();

        // Start a new sequence
        DupeSystem.isRunningSequence = true;
        Thread sequenceThread = new Thread(() -> {
            try {
                for (int i = 0; i < repeatCount && DupeSystem.isRunningSequence; i++) {
                    runSequence(sequence);

                    // Add a small delay between sequence repeats
                    if (DupeSystem.isRunningSequence && i < repeatCount - 1) {
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

    private void runSequence(DupeSequence sequence) throws InterruptedException {
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

                if (action.getType() == ActionType.WAIT) {
                    // For wait actions, just sleep for the specified time
                    int waitTime = Integer.parseInt(action.getData());
                    Thread.sleep(waitTime);
                } else {
                    // For other actions, execute them and then wait the sequence delay
                    MinecraftClient.getInstance().execute(() -> executeAction(action));
                    Thread.sleep(sequence.getDelayBetweenActions());
                }
            }
        }
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
        } else if (action.getType() == ActionType.CLOSE_GUI) {
            // Close GUI action
            if (mc.player != null && mc.currentScreen != null) {
                if (action.getData().equals("desync")) {
                    // Desync close - close client-side only
                    mc.player.closeScreen();
                } else {
                    // Normal close - send packet to server
                    mc.player.closeHandledScreen();
                }
            }
        }
    }

    // Separate screen for editing an action
    private class EditActionScreen extends WindowScreen {
        private final DupeSequence sequence;
        private final SequenceAction action;
        private final WindowScreen parent;

        public EditActionScreen(GuiTheme theme, DupeSequence sequence, SequenceAction action, WindowScreen parent) {
            super(theme, "Edit Action");
            this.sequence = sequence;
            this.action = action;
            this.parent = parent;
        }

        @Override
        public void initWidgets() {
            WTable table = add(theme.table()).expandX().widget();

            // Action type
            table.add(theme.label("Type:"));
            var typeDropdown = table.add(theme.dropdown(ActionType.values(), action.getType())).expandX().widget();
            typeDropdown.action = () -> {
                action.setType(typeDropdown.get());
                DupeSystem.get().save();
                clear();
                initWidgets();
            };
            table.row();

            // Different fields based on action type
            if (action.getType() == ActionType.COMMAND) {
                table.add(theme.label("Command:"));
                var cmdBox = table.add(theme.textBox(action.getData())).minWidth(400).expandX().widget();
                cmdBox.action = () -> {
                    action.setData(cmdBox.get());
                    DupeSystem.get().save();
                };
            } else if (action.getType() == ActionType.PACKET) {
                // Slot field
                table.add(theme.label("Slot:"));
                var slotBox = table.add(theme.textBox(Integer.toString(action.getSlot()))).expandX().widget();
                slotBox.action = () -> {
                    try {
                        int slot = Integer.parseInt(slotBox.get());
                        action.setSlot(slot);
                        DupeSystem.get().save();
                    } catch (NumberFormatException e) {
                        // Invalid number, do nothing
                    }
                };
                table.row();

                // Count field
                table.add(theme.label("Count:"));
                var countBox = table.add(theme.textBox(Integer.toString(action.getCount()))).expandX().widget();
                countBox.action = () -> {
                    try {
                        int count = Integer.parseInt(countBox.get());
                        if (count > 0) {
                            action.setCount(count);
                            DupeSystem.get().save();
                        }
                    } catch (NumberFormatException e) {
                        // Invalid number, do nothing
                    }
                };
                table.row();


                // Send at once count field
                table.add(theme.label("Send At Once Count:"));
                var sendAtOnceCountBox = table.add(theme.textBox(Integer.toString(action.getRepeatCount()))).expandX().widget();
                sendAtOnceCountBox.action = () -> {
                    try {
                        int sendAtOnceCount = Integer.parseInt(sendAtOnceCountBox.get());
                        if (sendAtOnceCount > 0) {
                            action.setRepeatCount(sendAtOnceCount);
                            DupeSystem.get().save();
                        }
                    } catch (NumberFormatException e) {
                        // Invalid number, do nothing
                    }
                };
                table.row();

                // Action type dropdown
                table.add(theme.label("Action:"));
                var actionTypeDropdown = table.add(theme.dropdown(SlotActionType.values(), action.getSlotActionType())).expandX().widget();
                actionTypeDropdown.action = () -> {
                    action.setSlotActionType(actionTypeDropdown.get());
                    DupeSystem.get().save();
                };
            } else if (action.getType() == ActionType.WAIT) {
                // Wait time field
                table.add(theme.label("Wait Time (ms):"));
                var waitTimeBox = table.add(theme.textBox(action.getData())).expandX().widget();
                waitTimeBox.action = () -> {
                    try {
                        int waitTime = Integer.parseInt(waitTimeBox.get());
                        if (waitTime >= 0) {
                            action.setData(Integer.toString(waitTime));
                            DupeSystem.get().save();
                        }
                    } catch (NumberFormatException e) {
                        // Invalid number, do nothing
                    }
                };
            } else if (action.getType() == ActionType.CLOSE_GUI) {
                // Close GUI type dropdown
                table.add(theme.label("Close Type:"));
                String[] options = {"normal", "desync"};
                String currentValue = action.getData();
                if (!currentValue.equals("normal") && !currentValue.equals("desync")) {
                    currentValue = "normal";
                    action.setData(currentValue);
                }
                var closeTypeDropdown = table.add(theme.dropdown(options, currentValue)).expandX().widget();
                closeTypeDropdown.action = () -> {
                    action.setData(closeTypeDropdown.get());
                    DupeSystem.get().save();
                };
            }
            table.row();

            // Done button
            WButton doneBtn = table.add(theme.button("Done")).expandX().widget();
            doneBtn.action = () -> {
                mc.setScreen(parent);
            };
        }
    }
}
