
/**
 * Created: 12/8/2024
 */
package com.chorus.api.repository.friend;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class FriendRepository {
    private final List<Friend> friends = new ArrayList<>();

    public boolean addFriend(Friend friend) {
        if (!friends.contains(friend)) {
            return friends.add(friend);
        }

        return false;
    }

    public boolean removeFriend(UUID id) {
        return friends.removeIf(friend -> friend.getId().equals(id));
    }
    public boolean removeFriend(String name) {
        return friends.removeIf(friend -> friend.getName().equals(name));
    }

    public void clear() {
        friends.clear();
    }

    public List<UUID> getAllFriends() {
        List<UUID> friendIds = new ArrayList<>();

        for (Friend friend : friends) {
            friendIds.add(friend.getId());
        }

        return friendIds;
    }

    public boolean isFriend(UUID id) {
        return friends.stream().anyMatch(friend -> friend.getId().equals(id));
    }
    public boolean isFriend(String name) {
        return friends.stream().anyMatch(friend -> friend.getName().equals(name));
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
