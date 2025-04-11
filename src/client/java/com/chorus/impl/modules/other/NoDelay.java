
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.other;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.player.AttackEvent;
import com.chorus.impl.events.player.BlockBreakingEvent;
import com.chorus.impl.events.player.SwingEvent;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@ModuleInfo(
        name = "NoDelay",
        description = "Removes Delays",
        category = ModuleCategory.OTHER
)
public class NoDelay extends BaseModule implements QuickImports {

    private final SettingCategory missDelays = new SettingCategory("Swing Miss Delays");
    private final SettingCategory movementDelays = new SettingCategory("Movement Delays");
    private final SettingCategory itemUseDelays = new SettingCategory("Use Item Delays");
    private final SettingCategory miscellaneousDelays = new SettingCategory("Miscellaneous Delays");


    public final BooleanSetting attackMissDelay = new BooleanSetting(missDelays, "Attack Miss", "Removes Attack Miss Delay", false);
    public final BooleanSetting miningMissDelay = new BooleanSetting(missDelays, "Mining Miss", "Removes Mining Miss Delay", false);
    public final BooleanSetting weaponOnly = new BooleanSetting(missDelays, "Only Weapons", "Only Removes Delay With Weapons", false);

    public final BooleanSetting elytraDelay = new BooleanSetting(movementDelays, "Elytra Delay", "Removes Elytra Flying Delay ", false);
    public final BooleanSetting jumpDelay = new BooleanSetting(movementDelays, "Jump Delay", "Sets Jump Delay", false);

    public final NumberSetting<Integer> shieldDelay = new NumberSetting<>(itemUseDelays, "Shield Delay", "Sets Shield Delay", 5, 0, 5);

    public final BooleanSetting crystalDelay = new BooleanSetting(miscellaneousDelays, "Crystal Explode Delay", "Removes Crystal Explode Delay ", false);
    public final NumberSetting<Integer> miningDelay = new NumberSetting<>(miscellaneousDelays, "Mining Delay", "Removes Mining Delay ", 5, 0, 5);

    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null) return;

        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (!elytraDelay.getValue()) return;

            // Elytra Delay

            boolean hasElytra = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA);
            if (!hasElytra) {
                mc.player.stopGliding();
            }
        }
    }

    @RegisterEvent
    private void BlockBreakingEventListener(BlockBreakingEvent event) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null) return;

        if (event.getMode().equals(BlockBreakingEvent.Mode.PRE)) {
            if (!miningMissDelay.getValue()) return;

            // Mining Miss Delay
            if (weaponOnly.getValue()
                    && !(mc.player.getMainHandStack().getItem() instanceof SwordItem
                    || mc.player.getMainHandStack().getItem() instanceof TridentItem
                    || mc.player.getMainHandStack().getItem() instanceof MaceItem)) {
                return;
            }
            event.setCancelled(mc.crosshairTarget.getType() == HitResult.Type.BLOCK);
        }
    }

    @RegisterEvent
    private void SwingEventListener(SwingEvent event) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null) return;

        if (event.getMode().equals(SwingEvent.Mode.PRE)) {
            if (!attackMissDelay.getValue()) return;

            // Attack Miss Delay
            if (weaponOnly.getValue()
                    && !(mc.player.getMainHandStack().getItem() instanceof SwordItem
                    || mc.player.getMainHandStack().getItem() instanceof TridentItem
                    || mc.player.getMainHandStack().getItem() instanceof MaceItem)) {
                return;
            }
            event.setCancelled(!(mc.crosshairTarget instanceof EntityHitResult));
        }
    }

    @RegisterEvent
    private void AttackEventListener(AttackEvent event) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null) return;

        if (event.getMode().equals(AttackEvent.Mode.POST)) {
            if (!crystalDelay.getValue()) return;

            // Crystal Delay

            if (mc.crosshairTarget instanceof EntityHitResult entityHitResult) {
                Entity entity = entityHitResult.getEntity();
                if (entity instanceof EndCrystalEntity && entity.isAlive()) {
                    entity.kill((ServerWorld) entity.getWorld());
                    entity.remove(Entity.RemovalReason.KILLED);
                    entity.onRemoved();
                }
            }
        }
    }

    public NoDelay() {
        getSettingRepository().registerSettings(
                missDelays,
                movementDelays,
                itemUseDelays,
                miscellaneousDelays,
                attackMissDelay,
                miningMissDelay,
                weaponOnly,
                elytraDelay,
                jumpDelay,
                shieldDelay,
                crystalDelay,
                miningDelay);
    }
}
