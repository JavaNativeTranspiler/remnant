/**
 * Created: 2/3/2025
 */
package com.chorus.api.command;

import chorus0.Chorus;
import com.chorus.api.command.exception.CommandException;
import com.chorus.common.QuickImports;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import lombok.Getter;
import net.minecraft.command.CommandSource;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public abstract class BaseCommand implements QuickImports {
    private final CommandInfo commandInfo;
    public static final int SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;

    public BaseCommand() {
        this.commandInfo = getClass().getAnnotation(CommandInfo.class);
        if (commandInfo == null) {
            throw new CommandException("Command missing @CommandInfo annotation: " + getClass().getName());
        }
    }

    public abstract void build(LiteralArgumentBuilder<CommandSource> builder);

    public void registerTo(CommandDispatcher<CommandSource> dispatcher) {
        register(dispatcher, commandInfo.name());
        for (String alias : commandInfo.aliases()) {
            register(dispatcher, alias);
        }
    }

    protected void register(CommandDispatcher<CommandSource> dispatcher, String name) {
        LiteralArgumentBuilder<CommandSource> builder = literal(name);
        build(builder);
        dispatcher.register(builder);
    }

    protected static <T> RequiredArgumentBuilder<CommandSource, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static String nameToTitle(String name) {
        return Arrays.stream(name.split("-")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    @Override
    public String toString() {
        return Chorus.getInstance().getCommandManager().getPrefix() + commandInfo.name();
    }

    public String toString(String... args) {
        StringBuilder base = new StringBuilder(toString());
        for (String arg : args)
            base.append(' ').append(arg);

        return base.toString();
    }
}