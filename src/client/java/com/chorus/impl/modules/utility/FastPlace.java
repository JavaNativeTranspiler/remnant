
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.utility;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.InventoryUtils;
import com.chorus.impl.events.player.ItemUseCooldownEvent;
import net.minecraft.item.BlockItem;

@ModuleInfo(
    name        = "Fastplace",
    description = "Removes Place Speed Limit",
    category    = ModuleCategory.UTILITY
)
public class FastPlace extends BaseModule implements QuickImports {
    private final SettingCategory generalSettings = new SettingCategory("General Settings");
    private final NumberSetting<Integer> speed = new NumberSetting<>(generalSettings, "Speed", "", 4, 0, 4);
    private final BooleanSetting onlyBlocks = new BooleanSetting(generalSettings, "Only Blocks", "", true);
    @RegisterEvent
    private void itemUseCooldownEventEventListener(ItemUseCooldownEvent event) {
        if (onlyBlocks.getValue() && !(InventoryUtils.getMainHandItem().getItem() instanceof BlockItem || InventoryUtils.getOffHandItem().getItem() instanceof BlockItem)) return;
        event.setSpeed(speed.getValue());
    }
    public FastPlace() {
        getSettingRepository().registerSettings(generalSettings, speed, onlyBlocks);
    }
}
