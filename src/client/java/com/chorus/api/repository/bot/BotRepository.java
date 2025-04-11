
/**
 * Created: 1/18/2025
 */
package com.chorus.api.repository.bot;

import lombok.Getter;

import java.util.*;

@Getter
public class BotRepository {
    private final List<Bot> npcs = new ArrayList<>();
    private final List<Bot> players = new ArrayList<>();

    public enum Flag {
        MOVED,
        TOUCHED_GROUND,
        TOUCHED_AIR,
        SWUNG,
        DAMAGED,
        ROTATED
    }

    public void registerPlayer(String name, UUID uuid, Flag... flags) {
        Set<Flag> flagSet = new HashSet<>(Arrays.asList(flags));
        if (players.stream().noneMatch(player -> player.getName().equals(name))) {
            players.add(new Bot(uuid, name, flagSet));
        }
    }

    public boolean isRegistered(String name) {
        return players.stream().anyMatch(player -> player.getName().equals(name));
    }

    public void registerNPC(String name, UUID uuid, Flag... flags) {
        Set<Flag> flagSet = new HashSet<>(Arrays.asList(flags));
        if (npcs.stream().noneMatch(npc -> npc.getName().equals(name))) {
            npcs.add(new Bot(uuid, name, flagSet));
        }
    }

    public boolean isNPC(String name) {
        return npcs.stream().anyMatch(npc -> npc.getName().equals(name));
    }

    public void addFlags(String name, Flag... flags) {
        for (Flag flag : flags) {
            players.stream().filter(player -> player.getName().equals(name) && !player.hasFlags(flag))
                    .forEach(player -> player.addFlags(flag));
            npcs.stream().filter(npc -> npc.getName().equals(name) && !npc.hasFlags(flag))
                    .forEach(npc -> npc.addFlags(flag));
        }
    }

    public void meetsCriteria(List<Flag> criteria) {
        List<Bot> makePlayer = new ArrayList<>(), makeNPC = new ArrayList<>();

        for (Bot player : players) {
            if (!player.hasFlags(criteria) && !npcs.contains(player)) {
                makeNPC.add(player);
            }
        }

        for (Bot npc : npcs) {
            if (npc.hasFlags(criteria) && !players.contains(npc)) {
                makePlayer.add(npc);
            }
        }

        npcs.addAll(makeNPC); npcs.removeAll(makePlayer);
    }

    public void clear(boolean NPCS, boolean Players) {
        if (Players) players.clear();
        if (NPCS) npcs.clear();
    }

}


//~ Formatted by Jindent --- http://www.jindent.com
