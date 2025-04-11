/**
 * Created: 2/16/2025
 */

package com.chorus.impl.commands;

import chorus0.Chorus;
import com.chorus.api.command.BaseCommand;
import com.chorus.api.command.CommandInfo;
import com.chorus.api.module.Module;
import com.chorus.common.util.player.ChatUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.core.listener.impl.KeyPressEventListener;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.command.CommandSource.suggestMatching;

@CommandInfo(
        name = "bind",
        description = "Bind modules to keys",
        aliases = {"keybind", "key"}
)
public class BindCommand extends BaseCommand {

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(literal("set")
                        .then(argument("module", StringArgumentType.word())
                                .suggests((context, suggestionsBuilder) -> {
                                    List<String> moduleNames = Chorus.getInstance().getModuleManager().getModules()
                                            .stream()
                                            .map(module -> module.getName().replace(" ", ""))
                                            .collect(Collectors.toList());
                                    return suggestMatching(moduleNames, suggestionsBuilder);
                                })
                                .executes(context -> {
                                    String moduleName = context.getArgument("module", String.class);
                                    Module module = Chorus.getInstance().getModuleManager().getModuleByName(moduleName);
                                    if (module != null) {
                                        KeyPressEventListener.setModuleToBindTo(module);
                                        ChatUtils.sendFormattedMessage("Listening for key press for module: " + module.getName());
                                        notificationManager.addNotification("Listening...", "Listening for key press for module: " + module.getName(), 10000);
                                    } else {
                                        ChatUtils.sendFormattedMessage("Module not found: " + moduleName);
                                    }
                                    return SINGLE_SUCCESS;
                                })))
                .then(literal("clear")
                        .executes(context -> {
                            ChatUtils.sendFormattedMessage("Usage: .bind clear <module>");
                            return SINGLE_SUCCESS;
                        })
                        .then(argument("module", StringArgumentType.word())
                                .suggests((context, suggestionsBuilder) -> {
                                    List<String> moduleNames = Chorus.getInstance().getModuleManager().getModules()
                                            .stream()
                                            .map(module -> module.getName().replace(" ", ""))
                                            .collect(Collectors.toList());
                                    return suggestMatching(moduleNames, suggestionsBuilder);
                                })
                                .executes(context -> {
                                    String moduleName = context.getArgument("module", String.class);
                                    Module module = Chorus.getInstance().getModuleManager().getModuleByName(moduleName);
                                    if (module != null) {
                                        module.setKey(-1);
                                        ChatUtils.sendFormattedMessage("Cleared keybind for " + module.getName());
                                    } else {
                                        ChatUtils.sendFormattedMessage("Module not found: " + moduleName);
                                    }
                                    return SINGLE_SUCCESS;
                                })))
                .then(literal("list")
                        .executes(context -> {
                            List<Module> modules = Chorus.getInstance().getModuleManager().getModules();
                            ChatUtils.sendFormattedMessage("Module bindings:");
                            for (Module module : modules) {
                                int key = module.getKey();
                                if (key != -1) {
                                    ChatUtils.sendFormattedMessage(module.getName() + ": " + InputUtils.getKeyName(key));
                                }
                            }
                            return SINGLE_SUCCESS;
                        }));
    }
}