/**
 * Created: 2/16/2025
 */

package com.chorus.impl.events.misc;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSlowdownEvent extends Event {
    private final Mode mode;
    public enum Mode { PRE, POST }

    public WebSlowdownEvent(WebSlowdownEvent.Mode mode) {
        this.mode = mode;
    }
}