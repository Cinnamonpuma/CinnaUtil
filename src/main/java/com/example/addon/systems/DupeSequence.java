package com.example.addon.systems;

import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import java.util.ArrayList;
import java.util.List;

public class DupeSequence implements ISerializable<DupeSequence> {
    private String name;
    private List<SequenceAction> actions = new ArrayList<>();
    private int delayBetweenActions = 0;
    private boolean allAtOnce = false;
    private Keybind keybind = Keybind.none();
    private boolean active = false;
    private int repeatCount = 1;

    public DupeSequence() {
        this.name = "Unnamed Sequence";
    }

    public DupeSequence(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SequenceAction> getActions() {
        return actions;
    }

    public void addAction(SequenceAction action) {
        actions.add(action);
    }

    public void removeAction(int index) {
        if (index >= 0 && index < actions.size()) {
            actions.remove(index);
        }
    }

    public int getDelayBetweenActions() {
        return delayBetweenActions;
    }

    public void setDelayBetweenActions(int delayBetweenActions) {
        this.delayBetweenActions = delayBetweenActions;
    }

    public boolean isAllAtOnce() {
        return allAtOnce;
    }

    public void setAllAtOnce(boolean allAtOnce) {
        this.allAtOnce = allAtOnce;
    }

    public Keybind getKeybind() {
        return keybind;
    }

    public void setKeybind(Keybind keybind) {
        if (keybind == null) {
            this.keybind = Keybind.none();
        } else {
            this.keybind = keybind;
        }
    }

    public List<String> getCommands() {
        List<String> commands = new ArrayList<>();
        for (SequenceAction action : actions) {
            if (action.getType() == ActionType.COMMAND) {
                commands.add(action.getData());
            }
        }
        return commands;
    }

    public void addCommand(String command) {
        actions.add(new SequenceAction(ActionType.COMMAND, command));
    }

    public int getDelayBetweenCommands() {
        return delayBetweenActions;
    }

    public void setDelayBetweenCommands(int delay) {
        this.delayBetweenActions = delay;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("name", name);
        tag.putInt("delay", delayBetweenActions);
        tag.putBoolean("allAtOnce", allAtOnce);
        tag.putInt("repeatCount", repeatCount);

        // Serialize keybind using Meteor Client's built-in method
        if (keybind != null && !keybind.equals(Keybind.none())) {
            NbtCompound keybindTag = keybind.toTag();
            tag.put("keybind", keybindTag);
        }

        NbtList actionsTag = new NbtList();
        for (SequenceAction action : actions) {
            actionsTag.add(action.toTag());
        }
        tag.put("actions", actionsTag);

        return tag;
    }

    @Override
    public DupeSequence fromTag(NbtCompound tag) {
        // Fixed NBT deserialization - use direct methods instead of Optional
        if (tag.contains("name")) {
            name = tag.getString("name");
        } else {
            name = "Unnamed Sequence";
        }
        
        if (tag.contains("delay")) {
            delayBetweenActions = tag.getInt("delay");
        } else {
            delayBetweenActions = 0;
        }
        
        if (tag.contains("allAtOnce")) {
            allAtOnce = tag.getBoolean("allAtOnce");
        } else {
            allAtOnce = false;
        }
        
        if (tag.contains("repeatCount")) {
            repeatCount = tag.getInt("repeatCount");
        } else {
            repeatCount = 1;
        }

        // Fixed keybind deserialization
        if (tag.contains("keybind")) {
            try {
                NbtCompound keybindTag = tag.getCompound("keybind");
                this.keybind = new Keybind();
                this.keybind.fromTag(keybindTag);
            } catch (Exception e) {
                System.err.println("[DupeSequence] Failed to deserialize keybind: " + e.getMessage());
                this.keybind = Keybind.none();
            }
        } else {
            this.keybind = Keybind.none();
        }

        // Ensure keybind is never null
        if (this.keybind == null) {
            this.keybind = Keybind.none();
        }

        actions.clear();
        if (tag.contains("actions")) {
            try {
                // Fixed getList call - use single parameter
                NbtList actionsTag = tag.getList("actions", NbtElement.COMPOUND_TYPE);
                for (NbtElement element : actionsTag) {
                    if (element instanceof NbtCompound actionTag) {
                        SequenceAction action = new SequenceAction();
                        action.fromTag(actionTag);
                        actions.add(action);
                    }
                }
            } catch (Exception e) {
                System.err.println("[DupeSequence] Failed to deserialize actions: " + e.getMessage());
            }
        } else if (tag.contains("commands")) {
            // Legacy support for old command format
            try {
                // Fixed getList call - use single parameter
                NbtList commandsTag = tag.getList("commands", NbtElement.STRING_TYPE);
                for (NbtElement element : commandsTag) {
                    // Fixed asString call - use direct method
                    String commandString = element.asString();
                    if (commandString != null && !commandString.isEmpty()) {
                        addCommand(commandString);
                    }
                }
            } catch (Exception e) {
                System.err.println("[DupeSequence] Failed to deserialize legacy commands: " + e.getMessage());
            }
        }

        return this;
    }
}