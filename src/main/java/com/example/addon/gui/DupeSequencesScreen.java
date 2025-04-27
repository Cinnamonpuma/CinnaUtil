package com.example.addon.gui;

import com.example.addon.systems.DupeSequence;
import com.example.addon.systems.DupeSystem;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import net.minecraft.client.MinecraftClient;

public class DupeSequencesScreen extends WindowScreen {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public DupeSequencesScreen(GuiTheme theme) {
        super(theme, "Dupe Sequences");
    }

    public void initWidgets() {
        WTable table = add(theme.table()).expandX().widget();


        WButton newBtn = table.add(theme.button("New Sequence")).expandX().widget();
        newBtn.action = () -> {
            DupeSequence sequence = new DupeSequence("New Sequence");
            DupeSystem.get().add(sequence);
            mc.setScreen(new EditDupeSequenceScreen(theme, sequence));
        };


        if (DupeSystem.isRunningSequence) {
            WButton stopBtn = table.add(theme.button("Stop Sequence")).expandX().widget();
            stopBtn.action = () -> {
                DupeSystem.stopCurrentSequence();
                clear();
                initWidgets();
            };
        }

        table.row();

        fillTable(table);
    }

    private void fillTable(WTable table) {
        for (DupeSequence sequence : DupeSystem.get()) {
            table.add(theme.label(sequence.getName())).expandX();

            WButton editBtn = table.add(theme.button("Edit")).widget();
            editBtn.action = () -> mc.setScreen(new EditDupeSequenceScreen(theme, sequence));

            WButton runBtn = table.add(theme.button("Run")).widget();
            runBtn.action = () -> runSequence(sequence);

            WButton deleteBtn = table.add(theme.button("Delete")).widget();
            deleteBtn.action = () -> {
                DupeSystem.get().remove(sequence);
                clear();
                initWidgets();
            };

            table.row();
        }
    }

    private void runSequence(DupeSequence sequence) {
        DupeSystem.executeSequence(sequence, sequence.getRepeatCount());
        clear();
        initWidgets();
    }
}
