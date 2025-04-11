package cc.polymorphism.eventbus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class CancellableEvent {
    private boolean cancelled;
}
