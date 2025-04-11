package com.chorus.api.system.render.animation;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ExcludeConstant
@ExcludeFlow
public class AnimationTimer {
    private long lastMS;

    public AnimationTimer() {
        reset();
    }

    public boolean finished(final long delay) {
        return System.currentTimeMillis() - delay >= lastMS;
    }

    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }

    public void reset() {
        this.lastMS = System.currentTimeMillis();
    }
}
