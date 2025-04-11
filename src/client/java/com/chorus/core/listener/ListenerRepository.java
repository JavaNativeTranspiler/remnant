/**
* Created: 2/8/2025
*/

package com.chorus.core.listener;

import chorus0.Chorus;
import com.chorus.core.listener.impl.KeyPressEventListener;
import com.chorus.core.listener.impl.TickEventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListenerRepository {
    final List<Listener> listeners = new ArrayList<>();

    public void setup() {
        registerListeners(
                new TickEventListener(),
                new KeyPressEventListener()
        );
    }

    public void registerListeners(Listener... listeners) {
        this.listeners.addAll(List.of(listeners));
        Arrays.stream(listeners).forEach(listener -> Chorus.getInstance().getEventManager().register(listener));
    }
}