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
            // Load action type with fallback
            if (tag.contains("type