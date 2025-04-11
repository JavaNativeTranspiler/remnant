
/**
 * Created: 12/15/2024
 */
package cc.polymorphism.eventbus;

import chorus0.Chorus;
import lombok.Getter;
import lombok.Setter;

import java.util.ConcurrentModificationException;

@Getter
@Setter
public class Event {
    private boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }

    public boolean equals(Class<? extends Event> eventClass) {
        return this.getClass() == eventClass;
    }

    @SuppressWarnings("unchecked")
    public <T> T run() {
        try {
            Chorus.getInstance().getEventManager().post(this);
        } catch (ConcurrentModificationException ignored) {}

        return (T) this;
    }
}