
/**
 * Created: 12/8/2024
 */
package com.chorus.api.repository.team;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TeamRepository {
    public Team currentTeam;

    public void addMemberToCurrentTeam(String member) {
        if (currentTeam != null) {
            currentTeam.addMember(member);
        }
    }

    public void removeMemberFromCurrentTeam(String member) {
        if (currentTeam != null) {
            currentTeam.removeMember(member);
        }
    }
    public void clear() {
        if (currentTeam == null) return;
        currentTeam.clear();
    }
    public List<String> getCurrentTeamMembers() {
        return (currentTeam != null)
               ? currentTeam.getMembers()
               : new ArrayList<>();
    }

    public boolean isMemberOfCurrentTeam(String member) {
        return (currentTeam != null) && currentTeam.isMember(member);
    }

    public void setTeam(String name) {
        if ((currentTeam == null) ||!currentTeam.getName().equalsIgnoreCase(name)) {
            currentTeam = new Team(name);
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
