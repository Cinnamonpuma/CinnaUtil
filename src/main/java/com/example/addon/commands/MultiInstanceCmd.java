package com.example.addon.commands;

import com.example.addon.modules.MultiInstanceCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;

public class MultiInstanceCmd extends Command {
    public MultiInstanceCmd() {
        super("multidupe", "Controls the multi-instance dupe module");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("start").executes(context -> {
            MultiInstanceCommand module = Modules.get().get(MultiInstanceCommand.class);
            if (module == null) {
                error("Module not found!");
                return SINGLE_SUCCESS;
            }

            if (!module.isActive()) {
                module.toggle();
                info("Enabled Multi-Instance Dupe module");
            }

            module.startCommandExecution();
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("stop").executes(context -> {
            MultiInstanceCommand module = Modules.get().get(MultiInstanceCommand.class);
            if (module == null) {
                error("Module not found!");
                return SINGLE_SUCCESS;
            }

            module.stopCommandExecution();
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("toggle").executes(context -> {
            MultiInstanceCommand module = Modules.get().get(MultiInstanceCommand.class);
            if (module == null) {
                error("Module not found!");
                return SINGLE_SUCCESS;
            }

            module.toggle();
            info(module.isActive() ? "Enabled Multi-Instance Dupe module" : "Disabled Multi-Instance Dupe module");
            return SINGLE_SUCCESS;
        }));
    }
}
