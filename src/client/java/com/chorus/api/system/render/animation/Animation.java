package com.chorus.api.system.render.animation;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ExcludeConstant
@ExcludeFlow
public class Animation {

    private final AnimationTimer stopwatch = new AnimationTimer();
    private EasingType easing;

    private long duration;
    private double startPoint, endPoint, value;

    private boolean finished;

    public Animation(final EasingType easing, final long duration) {
        this.easing = easing;
        this.duration = duration;
    }

    public void run(final double endPoint) {
        if (this.endPoint != endPoint) {
            this.endPoint = endPoint;
            this.reset();
        } else {
            this.finished = stopwatch.finished(duration);

            if (this.finished) {
                this.value = endPoint;
                return;
            }
        }

        final double newValue = this.easing.getFunction().apply(this.getProgress());

        if (this.value > endPoint) {
            this.value = this.startPoint - (this.startPoint - endPoint) * newValue;
        } else {
            this.value = this.startPoint + (endPoint - this.startPoint) * newValue;
        }
    }

    public double getProgress() {
        return (double) (System.currentTimeMillis() - this.stopwatch.getLastMS()) / (double) this.duration;
    }

    public void reset() {
        this.stopwatch.reset();
        this.startPoint = value;
        this.startPoint = 0;
        this.finished = false;
    }

    public void restart() {
        this.stopwatch.reset();
        this.startPoint = 0.0F;
        this.finished = false;
    }
}
