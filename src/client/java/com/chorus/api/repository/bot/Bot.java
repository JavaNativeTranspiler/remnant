
/**
 * Created: 1/18/2025
 */
package com.chorus.api.repository.bot;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@ToString
class Bot {
    private final UUID uuid;
    @Getter
    private final String name;
    private final Set<BotRepository.Flag> flags;

    public Bot(UUID uuid, String name, Set<BotRepository.Flag> flags) {
        this.uuid = uuid;
        this.name = name;
        this.flags = flags;
    }


    public void addFlags(BotRepository.Flag... flagsToAdd) {
        this.flags.addAll(Arrays.asList(flagsToAdd));
    }

    public boolean hasFlags(BotRepository.Flag... flagsToCheck) {
        return this.flags.containsAll(Arrays.asList(flagsToCheck));
    }
    public boolean hasFlags(List<BotRepository.Flag> flags) {
        return this.flags.containsAll(flags);
    }

}


//~ Formatted by Jindent --- http://www.jindent.com
