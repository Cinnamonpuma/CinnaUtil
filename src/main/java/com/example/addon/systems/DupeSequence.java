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
    private int delayBetweenActions = 500; // milliseconds
    private boolean allAtOnce = false;
    private Keybind keybind = Keybind.none();
    private boolean active = false;

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

    // For backward compatibility
    public List<String> getCommands() {
        List<String> commands = new ArrayList<>();
        for (SequenceAction action : actions) {
            if (action.getType() == ActionType.COMMAND) {
                commands.add(action.getData());
            }
        }
        return commands;
    }

    // For backward compatibility
    public void addCommand(String command) {
        actions.add(new SequenceAction(ActionType.COMMAND, command));
    }

    // For backward compatibility
    public int getDelayBetweenCommands() {
        return delayBetweenActions;
    }

    // For backward compatibility
    public void setDelayBetweenCommands(int delay) {
        this.delayBetweenActions = delay;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("name", name);
        tag.putInt("delay", delayBetweenActions);
        tag.putBoolean("allAtOnce", allAtOnce);

        NbtList actionsTag = new NbtList();
        for (SequenceAction action : actions) {
            actionsTag.add(action.toTag());
        }
        tag.put("actions", actionsTag);

        tag.putInt("repeatCount", repeatCount);
        return tag;
    }

    @Override
    public DupeSequence fromTag(NbtCompound tag) {
        name = tag.getString("name").orElse("Unnamed Sequence");
        delayBetweenActions = tag.getInt("delay").orElse(500);
        allAtOnce = tag.getBoolean("allAtOnce").orElse(false);

        actions.clear();
        if (tag.contains("actions")) {
            // New format with actions
            NbtList actionsTag = tag.getList("actions").orElse(new NbtList());
            for (NbtElement element : actionsTag) {
                if (element instanceof NbtCompound actionTag) {
                    SequenceAction action = new SequenceAction();
                    action.fromTag(actionTag);
                    actions.add(action);
                }
            }
        } else if (tag.contains("commands")) {
            // Old format with just commands
            NbtList commandsTag = tag.getList("commands").orElse(new NbtList());
            for (NbtElement element : commandsTag) {
                addCommand(element.asString().orElse(""));
            }
        }

        if (tag.contains("repeatCount")) {
            repeatCount = tag.getInt("repeatCount").orElse(1);
        }
        return this;
    }

    private int repeatCount = 1;

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }
}
