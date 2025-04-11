package com.chorus.api.system.render.font;

import cc.polymorphism.annot.ExcludeFlow;
import lombok.Getter;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;

@Getter
@ExcludeFlow
public class Fonts implements SimpleSynchronousResourceReloadListener {

    private FontAtlas interBold, interSemiBold, interMedium, proggyClean, poppins, icons, lucide;

    public Fonts() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this);
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("chorus", "reload_fonts");
    }

    @Override
    public void reload(final ResourceManager manager) {
        try {
            this.interBold = new FontAtlas(manager, "inter-bold");
            this.interMedium = new FontAtlas(manager, "inter-medium");
            this.interSemiBold = new FontAtlas(manager, "inter-semibold");
            this.proggyClean = new FontAtlas(manager, "proggy-clean");
            this.poppins = new FontAtlas(manager, "poppins");
            this.icons = new FontAtlas(manager, "icons");
            this.lucide = new FontAtlas(manager, "lucide");
        } catch (final IOException exception) {
            throw new RuntimeException("Couldn't load fonts", exception);
        }
    }
}
