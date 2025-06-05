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
            NbtList actionsTag = tag.getList("actions").orElse(new NbtList());
            for (NbtElement element : actionsTag) {
                if (element instanceof NbtCompound actionTag) {
                    SequenceAction action = new SequenceAction();
                    action.fromTag(actionTag);
                    actions.add(action);
                }
            }
        } else if (tag.contains("commands")) {
            // Legacy support for old command format
            NbtList commandsTag = tag.getList("commands").orElse(new NbtList());
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
        if (keybindString == null || keybindString.isEmpty() || keybindString.equalsIgnoreCase("none")) {
            return Keybind.none();
        }

        // Try to map common key names (like those from Keybind.toString()) to GLFW key codes.
        // This is an attempt to reverse what Keybind.toString() might be doing.
        // Example: if keybindString is "J", we look for GLFW.GLFW_KEY_J
        // If keybindString is "LEFT_CONTROL", we look for GLFW.GLFW_KEY_LEFT_CONTROL
        try {
            String glfwKeyName = keybindString.toUpperCase();
            if (!glfwKeyName.startsWith("GLFW_KEY_")) {
                // Simple letters/numbers might just be the character itself (e.g., "J")
                // or a descriptive name (e.g., "ESCAPE", "LEFT_CONTROL")
                // We prepend "GLFW_KEY_" to match GLFW constant names.
                if (glfwKeyName.length() == 1 && Character.isLetterOrDigit(glfwKeyName.charAt(0))) {
                    glfwKeyName = "GLFW_KEY_" + glfwKeyName;
                } else {
                    // For names like "ESCAPE", "LEFT_CONTROL", they might already be part of the GLFW constant name
                    // or need specific mapping. This is a heuristic.
                    // We'll try to find a field in GLFW class that ends with the key name.
                    // This part is more complex and might require a pre-defined map for accuracy.
                    // For now, let's assume Keybind.toString() gives a name that can be directly used
                    // or easily transformed into a GLFW constant name.
                    // A simple heuristic: if it's a known descriptive name, try it directly with GLFW_KEY_ prefix.
                    // This is still a guess without knowing Keybind.toString() exact output format.
                    boolean foundMatch = false;
                    for (java.lang.reflect.Field field : org.lwjgl.glfw.GLFW.class.getFields()) {
                        if (field.getName().endsWith("_" + glfwKeyName)) {
                            glfwKeyName = field.getName();
                            foundMatch = true;
                            break;
                        }
                    }
                    if (!foundMatch && !glfwKeyName.startsWith("GLFW_KEY_")) {
                         glfwKeyName = "GLFW_KEY_" + glfwKeyName; // Default attempt
                    }
                }
            }

            java.lang.reflect.Field field = org.lwjgl.glfw.GLFW.class.getField(glfwKeyName);
            int keyCode = field.getInt(null);
            return Keybind.fromKey(keyCode);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // System.err.println("[DupeSequence] Failed to find GLFW key code for '" + keybindString + "' (tried as '" + keybindString.toUpperCase().replace(".", "_") + "'): " + e.getMessage());
        } catch (Exception e) {
            // System.err.println("[DupeSequence] Error resolving keybind '" + keybindString + "' via reflection: " + e.getMessage());
        }

        // Fallback: try to parse as an integer key code directly (if it was stored as such)
        try {
            int keyCode = Integer.parseInt(keybindString);
            return Keybind.fromKey(keyCode);
        } catch (NumberFormatException e) {
            System.err.println("[DupeSequence] Failed to parse keybind string '" + keybindString + "' as integer, and GLFW name lookup also failed.");
        }

        return Keybind.none(); // Default to none if all parsing fails
    }
}