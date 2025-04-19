package com.example.addon.systems;

import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.SlotActionType;

public class SequenceAction implements ISerializable<SequenceAction> {
    private ActionType type;
    private String data;
    private int slot = -1;
    private int count = 1;
    private int repeatCount = 1; // Added field
    private SlotActionType slotActionType = SlotActionType.PICKUP;

    public SequenceAction() {
        this.type = ActionType.COMMAND;
        this.data = "";
    }

    public SequenceAction(ActionType type, String data) {
        this.type = type;
        this.data = data;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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
        this.count = count;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public SlotActionType getSlotActionType() {
        return slotActionType;
    }

    public void setSlotActionType(SlotActionType slotActionType) {
        this.slotActionType = slotActionType;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("type", type.name());
        tag.putString("data", data);
        tag.putInt("slot", slot);
        tag.putInt("count", count);
        tag.putInt("repeatCount", repeatCount); // Save repeatCount
        tag.putString("slotActionType", slotActionType.name());
        return tag;
    }

    @Override
    public SequenceAction fromTag(NbtCompound tag) {
        type = ActionType.valueOf(tag.getString("type").orElse("COMMAND")); // Default to COMMAND
        data = tag.getString("data").orElse("");
        slot = tag.getInt("slot").orElse(-1);
        count = tag.getInt("count").orElse(1);
        repeatCount = tag.contains("repeatCount") ? tag.getInt("repeatCount").orElse(1) : 1; // Default to 1
        slotActionType = SlotActionType.valueOf(tag.getString("slotActionType").orElse("PICKUP")); // Default to PICKUP
        return this;
    }
}
