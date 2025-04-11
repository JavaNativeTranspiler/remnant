package com.chorus.api.system.render.animation;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@Getter
@AllArgsConstructor
@ExcludeConstant
@ExcludeFlow
public enum EasingType {
    LINEAR(x -> x);

    private final Function<Double, Double> function;
}
