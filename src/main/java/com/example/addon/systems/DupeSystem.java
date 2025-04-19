package com.example.addon.systems;

import meteordevelopment.meteorclient.systems.System;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DupeSystem extends System<DupeSystem> implements Iterable<DupeSequence> {
    // Singleton instance
    public static DupeSystem INSTANCE;

    // List to store all dupe sequences
    private List<DupeSequence> sequences = new ArrayList<>();

    // Constructor
    public DupeSystem() {
        super("dupe");
    }

    // Static getter for the singleton instance
    public static DupeSystem get() {
        if (INSTANCE == null) {
            INSTANCE = new DupeSystem();
        }
        return INSTANCE;
    }

    // Add a new sequence
    public void add(DupeSequence sequence) {
        sequences.add(sequence);
        save();
    }

    // Remove a sequence
    public void remove(DupeSequence sequence) {
        sequences.remove(sequence);
        save();
    }

    // Save data to NBT
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

    // Load data from NBT
    @Override
    public DupeSystem fromTag(NbtCompound tag) {
        sequences.clear();

        if (tag.contains("sequences")) {
            NbtList sequencesTag = tag.getList("sequences").orElse(new NbtList());

            for (int i = 0; i < sequencesTag.size(); i++) {
                NbtCompound sequenceTag = sequencesTag.getCompound(i).orElse(new NbtCompound());
                DupeSequence sequence = new DupeSequence();
                sequence.fromTag(sequenceTag);
                sequences.add(sequence);
            }
        }

        return this;
    }

    @Override
    public Iterator<DupeSequence> iterator() {
        return sequences.iterator();
    }

    public static boolean isRunningSequence = false;
    public static Thread currentSequenceThread = null;

    public static void stopCurrentSequence() {
        isRunningSequence = false;
        if (currentSequenceThread != null) {
            currentSequenceThread.interrupt();
            currentSequenceThread = null;
        }
    }
}
