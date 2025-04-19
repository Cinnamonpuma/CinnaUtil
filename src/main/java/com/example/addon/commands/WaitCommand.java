package com.example.addon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

import java.util.Timer;
import java.util.TimerTask;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class WaitCommand extends Command {
    public WaitCommand() {
        super("wait", "Waits for the specified time (in milliseconds) before sending the given command as a chat message.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("time", IntegerArgumentType.integer(1))
            .then(argument("command", StringArgumentType.greedyString())
                .executes(context -> {
                    int time = context.getArgument("time", Integer.class);
                    String command = context.getArgument("command", String.class);

                    new CommandExecutor(time, command);
                    return SINGLE_SUCCESS;
                })
            )
        );
    }

    private static class CommandExecutor {
        private final int time;
        private final String command;

        public CommandExecutor(int time, String command) {
            this.time = time;
            this.command = command;

            executeWithDelay();
        }

        private void executeWithDelay() {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // Send the command as a chat message
                    ChatUtils.sendPlayerMsg(command);
                }
            }, time);
        }
    }
}
