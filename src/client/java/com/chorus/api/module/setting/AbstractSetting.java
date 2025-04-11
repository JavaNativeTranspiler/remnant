/**
* Created: 3/3/2025
*/

package com.chorus.api.module.setting;

import java.util.function.Supplier;

public abstract class AbstractSetting<T> implements Setting<T> {
    private Supplier<Boolean> renderCondition = () -> true;

    @Override
    public Supplier<Boolean> getRenderCondition() {
        return renderCondition;
    }

    @Override
    public void setRenderCondition(Supplier<Boolean> condition) {
        this.renderCondition = condition;
    }
}