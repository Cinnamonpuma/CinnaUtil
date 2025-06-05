package com.example.addon.systems;

import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.SlotActionType;

public class SequenceAction implements ISerializable<SequenceAction> {
    private ActionType type;
    private String data;
    private int slot = -1;
    private int count = 1;
    private int repeatCount = 1;
    private SlotActionType slotActionType = SlotActionType.PICKUP;

    public SequenceAction() {
        this.type = ActionType.COMMAND;
        this.data = "";
    }

    public SequenceAction(ActionType type, String data) {
        this.type = type != null ? type : ActionType.COMMAND;
        this.data = data != null ? data : "";
    }

    // Getters and setters with validation
    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type != null ? type : ActionType.COMMAND;
    }

    public String getData() {
        return data != null ? data : "";
    }

    public void setData(String data) {
        this.data = data != null ? data : "";
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = Math.max(1, count); // Ensure count is at least 1
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = Math.max(1, repeatCount); // Ensure repeatCount is at least 1
    }

    public SlotActionType getSlotActionType() {
        return slotActionType;
    }

    public void setSlotActionType(SlotActionType slotActionType) {
        this.slotActionType = slotActionType != null ? slotActionType : SlotActionType.PICKUP;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        
        try {
            // Store type as string
            tag.putString("type", type.name());
            
            // Store data
            tag.putString("data", data != null ? data : "");
            
            // Store numeric values
            tag.putInt("slot", slot);
            tag.putInt("count", count);
            tag.putInt("repeatCount", repeatCount);
            
            // Store slot action type
            tag.putString("slotActionType", slotActionType.name());
            
        } catch (Exception e) {
            System.err.println("Error serializing SequenceAction: " + e.getMessage());
        }
        
        return tag;
    }

    @Override
    public SequenceAction fromTag(NbtCompound tag) {
        if (tag == null) {
            return this;
        }

        try {
            // Load action type with fallback - Fixed valueOf call
            if (tag.contains("type")) {
                String typeString = tag.getString("type");
                try {
                    this.type = ActionType.valueOf(typeString);
                } catch (IllegalArgumentException e) {
                    this.type = ActionType.COMMAND;
                }
            } else {
                this.type = ActionType.COMMAND;
            }

            // Load data - Fixed getString call
            if (tag.contains("data")) {
                this.data = tag.getString("data");
            } else {
                this.data = "";
            }

            // Load numeric values - Fixed getInt calls
            if (tag.contains("slot")) {
                this.slot = tag.getInt("slot");
            } else {
                this.slot = -1;
            }

            if (tag.contains("count")) {
                this.count = Math.max(1, tag.getInt("count"));
            } else {
                this.count = 1;
            }

            if (tag.contains("repeatCount")) {
                this.repeatCount = Math.max(1, tag.getInt("repeatCount"));
            } else {
                this.repeatCount = 1;
            }

            // Load slot action type - Fixed valueOf call
            if (tag.contains("slotActionType")) {
                String slotActionTypeString = tag.getString("slotActionType");
                try {
                    this.slotActionType = SlotActionType.valueOf(slotActionTypeString);
                } catch (IllegalArgumentException e) {
                    this.slotActionType = SlotActionType.PICKUP;
                }
            } else {
                this.slotActionType = SlotActionType.PICKUP;
            }

        } catch (Exception e) {
            System.err.println("Error deserializing SequenceAction: " + e.getMessage());
            // Set safe defaults
            this.type = ActionType.COMMAND;
            this.data = "";
            this.slot = -1;
            this.count = 1;
            this.repeatCount = 1;
            this.slotActionType = SlotActionType.PICKUP;
        }
        
        return this;
    }
}