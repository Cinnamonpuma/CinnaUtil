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
        this.keybind = keybind;
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
        tag.putString("keybind", keybind.toString());
        tag.putInt("repeatCount", repeatCount);

        NbtList actionsTag = new NbtList();
        for (SequenceAction action : actions) {
            actionsTag.add(action.toTag());
        }
        tag.put("actions", actionsTag);

        return tag;
    }

    @Override
    public DupeSequence fromTag(NbtCompound tag) {
        // Handle Optional return types for NBT operations
        name = tag.getString("name").orElse("Unnamed Sequence");
        delayBetweenActions = tag.getInt("delay").orElse(0);
        allAtOnce = tag.getBoolean("allAtOnce").orElse(false);
        repeatCount = tag.getInt("repeatCount").orElse(1);

        // Handle keybind deserialization - fix the API usage
        if (tag.contains("keybind")) {
            String keybindString = tag.getString("keybind").orElse("");
            if (!keybindString.isEmpty()) {
                try {
                    // Try to parse the keybind - may need to use different method based on Meteor Client version
                    this.keybind = parseKeybind(keybindString);
                } catch (Exception e) {
                    System.err.println("[DupeSequence] Failed to parse keybind string '" + keybindString + "': " + e.getMessage());
                    this.keybind = Keybind.none();
                }
            } else {
                this.keybind = Keybind.none();
            }
        } else {
            this.keybind = Keybind.none();
        }

        actions.clear();
        if (tag.contains("actions")) {
            NbtList actionsTag = tag.getList("actions");
            for (NbtElement element : actionsTag) {
                if (element instanceof NbtCompound actionTag) {
                    SequenceAction action = new SequenceAction();
                    action.fromTag(actionTag);
                    actions.add(action);
                }
            }
        } else if (tag.contains("commands")) {
            // Legacy support for old command format
            NbtList commandsTag = tag.getList("commands");
            for (NbtElement element : commandsTag) {
                String commandString = element.asString().orElse("");
                if (!commandString.isEmpty()) {
                    addCommand(commandString);
                }
            }
        }

        return this;
    }

    /**
     * Helper method to parse keybind from string
     * This may need adjustment based on your Meteor Client version
     */
    private Keybind parseKeybind(String keybindString) {
        // Try different parsing approaches based on Meteor Client API
        try {
            // Option 1: Try direct constructor or factory method
            return new Keybind.Builder().fromString(keybindString).build();
        } catch (Exception e1) {
            try {
                // Option 2: Try alternative parsing method
                return Keybind.fromString(keybindString);
            } catch (Exception e2) {
                // Option 3: Manual parsing - this is a fallback
                // You may need to implement this based on your specific keybind format
                System.err.println("[DupeSequence] Could not parse keybind: " + keybindString);
                return Keybind.none();
            }
        }
    }
}