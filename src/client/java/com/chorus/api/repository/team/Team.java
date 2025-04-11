
/**
 * Created: 12/8/2024
 */
package com.chorus.api.repository.team;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Team {
    private final String name;
    private final List<String> members;

    public Team(String name) {
        this.name    = name;
        this.members = new ArrayList<>();
    }

    public void addMember(String member) {
        if (!members.contains(member)) {
            members.add(member);
        }
    }

    public void removeMember(String member) {
        members.remove(member);
    }
    public void clear() {
        members.clear();
    }
    public boolean isMember(String member) {
        return members.contains(member);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
