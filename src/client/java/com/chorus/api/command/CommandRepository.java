/**
 * Created: 2/3/2025
 */
package com.chorus.api.command;

import com.chorus.api.command.exception.CommandException;
import com.mojang.brigadier.CommandDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class CommandRepository {
    private final Map<String, BaseCommand> commands = new HashMap<>();
    private final CommandDispatcher<CommandSource> dispatcher;

    public void register(BaseCommand command) {
        CommandInfo info = command.getCommandInfo();

        if (commands.containsKey(info.name())) {
            throw new CommandException("Duplicate command registration: " + info.name());
        }

        commands.put(info.name(), command);
        command.registerTo(dispatcher);

        for (String alias : info.aliases()) {
            commands.put(alias, command);
        }

        log.info("Registered command: {} with aliases: {}", info.name(), Arrays.toString(info.aliases()));
    }
}
