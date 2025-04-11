/**
 * Created: 2/16/2025
 */

package com.chorus.impl.events.input;

import cc.polymorphism.eventbus.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KeyPressEvent extends Event {
    private final int key;
    private final int scancode;
    private final int action;
    private final int modifiers;
}