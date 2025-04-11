
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.other;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.events.player.AttackEvent;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.entity.player.PlayerEntity;

@ModuleInfo(
    name        = "Target",
    description = "Sets Focus On One Enemy",
    category    = ModuleCategory.OTHER
)
public class Target extends BaseModule implements QuickImports {
    public final NumberSetting<Float> updateTime = new NumberSetting<>("Enemy Update Time", "Sets Time Before An Enemy is removed (Seconds)", 10f, 0f, 50f);
    public PlayerEntity enemy = null;
    private final TimerUtils timer = new TimerUtils();
    @RegisterEvent
    private void TickEventListener(TickEvent event) {
       if (event.getMode().equals(TickEvent.Mode.PRE)) {
           if (timer.hasReached(1000 * updateTime.getValue())) {
               enemy = null;
           }
           if (enemy == null || mc.player == null)
               return;
           if (enemy.isDead() || enemy.getHealth() == 0 || mc.player.isDead())
               enemy = null;
       }
    }
    @RegisterEvent
    private void AttackEventListener(AttackEvent event) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null) return;
        if (event.getMode().equals(AttackEvent.Mode.PRE)) {
            if (event.getTarget().isPlayer() && InputUtils.mouseDown(0)) {
                enemy = (PlayerEntity) event.getTarget();
                timer.reset();
            }
        }
    }
    @Override
    protected void onModuleDisabled() {
        enemy = null;
    }
    @Override
    protected void onModuleEnabled() {

    }
    public Target() {
        getSettingRepository().registerSetting(updateTime);
    }
}
