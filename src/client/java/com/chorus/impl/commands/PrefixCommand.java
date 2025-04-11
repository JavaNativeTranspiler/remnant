/**
 * Created: 2/3/2025
 */
package com.chorus.impl.commands;

import chorus0.Chorus;
import com.chorus.api.command.BaseCommand;
import com.chorus.api.command.CommandInfo;
import com.chorus.common.util.player.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

@CommandInfo(
        name = "prefix",
        description = "Sets the command prefix.",
        aliases = {"p"}
)
public class PrefixCommand extends BaseCommand {

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("prefix", StringArgumentType.word())
                .executes(context -> {
                    String prefix = context.getArgument("prefix", String.class);
                    Chorus.getInstance().getCommandManager().setPrefix(prefix);
                    ChatUtils.addChatMessage("Changed prefix to " + prefix + ".");
                    return SINGLE_SUCCESS;
                }));
    }
}