/**
 * Created: 2/3/2025
 */
package com.chorus.api.command;

import com.chorus.api.command.exception.CommandException;
import com.chorus.impl.commands.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class CommandManager {
    private String prefix = ".";
    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
    private final CommandSource commandSource = new ChatCommandSource(MinecraftClient.getInstance());
    private final CommandRepository repository = new CommandRepository(dispatcher);

    private static final List<Class<? extends BaseCommand>> COMMAND_REGISTRATIONS = Arrays.asList(
            PrefixCommand.class,
            ConfigCommand.class,
            BindCommand.class,
            FriendCommand.class,
            ToggleCommand.class
    );

    public void init() {
        registerCommands();
    }

    private void registerCommands() {
        COMMAND_REGISTRATIONS.forEach(commandClass -> {
            try {
                BaseCommand command = commandClass.getDeclaredConstructor().newInstance();
                repository.register(command);
            } catch (Exception e) {
                throw new CommandException("Failed to initialize command: " + commandClass.getSimpleName(), e);
            }
        });
    }

    public void registerCommand(Class<? extends BaseCommand> commandClass) {
        try {
            BaseCommand command = commandClass.getDeclaredConstructor().newInstance();
            repository.register(command);
        } catch (Exception e) {
            throw new CommandException("Failed to register command: " + commandClass.getSimpleName(), e);
        }
    }

    public void dispatch(String message) throws CommandSyntaxException {
        ParseResults<CommandSource> results = dispatcher.parse(message, commandSource);
        dispatcher.execute(results);
    }

    private static final class ChatCommandSource extends ClientCommandSource {
        public ChatCommandSource(MinecraftClient client) {
            super(null, client);
        }
    }
}