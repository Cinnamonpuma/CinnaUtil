package com.example.addon.commands;

import com.example.addon.modules.ChatSyncModule;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;

public class ChatSyncCmd extends Command {
    public ChatSyncCmd() {
        super("chatsync", "Controls the chat sync module");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("send").executes(context -> {
            ChatSyncModule module = Modules.get().get(ChatSyncModule.class);
            if (module == null) {
                error("Module not found!");
                return SINGLE_SUCCESS;
            }

            if (!module.isActive()) {
                module.toggle();
                info("Enabled Chat Sync module");
            }

            module.sendMessage();
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("toggle").executes(context -> {
            ChatSyncModule module = Modules.get().get(ChatSyncModule.class);
            if (module == null) {
                error("Module not found!");
                return SINGLE_SUCCESS;
            }

            module.toggle();
            info(module.isActive() ? "Enabled Chat Sync module" : "Disabled Chat Sync module");
            return SINGLE_SUCCESS;
        }));
    }
}
