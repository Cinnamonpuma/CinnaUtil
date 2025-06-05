package com.example.addon.systems;

import meteordevelopment.meteorclient.systems.System;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class DupeSystem extends System<DupeSystem> implements Iterable<DupeSequence> {
    public static DupeSystem INSTANCE;
    private final List<DupeSequence> sequences = new ArrayList<>();
    
    // Thread-safe execution state - Made public instead of private
    public static final AtomicBoolean isRunningSequence = new AtomicBoolean(false);
    private static volatile Thread currentSequenceThread = null;
    private static volatile DupeSequence currentSequence = null;
    private static final ReentrantLock executionLock = new ReentrantLock();
    
    private static final int TICKS_PER_SECOND = 20;
    private static final long TICK_DURATION_MS = 50; // 50ms per tick

    public DupeSystem() {
        super("dupe");
    }

    public static DupeSystem get() {
        if (INSTANCE == null) {
            INSTANCE = new DupeSystem();
        }
        return INSTANCE;
    }

    // Thread-safe getters - Fixed access to isRunningSequence
    public static boolean isRunningSequence() {
        return isRunningSequence.get();
    }

    public static DupeSequence getCurrentSequence() {
        return currentSequence;
    }

    public void add(DupeSequence sequence) {
        if (sequence != null) {
            sequences.add(sequence);
            save();
        }
    }

    public void remove(DupeSequence sequence) {
        if (sequence != null) {
            sequences.remove(sequence);
            save();
        }
    }

    public void save() {
        try {
            meteordevelopment.meteorclient.systems.Systems.add(this);
        } catch (Exception e) {
            // Fixed System.err reference
            java.lang.System.out.println("Failed to save DupeSystem: " + e.getMessage());
        }
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        NbtList sequencesTag = new NbtList();
        
        try {
            for (DupeSequence sequence : sequences) {
                if (sequence != null) {
                    NbtCompound sequenceTag = sequence.toTag();
                    if (sequenceTag != null) {
                        sequencesTag.add(sequenceTag);
                    }
                }
            }
            tag.put("sequences", sequencesTag);
        } catch (Exception e) {
            // Fixed System.err reference
            java.lang.System.out.println("Error serializing DupeSystem: " + e.getMessage());
        }
        
        return tag;
    }

    @Override
    public DupeSystem fromTag(NbtCompound tag) {
        sequences.clear();
        
        try {
            if (tag.contains("sequences")) {
                // Fixed getList call - use single parameter for 1.21.5
                NbtList sequencesTag = (NbtList) tag.get("sequences");
                if (sequencesTag.size() > 0) {
                NbtCompound compoundTag = (NbtCompound) sequencesTag.get(0);
    // Continue with your logic
}
                
                for (int i = 0; i < sequencesTag.size(); i++) {
                    try {
                        // Fixed getCompound call - use direct method
                        NbtCompound compoundTag = sequencesTag.getCompound(i).orElse(new NbtCompound());
                        if (compoundTag != null) {
                            DupeSequence sequence = new DupeSequence();
                            sequence.fromTag(compoundTag);
                            sequences.add(sequence);
                        }
                    } catch (Exception e) {
                        // Fixed System.err reference
                        java.lang.System.out.println("Error loading sequence " + i + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // Fixed System.err reference
            java.lang.System.out.println("Error deserializing DupeSystem: " + e.getMessage());
        }
        
        return this;
    }

    @Override
    public Iterator<DupeSequence> iterator() {
        return new ArrayList<>(sequences).iterator(); // Return copy to prevent concurrent modification
    }

    public static void stopCurrentSequence() {
        executionLock.lock();
        try {
            isRunningSequence.set(false);
            currentSequence = null;
            
            Thread thread = currentSequenceThread;
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
                try {
                    thread.join(1000); // Wait up to 1 second for thread to finish
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            currentSequenceThread = null;
        } finally {
            executionLock.unlock();
        }
    }

    public static void executeSequence(DupeSequence sequence, int repeatCount) {
        if (sequence == null || repeatCount <= 0) {
            return;
        }

        executionLock.lock();
        try {
            // Stop any currently running sequence
            stopCurrentSequence();
            
            currentSequence = sequence;
            isRunningSequence.set(true);

            Thread sequenceThread = new Thread(() -> {
                try {
                    executeSequenceInternal(sequence, repeatCount);
                } catch (InterruptedException e) {
                    // Thread was interrupted, clean exit
                } catch (Exception e) {
                    // Fixed System.err reference
                    java.lang.System.out.println("Error executing sequence: " + e.getMessage());
                } finally {
                    // Cleanup
                    isRunningSequence.set(false);
                    currentSequence = null;
                    currentSequenceThread = null;
                }
            }, "DupeSequence-" + sequence.getName());

            currentSequenceThread = sequenceThread;
            sequenceThread.setDaemon(true);
            sequenceThread.start();
        } finally {
            executionLock.unlock();
        }
    }

    private static void executeSequenceInternal(DupeSequence sequence, int repeatCount) throws InterruptedException {
        for (int cycle = 0; cycle < repeatCount && isRunningSequence.get(); cycle++) {
            if (sequence.isAllAtOnce()) {
                // Execute all non-wait actions at once
                for (SequenceAction action : sequence.getActions()) {
                    if (!isRunningSequence.get()) {
                        break;
                    }
                    
                    if (action.getType() != ActionType.WAIT) {
                        executeActionSafely(action);
                    }
                }
            } else {
                // Execute actions sequentially with delays
                for (SequenceAction action : sequence.getActions()) {
                    if (!isRunningSequence.get()) {
                        break;
                    }

                    if (action.getType() == ActionType.WAIT) {
                        int waitTicks = parseWaitTicks(action.getData());
                        waitTicks(waitTicks);
                    } else {
                        executeActionSafely(action);
                        
                        // Add delay between actions if specified
                        int delay = sequence.getDelayBetweenActions();
                        if (delay > 0) {
                            waitTicks(delay);
                        }
                    }
                }
            }

            // Add delay between cycles (except for the last one)
            if (isRunningSequence.get() && cycle < repeatCount - 1) {
                waitTicks(10); // 0.5 second delay between cycles
            }
        }
    }

    private static int parseWaitTicks(String data) {
        try {
            return Math.max(0, Integer.parseInt(data));
        } catch (NumberFormatException e) {
            return 20; // Default to 1 second
        }
    }

    private static void waitTicks(int ticks) throws InterruptedException {
        if (ticks <= 0) return;
        
        long waitTimeMs = ticks * TICK_DURATION_MS;
        // Fixed System.currentTimeMillis() references
        long endTime = java.lang.System.currentTimeMillis() + waitTimeMs;
        
        while (java.lang.System.currentTimeMillis() < endTime && isRunningSequence.get()) {
            Thread.sleep(Math.min(10, endTime - java.lang.System.currentTimeMillis()));
        }
        
        if (!isRunningSequence.get()) {
            throw new InterruptedException("Sequence stopped");
        }
    }

    private static void executeActionSafely(SequenceAction action) {
        try {
            MinecraftClient.getInstance().execute(() -> {
                try {
                    executeAction(action);
                } catch (Exception e) {
                    // Fixed System.err reference
                    java.lang.System.out.println("Error executing action: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            // Fixed System.err reference
            java.lang.System.out.println("Error scheduling action: " + e.getMessage());
        }
    }

    private static void executeAction(SequenceAction action) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        if (mc.player == null) {
            return;
        }

        switch (action.getType()) {
            case COMMAND:
                executeCommandAction(mc, action);
                break;
            case PACKET:
                executePacketAction(mc, action);
                break;
            case CLOSE_GUI:
                executeCloseGuiAction(mc, action);
                break;
            default:
                // Fixed System.err reference
                java.lang.System.out.println("Unknown action type: " + action.getType());
                break;
        }
    }

    private static void executeCommandAction(MinecraftClient mc, SequenceAction action) {
        String cmd = action.getData();
        if (cmd == null || cmd.trim().isEmpty()) {
            return;
        }
        
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }
        
        if (mc.player.networkHandler != null) {
            mc.player.networkHandler.sendCommand(cmd);
        }
    }

    private static void executePacketAction(MinecraftClient mc, SequenceAction action) {
        if (mc.player.currentScreenHandler == null || mc.interactionManager == null) {
            return;
        }

        int slot = action.getSlot();
        int repeatCount = Math.max(1, action.getRepeatCount());
        
        for (int i = 0; i < repeatCount && isRunningSequence.get(); i++) {
            try {
                mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    slot,
                    0,
                    action.getSlotActionType(),
                    mc.player
                );
            } catch (Exception e) {
                // Fixed System.err reference
                java.lang.System.out.println("Error clicking slot: " + e.getMessage());
                break;
            }
        }
    }

    private static void executeCloseGuiAction(MinecraftClient mc, SequenceAction action) {
        if (mc.currentScreen == null) {
            return;
        }

        String closeType = action.getData();
        if ("desync".equals(closeType)) {
            mc.player.closeScreen();
        } else {
            mc.player.closeHandledScreen();
        }
    }
}