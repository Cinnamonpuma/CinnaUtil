package com.example.addon.gui;

import com.example.addon.systems.ActionType;
import com.example.addon.systems.DupeSequence;
import com.example.addon.systems.DupeSystem;
import com.example.addon.systems.SequenceAction;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
// Remove the problematic import
// import meteordevelopment.meteorclient.gui.widgets.input.WKeybind;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.slot.SlotActionType;

public class EditDupeSequenceScreen extends WindowScreen {
    private boolean settingKeybind = false;
    private final DupeSequence sequence;
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public EditDupeSequenceScreen(GuiTheme theme, DupeSequence sequence) {
        super(theme, "Edit Sequence");
        this.sequence = sequence;
    }

    @Override
    public void initWidgets() {
        WTable table = add(theme.table()).expandX().widget();

        // Name
        table.add(theme.label("Name:"));
        var nameBox = table.add(theme.textBox(sequence.getName())).minWidth(400).expandX().widget();
        nameBox.action = () -> {
            sequence.setName(nameBox.get());
            DupeSystem.get().save();
        };
        table.row();

        // Delay
        table.add(theme.label("Delay (ticks):"));
        var delayEdit = table.add(theme.textBox(Integer.toString(sequence.getDelayBetweenActions()))).expandX().widget();
        delayEdit.action = () -> {
            try {
                int delay = Integer.parseInt(delayEdit.get());
                if (delay >= 0) {
                    sequence.setDelayBetweenActions(delay);
                    DupeSystem.get().save();
                }
            } catch (NumberFormatException e) {
                // Invalid input, ignore
            }
        };
        table.row();

        // All at once checkbox
        table.add(theme.label("Execute all at once:"));
        var allAtOnceCheckbox = table.add(theme.checkbox(sequence.isAllAtOnce())).widget();
        allAtOnceCheckbox.action = () -> {
            sequence.setAllAtOnce(allAtOnceCheckbox.checked);
            DupeSystem.get().save();
        };
        table.row();

        // Repeat count
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
                // Invalid input, ignore
            }
        };
        table.row();

        // Keybind - Alternative approach using text box for now
        table.add(theme.label("Keybind:"));
        String keybindText = sequence.getKeybind() != null ? sequence.getKeybind().toString() : "None";
        var keybindLabel = table.add(theme.label(keybindText)).expandX().widget();
        
        // Add a button to set keybind manually (you can enhance this later)
        var setKeybindBtn = table.add(theme.button("Set Keybind")).widget();
        setKeybindBtn.action = () -> {
            // For now, just show that keybind functionality needs to be implemented
            settingKeybind = true;
            keybindLabel.set("Press a key..."); // Use set() instead of setRenderText()
        };
        table.row();

        // Actions section
        var actionsSection = table.add(theme.section("Actions")).expandX().widget();
        var actionsTable = actionsSection.add(theme.table()).expandX().widget();

        // Display existing actions
        for (int i = 0; i < sequence.getActions().size(); i++) {
            final int index = i;
            SequenceAction action = sequence.getActions().get(i);

            WTable actionRow = actionsTable.add(theme.table()).expandX().widget();

            // Action label
            String actionLabel;
            if (action.getType() == ActionType.COMMAND) {
                actionLabel = "Command: " + action.getData();
            } else if (action.getType() == ActionType.PACKET) {
                actionLabel = "Packet: Slot " + action.getSlot() + (action.getRepeatCount() > 1 ? " (x" + action.getRepeatCount() + ")" : "");
            } else if (action.getType() == ActionType.WAIT) {
                actionLabel = "Wait: " + action.getData() + "ticks";
            } else if (action.getType() == ActionType.CLOSE_GUI) {
                actionLabel = "Close GUI: " + (action.getData().equals("desync") ? "Desync" : "Normal");
            } else {
                actionLabel = "Unknown action type";
            }

            actionRow.add(theme.label(actionLabel)).expandX();

            WButton editBtn = actionRow.add(theme.button("Edit")).widget();
            editBtn.action = () -> {
                mc.setScreen(new EditActionScreen(theme, sequence, action, this));
            };

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

        WButton addWaitBtn = addButtonsTable.add(theme.button("Add Wait")).expandX().widget();
        addWaitBtn.action = () -> {
            SequenceAction action = new SequenceAction(ActionType.WAIT, "");
            action.setData("20");
            sequence.addAction(action);
            DupeSystem.get().save();
            clear();
            initWidgets();
        };

        WButton addCloseGuiBtn = addButtonsTable.add(theme.button("Add Close GUI")).expandX().widget();
        addCloseGuiBtn.action = () -> {
            SequenceAction action = new SequenceAction(ActionType.CLOSE_GUI, "normal");
            sequence.addAction(action);
            DupeSystem.get().save();
            clear();
            initWidgets();
        };
        table.row();

        // Start/Stop sequence button
        String buttonText = "Start Sequence";
        if (DupeSystem.isRunningSequence && DupeSystem.getCurrentSequence() == sequence) {
            buttonText = "Stop Sequence";
        }
        WButton testBtn = table.add(theme.button(buttonText)).expandX().widget();
        testBtn.action = () -> {
            if (DupeSystem.isRunningSequence && DupeSystem.getCurrentSequence() == sequence) {
                DupeSystem.stopCurrentSequence();
            } else {
                DupeSystem.executeSequence(sequence, sequence.getRepeatCount());
            }
            clear();
            initWidgets();
        };

        WButton backBtn = table.add(theme.button("Back")).expandX().widget();
        backBtn.action = () -> mc.setScreen(new DupeSequencesScreen(theme));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (settingKeybind) {
            sequence.setKeybind(Keybind.fromKey(keyCode));
            DupeSystem.get().save();
            settingKeybind = false;
            // Re-initialize widgets to update the keybind label
            clear();
            initWidgets();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

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

            table.add(theme.label("Type:"));
            var typeDropdown = table.add(theme.dropdown(ActionType.values(), action.getType())).expandX().widget();
            typeDropdown.action = () -> {
                action.setType(typeDropdown.get());
                DupeSystem.get().save();
                clear();
                initWidgets();
            };
            table.row();

            if (action.getType() == ActionType.COMMAND) {
                table.add(theme.label("Command:"));
                var cmdBox = table.add(theme.textBox(action.getData())).minWidth(400).expandX().widget();
                cmdBox.action = () -> {
                    action.setData(cmdBox.get());
                    DupeSystem.get().save();
                };
            } else if (action.getType() == ActionType.PACKET) {
                table.add(theme.label("Slot:"));
                var slotBox = table.add(theme.textBox(Integer.toString(action.getSlot()))).expandX().widget();
                slotBox.action = () -> {
                    try {
                        int slot = Integer.parseInt(slotBox.get());
                        action.setSlot(slot);
                        DupeSystem.get().save();
                    } catch (NumberFormatException e) {
                    }
                };
                table.row();

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
                    }
                };
                table.row();

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
                    }
                };
                table.row();

                table.add(theme.label("Action:"));
                var actionTypeDropdown = table.add(theme.dropdown(SlotActionType.values(), action.getSlotActionType())).expandX().widget();
                actionTypeDropdown.action = () -> {
                    action.setSlotActionType(actionTypeDropdown.get());
                    DupeSystem.get().save();
                };
            } else if (action.getType() == ActionType.WAIT) {
                table.add(theme.label("Delay (ticks):"));
                var waitTimeBox = table.add(theme.textBox(action.getData())).expandX().widget();
                waitTimeBox.action = () -> {
                    try {
                        int waitTime = Integer.parseInt(waitTimeBox.get());
                        if (waitTime >= 0) {
                            action.setData(Integer.toString(waitTime));
                            DupeSystem.get().save();
                        }
                    } catch (NumberFormatException e) {
                    }
                };
            } else if (action.getType() == ActionType.CLOSE_GUI) {
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

            WButton doneBtn = table.add(theme.button("Done")).expandX().widget();
            doneBtn.action = () -> {
                mc.setScreen(parent);
            };
        }
    }
}