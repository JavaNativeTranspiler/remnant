
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.client;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.impl.events.player.TickEvent;

import java.util.Map;

@ModuleInfo(
    name        = "Capes",
    description = "Use Custom Minecraft Capes",
    category    = ModuleCategory.CLIENT
)
public class Capes extends BaseModule {

    private final ModeSetting capes = new ModeSetting("Capes", "Set which cape you want used", "Vanilla",
            "1 Million Customer",
            "15 Anniversary",
            "Big Shot",
            "Birthday",
            "Cherry Blossom",
            "Cobalt",
            "Debug",
            "Dyrk",
            "Follower",
            "MCC 15 Year",
            "MC Experience",
            "Migrator",
            "Minecon 2011",
            "Minecon 2012",
            "Minecon 2013",
            "Minecon 2015",
            "Minecon 2016",
            "Mojang",
            "Mojang Classic",
            "Mojang Office",
            "Mojang Studios",
            "Mojira Mod",
            "Prismarine",
            "Purple Heart",
            "Realm Maker",
            "Remnant",
            "Scrolls",
            "Snowman",
            "Spade",
            "Test",
            "Translator",
            "Valentines",
            "Vanilla");
    Map<String, String> capePaths = Map.ofEntries(
            Map.entry("1 Million Customer", "1millioncustomer"),
            Map.entry("15 Anniversary", "15anniversary"),
            Map.entry("Big Shot", "bigshot"),
            Map.entry("Birthday", "birthday"),
            Map.entry("Cherry Blossom", "cherryblossom"),
            Map.entry("Cobalt", "cobalt"),
            Map.entry("Debug", "debug"),
            Map.entry("Dyrk", "dyrk"),
            Map.entry("Follower", "follower"),
            Map.entry("MCC 15 Year", "mcc15year"),
            Map.entry("MC Experience", "mcexperience"),
            Map.entry("Migrator", "migrator"),
            Map.entry("Minecon 2011", "minecon2011"),
            Map.entry("Minecon 2012", "minecon2012"),
            Map.entry("Minecon 2013", "minecon2013"),
            Map.entry("Minecon 2015", "minecon2015"),
            Map.entry("Minecon 2016", "minecon2016"),
            Map.entry("Mojang", "mojang"),
            Map.entry("Mojang Classic", "mojangclassic"),
            Map.entry("Mojang Office", "mojangoffice"),
            Map.entry("Mojang Studios", "mojangstudios"),
            Map.entry("Mojira Mod", "mojiramod"),
            Map.entry("Prismarine", "prismarine"),
            Map.entry("Purple Heart", "purpleheart"),
            Map.entry("Realm Maker", "realmmaker"),
            Map.entry("Remnant", "remnant"),
            Map.entry("Scrolls", "scrolls"),
            Map.entry("Snowman", "snowman"),
            Map.entry("Spade", "spade"),
            Map.entry("Test", "test"),
            Map.entry("Translator", "translator"),
            Map.entry("Valentines", "valentines"),
            Map.entry("Vanilla", "vanilla")
    );
    public String cape = "vanilla";
    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        cape = capePaths.getOrDefault(capes.getValue(), "vanilla");
    }
    public Capes() {
        getSettingRepository().registerSettings(capes);
    }
}
