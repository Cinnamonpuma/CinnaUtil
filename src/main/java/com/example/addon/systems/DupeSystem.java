package com.example.addon.systems;

import meteordevelopment.meteorclient.systems.System;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DupeSystem extends System<DupeSystem> implements Iterable<DupeSequence> {
    public static DupeSystem INSTANCE;
    private List<DupeSequence> sequences = new ArrayList<>();
    public static boolean isRunningSequence = false;
    public static Thread currentSequenceThread = null;
    private static DupeSequence currentSequence = null; // Added field
    private static final int TICKS_PER_SECOND = 20;

    // Added getter
    public static DupeSequence getCurrentSequence() {
        return currentSequence;
    }

    public DupeSystem() {
        super("dupe");
    }

    public static DupeSystem get() {
        if (INSTANCE == null) INSTANCE = new DupeSystem();
        if (INSTANCE == null) INSTANCE = new DupeSystem();
        return INSTANCE;
    }

    public void add(DupeSequence sequence) {
        sequences.add(sequence);
        save();
    }

    public void save() {
        meteordevelopment.meteorclient.systems.Systems.add(this);
    }

    public void save() {
        meteordevelopment.meteorclient.systems.Systems.add(this);
    }

    public void remove(DupeSequence sequence) {
        sequences.remove(sequence);
        save();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        NbtList sequencesTag = new NbtList();
        for (DupeSequence sequence : sequences) {
            sequencesTag.add(sequence.toTag());
        }
        tag.put("sequences", sequencesTag);
        return tag;
    }

    @Override
    public DupeSystem fromTag(NbtCompound tag) {
        sequences.clear();
        if (tag.contains("sequences")) {
            NbtList sequencesTag = (NbtList) tag.get("sequences");
            if (sequencesTag != null) {
                for (int i = 0; i < sequencesTag.size(); i++) {
                    if (sequencesTag.get(i) instanceof NbtCompound compoundTag) {
                        DupeSequence sequence = new DupeSequence();
                        sequence.fromTag(compoundTag);
                        sequences.add(sequence);
                    }
                }
            NbtList sequencesTag = (NbtList) tag.get("sequences");
            if (sequencesTag != null) {
                for (int i = 0; i < sequencesTag.size(); i++) {
                    if (sequencesTag.get(i) instanceof NbtCompound compoundTag) {
                        DupeSequence sequence = new DupeSequence();
                        sequence.fromTag(compoundTag);
                        sequences.add(sequence);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public Iterator<DupeSequence> iterator() {
        return sequences.iterator();
    }

    public static void stopCurrentSequence() {
        isRunningSequence = false;
        currentSequence = null; // Clear current sequence
        if (currentSequenceThread != null) {
            currentSequenceThread.interrupt();
            currentSequenceThread = null;
        }
    }

    public static void executeSequence(DupeSequence sequence, int repeatCount) {
        stopCurrentSequence();
        currentSequence = sequence; // Set current sequence
        isRunningSequence = true;

        Thread sequenceThread = new Thread(() -> {
            try {
                int currentTick = 0;

                for (int i = 0; i < repeatCount && isRunningSequence; i++) {
                    if (sequence.isAllAtOnce()) {
                        for (SequenceAction action : sequence.getActions()) {
                            if (!isRunningSequence) break;
                            if (action.getType() != ActionType.WAIT) {
                                MinecraftClient.getInstance().execute(() -> executeAction(action));
                            }
                        }
                    } else {
                        for (SequenceAction action : sequence.getActions()) {
                            if (!isRunningSequence) break;

                            if (action.getType() == ActionType.WAIT) {
                                int waitTicks = Integer.parseInt(action.getData());
                                currentTick += waitTicks;
                                waitTicks(currentTick);
                            } else {
                                MinecraftClient.getInstance().execute(() -> executeAction(action));
                                currentTick += sequence.getDelayBetweenActions();
                                waitTicks(currentTick);
                            }
                        }
                    }

                    if (isRunningSequence && i < repeatCount - 1) {
                        currentTick += 10;
                        waitTicks(currentTick);
                    }
                }
            } finally {
                isRunningSequence = false;
                currentSequenceThread = null;
                 currentSequence = null; // Clear current sequence on natural completion
            }
        });

        currentSequenceThread = sequenceThread;
        sequenceThread.start();
    }

    private static void waitTicks(int targetTick) {
        long targetTime = java.lang.System.currentTimeMillis() + (targetTick * 50);
        while (java.lang.System.currentTimeMillis() < targetTime && isRunningSequence) {
            Thread.yield();
        }
    }

    private static void executeAction(SequenceAction action) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (action.getType() == ActionType.COMMAND) {
            if (mc.player != null) {
                String cmd = action.getData();
                if (cmd.startsWith("/")) cmd = cmd.substring(1);
                mc.player.networkHandler.sendCommand(cmd);
            }
        } else if (action.getType() == ActionType.PACKET) {
            if (mc.player != null && mc.player.currentScreenHandler != null) {
                int slot = action.getSlot();
                for (int i = 0; i < action.getRepeatCount(); i++) {
                    mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        slot,
                        0,
                        action.getSlotActionType(),
                        mc.player
                    );
                }
            }
        } else if (action.getType() == ActionType.CLOSE_GUI) {
            if (mc.player != null && mc.currentScreen != null) {
                if (action.getData().equals("desync")) {
                    mc.player.closeScreen();
                } else {
                    mc.player.closeHandledScreen();
                }
            }
        }
    }
}
