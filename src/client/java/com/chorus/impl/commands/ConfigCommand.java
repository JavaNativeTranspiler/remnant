/**
 * Created: 2/3/2025
 */

package com.chorus.impl.commands;

import chorus0.Chorus;
import com.chorus.api.command.BaseCommand;
import com.chorus.api.command.CommandInfo;
import com.chorus.common.util.player.ChatUtils;
import com.chorus.core.client.config.ConfigManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import java.util.List;

import static net.minecraft.command.CommandSource.suggestMatching;

@CommandInfo(
        name = "config",
        description = "Manage configuration profiles",
        aliases = {"cfg", "profile"}
)
public class ConfigCommand extends BaseCommand {

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        ConfigManager cfg = Chorus.getInstance().getConfigManager();

        builder.then(literal("load")
                        .then(argument("profile", StringArgumentType.string())
                                .suggests((context, suggestionsBuilder) ->
                                        suggestMatching(cfg.getProfileNames(), suggestionsBuilder))
                                .executes(context -> {
                                    String profile = context.getArgument("profile", String.class);
                                    if (cfg.profileExists(profile)) {
                                        cfg.loadProfile(profile);
                                        ChatUtils.addChatMessage("Loaded profile: " + profile);
                                    } else {
                                        ChatUtils.addChatMessage("§cProfile not found: " + profile);
                                    }
                                    return SINGLE_SUCCESS;
                                })))
                .then(literal("save")
                        .executes(context -> {
                            cfg.saveCurrentProfile();
                            ChatUtils.addChatMessage("Saved current profile");
                            return SINGLE_SUCCESS;
                        })
                        .then(argument("profile", StringArgumentType.string())
                                .executes(context -> {
                                    String profile = context.getArgument("profile", String.class);
                                    cfg.saveProfile(profile);
                                    ChatUtils.addChatMessage("Saved to profile: " + profile);
                                    return SINGLE_SUCCESS;
                                })))
                .then(literal("create")
                        .then(argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    String name = context.getArgument("name", String.class);
                                    cfg.createProfile(name);
                                    ChatUtils.addChatMessage("Created profile: " + name);
                                    return SINGLE_SUCCESS;
                                })))
                .then(literal("delete")
                        .then(argument("profile", StringArgumentType.string())
                                .suggests((context, suggestionsBuilder) ->
                                        suggestMatching(cfg.getProfileNames(), suggestionsBuilder))
                                .executes(context -> {
                                    String profile = context.getArgument("profile", String.class);
                                    if (cfg.profileExists(profile)) {
                                        cfg.deleteProfile(profile);
                                        ChatUtils.addChatMessage("Deleted profile: " + profile);
                                    } else {
                                        ChatUtils.addChatMessage("§cProfile not found: " + profile);
                                    }
                                    return SINGLE_SUCCESS;
                                })))
                .then(literal("list")
                        .executes(context -> {
                            List<String> profiles = cfg.getProfileNames();
                            ChatUtils.addChatMessage("Available profiles:");
                            profiles.forEach(p ->
                                    ChatUtils.addChatMessage(
                                            (p.equals(cfg.getActiveProfile()) ? "§a> " : "§7- ") + p
                                    ));
                            return SINGLE_SUCCESS;
                        }))
                .then(literal("reload")
                        .executes(context -> {
                            cfg.reloadActiveProfile();
                            ChatUtils.addChatMessage("Reloaded current profile");
                            return SINGLE_SUCCESS;
                        }));
    }
}
