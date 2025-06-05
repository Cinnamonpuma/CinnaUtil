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
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class EditDupeSequenceScreen extends WindowScreen {
    private boolean settingKeybind = false;
    private final DupeSequence sequence;
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private WLabel keybindLabel;

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

        // Keybind section - FIXED IMPLEMENTATION
        table.add(theme.label("Keybind:"));
        String keybindText = getKeybindDisplayText();
        keybindLabel = table.add(theme.label(keybindText)).expandX().widget();
        
        var setKeybindBtn = table.add(theme.button(settingKeybind ? "Press key..." : "Set Keybind")).widget();
        setKeybindBtn.action = () -> {
            if (!settingKeybind) {
                settingKeybind = true;
                updateKeybindDisplay();
            }
        };
        
        // Clear keybind button
        var clearKeybindBtn = table.add(theme.button("Clear")).widget();
        clearKeybindBtn.action = () -> {
            sequence.setKeybind(null);
            DupeSystem.get().save();
            settingKeybind = false;
            updateKeybindDisplay();
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
            String actionLabel = getActionDisplayText(action);
            actionRow.add(theme.label(actionLabel)).expandX();

            WButton editBtn = actionRow.add(theme.button("Edit")).widget();
            editBtn.action = () -> {
                mc.setScreen(new EditActionScreen(theme, sequence, action, this));
            };

            if (i > 0) {
                WButton upBtn = actionRow.add(theme.button("↑")).widget();
                upBtn.action = () -> {
                    moveAction(index, -1);
                };
            }

            if (i < sequence.getActions().size() - 1) {
                WButton downBtn = actionRow.add(theme.button("↓")).widget();
                downBtn.action = () -> {
                    moveAction(index, 1);
                };
            }

            WButton removeBtn = actionRow.add(theme.button("Remove")).widget();
            removeBtn.action = () -> {
                sequence.removeAction(index);
                DupeSystem.get().save();
                refreshUI();
            };

            actionsTable.row();
        }

        // Add action buttons
        WTable addButtonsTable = actionsTable.add(theme.table()).expandX().widget();

        WButton addCmdBtn = addButtonsTable.add(theme.button("Add Command")).expandX().widget();
        addCmdBtn.action = () -> {
            sequence.addAction(new SequenceAction(ActionType.COMMAND, ""));
            DupeSystem.get().save();
            refreshUI();
        };

        WButton addPacketBtn = addButtonsTable.add(theme.button("Add Packet")).expandX().widget();
        addPacketBtn.action = () -> {
            SequenceAction action = new SequenceAction(ActionType.PACKET, "");
            action.setSlot(0);
            action.setCount(1);
            action.setRepeatCount(1);
            sequence.addAction(action);
            DupeSystem.get().save();
            refreshUI();
        };

        WButton addWaitBtn = addButtonsTable.add(theme.button("Add Wait")).expandX().widget();
        addWaitBtn.action = () -> {
            SequenceAction action = new SequenceAction(ActionType.WAIT, "20");
            sequence.addAction(action);
            DupeSystem.get().save();
            refreshUI();
        };

        WButton addCloseGuiBtn = addButtonsTable.add(theme.button("Add Close GUI")).expandX().widget();
        addCloseGuiBtn.action = () -> {
            SequenceAction action = new SequenceAction(ActionType.CLOSE_GUI, "normal");
            sequence.addAction(action);
            DupeSystem.get().save();
            refreshUI();
        };
        table.row();

        // Start/Stop sequence button - FIXED AtomicBoolean usage
        String buttonText = (DupeSystem.isRunningSequence.get() && DupeSystem.getCurrentSequence() == sequence) 
            ? "Stop Sequence" : "Start Sequence";
        WButton testBtn = table.add(theme.button(buttonText)).expandX().widget();
        testBtn.action = () -> {
            if (DupeSystem.isRunningSequence.get() && DupeSystem.getCurrentSequence() == sequence) {
                DupeSystem.stopCurrentSequence();
            } else {
                DupeSystem.executeSequence(sequence, sequence.getRepeatCount());
            }
            refreshUI();
        };

        WButton backBtn = table.add(theme.button("Back")).expandX().widget();
        backBtn.action = () -> mc.setScreen(new DupeSequencesScreen(theme));
    }

    private String getKeybindDisplayText() {
        if (settingKeybind) {
            return "Press any key...";
        }
        return sequence.getKeybind() != null ? sequence.getKeybind().toString() : "None";
    }

    private void updateKeybindDisplay() {
        refreshUI();
     }

    private String getActionDisplayText(SequenceAction action) {
        switch (action.getType()) {
            case COMMAND:
                return "Command: " + (action.getData().isEmpty() ? "[empty]" : action.getData());
            case PACKET:
                return String.format("Packet: Slot %d%s (%s)", 
                    action.getSlot(),
                    action.getRepeatCount() > 1 ? " (x" + action.getRepeatCount() + ")" : "",
                    action.getSlotActionType().name().toLowerCase());
            case WAIT:
                return "Wait: " + action.getData() + " ticks";
            case CLOSE_GUI:
                return "Close GUI: " + (action.getData().equals("desync") ? "Desync" : "Normal");
            default:
                return "Unknown action type";
        }
    }

    private void moveAction(int index, int direction) {
        int newIndex = index + direction;
        if (newIndex >= 0 && newIndex < sequence.getActions().size()) {
            SequenceAction temp = sequence.getActions().get(index);
            sequence.getActions().set(index, sequence.getActions().get(newIndex));
            sequence.getActions().set(newIndex, temp);
            DupeSystem.get().save();
            refreshUI();
        }
    }

    private void refreshUI() {
        clear();
        initWidgets();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (settingKeybind) {
            // Handle escape key to cancel keybind setting
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                settingKeybind = false;
                updateKeybindDisplay();
                return true;
            }
            
            // Set the keybind
            Keybind newKeybind = Keybind.fromKey(keyCode);
            sequence.setKeybind(newKeybind);
            DupeSystem.get().save();
            settingKeybind = false;
            updateKeybindDisplay();
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

            switch (action.getType()) {
                case COMMAND:
                    table.add(theme.label("Command:"));
                    var cmdBox = table.add(theme.textBox(action.getData())).minWidth(400).expandX().widget();
                    cmdBox.action = () -> {
                        action.setData(cmdBox.get());
                        DupeSystem.get().save();
                    };
                    break;

                case PACKET:
                    table.add(theme.label("Slot:"));
                    var slotBox = table.add(theme.textBox(Integer.toString(action.getSlot()))).expandX().widget();
                    slotBox.action = () -> {
                        try {
                            int slot = Integer.parseInt(slotBox.get());
                            action.setSlot(slot);
                            DupeSystem.get().save();
                        } catch (NumberFormatException e) {
                            // Invalid input, ignore
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
                            // Invalid input, ignore
                        }
                    };
                    table.row();

                    table.add(theme.label("Repeat Count:"));
                    var repeatCountBox = table.add(theme.textBox(Integer.toString(action.getRepeatCount()))).expandX().widget();
                    repeatCountBox.action = () -> {
                        try {
                            int repeatCount = Integer.parseInt(repeatCountBox.get());
                            if (repeatCount > 0) {
                                action.setRepeatCount(repeatCount);
                                DupeSystem.get().save();
                            }
                        } catch (NumberFormatException e) {
                            // Invalid input, ignore
                        }
                    };
                    table.row();

                    table.add(theme.label("Action Type:"));
                    var actionTypeDropdown = table.add(theme.dropdown(SlotActionType.values(), action.getSlotActionType())).expandX().widget();
                    actionTypeDropdown.action = () -> {
                        action.setSlotActionType(actionTypeDropdown.get());
                        DupeSystem.get().save();
                    };
                    break;

                case WAIT:
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
                            // Invalid input, ignore
                        }
                    };
                    break;

                case CLOSE_GUI:
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
                    break;
            }
            table.row();

            WButton doneBtn = table.add(theme.button("Done")).expandX().widget();
            doneBtn.action = () -> mc.setScreen(parent);
        }
    }
}