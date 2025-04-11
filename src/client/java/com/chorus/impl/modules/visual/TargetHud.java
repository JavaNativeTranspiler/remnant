package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.font.FontAtlas;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.MathUtils;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.impl.events.render.Render2DEvent;
import com.chorus.impl.modules.other.Target;
import com.chorus.impl.screen.hud.HUDEditorScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

import java.awt.*;
import java.text.DecimalFormat;

@ModuleInfo(name = "TargetHUD", description = "Renders Target Information", category = ModuleCategory.VISUAL)
public class TargetHud extends BaseModule implements QuickImports {

    private final SettingCategory general = new SettingCategory("General");
    private final ModeSetting mode = new ModeSetting(general, "Mode", "Choose style", "Remnant Compact", "Adjust", "Remnant Compact", "Remnant Info");
    private final NumberSetting<Integer> xPos = new NumberSetting<>(general, "xPos", "Internal setting", 5, 0, 1920);
    private final NumberSetting<Integer> yPos = new NumberSetting<>(general, "yPos", "Internal setting", 5, 0, 1080);

    private final int ENEMY_TIMEOUT = 1000;
    private TimerUtils enemyTimer = new TimerUtils();
    public PlayerEntity enemy = null;
    
    private float animationProgress = 0.0f;
    private boolean wasEnemyNull = true;
    private static final float ANIMATION_SPEED = 0.035f;
    private boolean isDisappearing = false;
    private float disappearY = 0f;
    private PlayerEntity lastEnemy = null;
    private boolean forceReappearCheck = false;

    @RegisterEvent
    private void render2DListener(Render2DEvent event) {
        DrawContext context = event.getContext();
        MatrixStack matrices = context.getMatrices();
        
        boolean inHudEditor = mc.currentScreen instanceof HUDEditorScreen;

        PlayerEntity currentEnemy = Chorus.getInstance().getModuleManager().getModule(Target.class).enemy;
        boolean hasTargetEnemy = currentEnemy != null;
        boolean hasAttackingEnemy = mc.player != null && mc.player.getAttacking() instanceof PlayerEntity;
        boolean recentAttack = mc.player != null && mc.player.age - mc.player.getLastAttackTime() < 40;
        
        boolean shouldHaveEnemy = (hasTargetEnemy || hasAttackingEnemy) && recentAttack;
        
        if (forceReappearCheck) {
            shouldHaveEnemy = true;
            forceReappearCheck = false;
        }
        
        if (enemy != null && !shouldHaveEnemy && !isDisappearing) {
            isDisappearing = true;
            lastEnemy = enemy;
        }
        
        if (isDisappearing && animationProgress <= 0) {
            enemy = null;
            lastEnemy = null;
            isDisappearing = false;
            forceReappearCheck = true;
        }
        else if (shouldHaveEnemy) {
            if (isDisappearing) {
                isDisappearing = false;
                disappearY = 0f;
            }
            
            boolean updatedEnemy = false;
            
            if (hasAttackingEnemy && mc.player.getAttacking() instanceof PlayerEntity attackTarget) {
                if (attackTarget != enemy) {
                    enemy = attackTarget;
                    enemyTimer.reset();
                    totemPops = 0;
                    updatedEnemy = true;
                }
            }
            
            if (!updatedEnemy && hasTargetEnemy && currentEnemy != enemy) {
                enemy = currentEnemy;
                enemyTimer.reset();
                totemPops = 0;
                updatedEnemy = true;
            }
            
            if (updatedEnemy) {
                wasEnemyNull = true;
            }
        }

        boolean isEnemyNull = (enemy == null) && !inHudEditor;
        
        if (inHudEditor) {
            isDisappearing = false;
            disappearY = 0f;
            animationProgress = 1.0f;
        }
        else if (isEnemyNull != wasEnemyNull || (isEnemyNull && animationProgress > 0) || (!isEnemyNull && animationProgress < 1) || isDisappearing) {
            if (isEnemyNull || isDisappearing) {
                animationProgress = Math.max(0, animationProgress - ANIMATION_SPEED);
                disappearY = animationProgress > 0 ? (1f - animationProgress) * 100f : 0f;
            } else {
                isDisappearing = false;
                disappearY = 0f;
                animationProgress = Math.min(1, animationProgress + ANIMATION_SPEED);
            }
            wasEnemyNull = isEnemyNull;
        }
        
        if (animationProgress > 0 || inHudEditor) {
            PlayerEntity displayEnemy = enemy != null ? enemy : (isDisappearing ? lastEnemy : null);
            
            if (displayEnemy != null || inHudEditor) {
                switch (mode.getValue()) {
                    case "Adjust":
                        renderAdjust(matrices, context, displayEnemy);
                        break;
                    case "Remnant Compact":
                        renderRemnantCompact(matrices, context, displayEnemy);
                        break;
                    case "Remnant Info":
                        renderRemnantInfo(matrices, context, displayEnemy);
                        break;
                }
            }
        }

        if (enemy != null) {
            if (enemyTimer.delay(ENEMY_TIMEOUT)) {
                isDisappearing = true;
                lastEnemy = enemy;
            }
        }
    }
    public float firstHealth = 0, secondHealth = 0, absorption = 0, armorBar = 0, totemPops = 0;
    EquipmentSlot[] armorSlots = {
            EquipmentSlot.MAINHAND,
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.OFFHAND
    };
    
    private void renderAdjust(MatrixStack matrices, DrawContext context, PlayerEntity displayEnemy) {
        FontAtlas font = Chorus.getInstance().getFonts().getInterSemiBold();
        boolean resetToNull = false;
        if (displayEnemy == null) {
            if (mc.currentScreen instanceof HUDEditorScreen) {
                displayEnemy = mc.player;
                resetToNull = true;
            } else {
                return;
            }
        }

        String name = displayEnemy.getNameForScoreboard();
        float hpDiff = mc.player.getHealth() - displayEnemy.getHealth();
        DecimalFormat format = new DecimalFormat("#.##");
        String hp = (hpDiff < 0 ? "-" : "+") + format.format(Math.abs(hpDiff)) + "hp";
        Color themeColor = new Color(184, 112, 242, 255);
        Color background = new Color(0, 0, 0, 100);

        int height = 35, length = 105, size = 25;
        int x = xPos.getValue();
        int y = yPos.getValue();
        float padding = 3, centerPadding = padding * 2, thickness = 3f;
        float fullWidth = font.getWidth(name, 6) + length;

        matrices.push();
        
        float scale = 0.5f + (0.5f * animationProgress);
        float alpha = animationProgress;
        float scaleX = x + (fullWidth / 2);
        float scaleY = y + (height / 2);
        
        matrices.translate(scaleX, scaleY, 0);
        
        if (isDisappearing) {
            matrices.translate(0, disappearY, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(disappearY * 0.8f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(disappearY * 0.3f));
        }
        
        matrices.scale(scale, scale, 1.0f);
        matrices.translate(-scaleX, -scaleY, 0);
        
        background = new Color(background.getRed(), background.getGreen(), background.getBlue(), 
                              (int)(background.getAlpha() * alpha));
        themeColor = new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 
                              (int)(themeColor.getAlpha() * alpha));

        Render2DEngine.drawRect(matrices, x, y, fullWidth, height, background);
        setWidth(fullWidth);
        setHeight(height);
        
        if (alpha > 0.05f) {
            int adjustedSize = (int)(size * Math.min(1.0f, alpha * 1.2f));
            if (adjustedSize > 0) {
                PlayerSkinDrawer.draw(context, ((AbstractClientPlayerEntity) displayEnemy).getSkinTextures(), 
                    (int) (x + padding), (int) (y + padding), adjustedSize);
            }
        }
        
        font.render(matrices, name, x + size + centerPadding, y + padding, 10f, new Color(255, 255, 255, (int)(255 * alpha)).getRGB());
        font.render(matrices, hp, x + fullWidth - font.getWidth(hp, 7f) - padding, y + size - centerPadding, 7f, new Color(255, 255, 255, (int)(255 * alpha)).getRGB());

        if (firstHealth == 0 || firstHealth != displayEnemy.getHealth()) {
            firstHealth = MathUtils.lerp(firstHealth == 0 ? displayEnemy.getHealth() : firstHealth, displayEnemy.getHealth(), 0.05f);
            secondHealth = MathUtils.lerp(secondHealth == 0 ? displayEnemy.getHealth() : secondHealth, displayEnemy.getHealth(), 0.005f);
        }

        float firstWidth = (fullWidth - centerPadding) * firstHealth / displayEnemy.getMaxHealth();
        float secondWidth = (fullWidth - centerPadding) * secondHealth / displayEnemy.getMaxHealth();
        Render2DEngine.drawRect(matrices, x + padding, y + height - (thickness * 2), secondWidth, thickness, themeColor.darker().darker());
        Render2DEngine.drawRect(matrices, x + padding, y + height - (thickness * 2), firstWidth, thickness, themeColor);

        int armorX = (int) (x + size + centerPadding);
        int armorY = (int) (y + padding + 10);

        matrices.push();
        matrices.translate(armorX, armorY, 0);
        matrices.scale(0.9f, 0.9f, 1f);

        context.drawItem(displayEnemy.getEquippedStack(EquipmentSlot.MAINHAND), 0, 0, 0);
        context.drawItem(displayEnemy.getEquippedStack(EquipmentSlot.HEAD), 15, 0, 0);
        context.drawItem(displayEnemy.getEquippedStack(EquipmentSlot.CHEST), 30, 0, 0);
        context.drawItem(displayEnemy.getEquippedStack(EquipmentSlot.LEGS), 45, 0, 0);
        context.drawItem(displayEnemy.getEquippedStack(EquipmentSlot.FEET), 60, 0, 0);

        matrices.pop();
        
        matrices.pop();

        if (resetToNull) {
            enemy = null;
        }
    }
    
    private void renderRemnantCompact(MatrixStack matrices, DrawContext context, PlayerEntity displayEnemy) {
        FontAtlas font = Chorus.getInstance().getFonts().getInterSemiBold();
        boolean resetToNull = false;
        if (displayEnemy == null) {
            if (mc.currentScreen instanceof HUDEditorScreen) {
                displayEnemy = mc.player;
                resetToNull = true;
            } else {
                return;
            }
        }

        String name = displayEnemy.getNameForScoreboard();
        float hpDiff = mc.player.getHealth() - displayEnemy.getHealth();
        DecimalFormat format = new DecimalFormat("#.##");
        String hp = (hpDiff < 0 ? "-" : "+") + format.format(Math.abs(hpDiff)) + "hp";
        Color themeColor = new Color(255,127,127, 255);
        Color background = new Color(150, 150, 150, 75);

        int height = 35, length = 120, size = 30;
        int x = xPos.getValue();
        int y = yPos.getValue();
        float padding = 3, centerPadding = padding * 2, thickness = 3.5f;
        float fullWidth = font.getWidth(name, 6) + length;

        matrices.push();
        
        float scale = 0.5f + (0.5f * animationProgress);
        float alpha = animationProgress;
        float scaleX = x + (fullWidth / 2);
        float scaleY = y + (height / 2);
        
        matrices.translate(scaleX, scaleY, 0);
        
        if (isDisappearing) {
            matrices.translate(0, disappearY, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(disappearY * 0.8f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(disappearY * 0.3f));
        }
        
        matrices.scale(scale, scale, 1.0f);
        matrices.translate(-scaleX, -scaleY, 0);
        
        background = new Color(background.getRed(), background.getGreen(), background.getBlue(), 
                              (int)(background.getAlpha() * alpha));
        themeColor = new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 
                              (int)(themeColor.getAlpha() * alpha));

        Render2DEngine.drawRoundedBlur(matrices, x, y, fullWidth, height, 4, 8, new Color(255, 255, 255, (int)(10 * alpha)));
        Render2DEngine.drawRoundedOutline(matrices, x, y, fullWidth, height, 4, 1, background);
        setWidth(fullWidth);
        setHeight(height);
        
        if (alpha > 0.05f) {
            int hurtTimeOffset = (int) (displayEnemy.hurtTime / 4f);
            int hurtTimeSize = (int) (displayEnemy.hurtTime / 2f);
            
            int adjustedSize = (int)((size - hurtTimeSize) * Math.min(1.0f, alpha * 1.2f));
            if (adjustedSize > 0) {
                PlayerSkinDrawer.draw(context, ((AbstractClientPlayerEntity) displayEnemy).getSkinTextures(), 
                    (int) (x + padding + hurtTimeOffset), (int) (y + padding + hurtTimeOffset), adjustedSize);
                
                Render2DEngine.drawRect(matrices, 
                    (int) (x + padding + hurtTimeOffset), 
                    (int) (y + padding + hurtTimeOffset), 
                    adjustedSize, adjustedSize, 
                    new Color(255, 0, 0, (int)(25 * displayEnemy.hurtTime * alpha)));
            }
        }
        
        font.render(matrices, name, x + size + centerPadding, y + padding, 10f, new Color(255, 255, 255, (int)(255 * alpha)).getRGB());
        font.render(matrices, hp, x + fullWidth - font.getWidth(hp, 7f) - padding, y + centerPadding, 7f, new Color(255, 255, 255, (int)(255 * alpha)).getRGB());

        if (firstHealth == 0 || firstHealth != displayEnemy.getHealth()) {
            firstHealth = MathUtils.lerp(firstHealth == 0 ? displayEnemy.getHealth() : firstHealth, displayEnemy.getHealth(), 0.05f);
            secondHealth = MathUtils.lerp(secondHealth == 0 ? displayEnemy.getHealth() : secondHealth, displayEnemy.getHealth(), 0.005f);
        }

        float firstWidth = (fullWidth - centerPadding - size - padding) * firstHealth / displayEnemy.getMaxHealth();
        float secondWidth = (fullWidth - centerPadding - size - padding) * secondHealth / displayEnemy.getMaxHealth();
        Render2DEngine.drawRoundedRect(matrices, x + size + centerPadding, y + height - thickness - padding, secondWidth, thickness, 1, themeColor.darker().darker());
        Render2DEngine.drawRoundedRect(matrices, x + size + centerPadding, y + height - thickness - padding, firstWidth, thickness, 1, themeColor);

        int armorX = (int) (x + size + centerPadding);
        int armorY = (int) (y + padding + 10);

        matrices.push();
        matrices.translate(armorX, armorY, 0);
        matrices.scale(0.85f, 0.85f, 1f);
        int armorPosition = 0;

        for (EquipmentSlot slot : armorSlots) {
            ItemStack itemStack = displayEnemy.getEquippedStack(slot);

            if (!itemStack.isEmpty()) {
                context.drawItem(itemStack, armorPosition, 0, 0);
                armorPosition += 15;
            }
        }
        matrices.pop();
        
        matrices.pop();

        if (resetToNull) {
            enemy = null;
        }
    }

    private void renderRemnantInfo(MatrixStack matrices, DrawContext context, PlayerEntity displayEnemy) {
        FontAtlas font = Chorus.getInstance().getFonts().getInterSemiBold();
        boolean resetToNull = false;
        if (displayEnemy == null) {
            if (mc.currentScreen instanceof HUDEditorScreen) {
                displayEnemy = mc.player;
                resetToNull = true;
            } else {
                return;
            }
        }

        String name = displayEnemy.getNameForScoreboard();
        name = name.replaceAll("§", "");
        float hpDiff = mc.player.getHealth() - displayEnemy.getHealth();
        float absDiff = mc.player.getAbsorptionAmount() - displayEnemy.getAbsorptionAmount();
        DecimalFormat format = new DecimalFormat("#.##");
        Color healthColor = new Color(255,127,127, 255);
        Color absorptionColor = new Color(255,255,127, 255);
        Color armorColor = new Color(127,127,255, 255);
        Color background = new Color(150, 150, 150, 75);

        float height = 47.5f, length = 130, size = 30;
        int x = xPos.getValue();
        int y = yPos.getValue();
        float padding = 3, centerPadding = padding * 2, thickness = 3.5f;
        float fullWidth = Math.max(font.getWidth(name, 10f) + 80, length);

        matrices.push();
        
        float scale = 0.5f + (0.5f * animationProgress);
        float alpha = animationProgress;
        float scaleX = x + (fullWidth / 2);
        float scaleY = y + (height / 2);
        
        matrices.translate(scaleX, scaleY, 0);
        
        if (isDisappearing) {
            matrices.translate(0, disappearY, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(disappearY * 0.8f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(disappearY * 0.3f));
        }
        
        matrices.scale(scale, scale, 1.0f);
        matrices.translate(-scaleX, -scaleY, 0);
        
        background = new Color(background.getRed(), background.getGreen(), background.getBlue(), 
                              (int)(background.getAlpha() * alpha));
        healthColor = new Color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 
                              (int)(healthColor.getAlpha() * alpha));
        absorptionColor = new Color(absorptionColor.getRed(), absorptionColor.getGreen(), absorptionColor.getBlue(), 
                                   (int)(absorptionColor.getAlpha() * alpha));
        armorColor = new Color(armorColor.getRed(), armorColor.getGreen(), armorColor.getBlue(), 
                             (int)(armorColor.getAlpha() * alpha));

        Render2DEngine.drawRoundedBlur(matrices, x, y, fullWidth, height, 4, 8, new Color(255, 255, 255, (int)(10 * alpha)));
        Render2DEngine.drawRoundedOutline(matrices, x, y, fullWidth, height, 4, 1, background);
        setWidth(fullWidth);
        setHeight(height);
        
        if (alpha > 0.05f) {
            int hurtTimeOffset = (int) (displayEnemy.hurtTime / 4f);
            int hurtTimeSize = (int) (displayEnemy.hurtTime / 2f);
            
            int adjustedSize = (int)((size - hurtTimeSize) * Math.min(1.0f, alpha * 1.2f));
            if (adjustedSize > 0) {
                PlayerSkinDrawer.draw(context, ((AbstractClientPlayerEntity) displayEnemy).getSkinTextures(), 
                    (int) (x + padding + hurtTimeOffset), (int) (y + padding + hurtTimeOffset), adjustedSize);
                
                Render2DEngine.drawRect(matrices, 
                    (int) (x + padding + hurtTimeOffset), 
                    (int) (y + padding + hurtTimeOffset), 
                    adjustedSize, adjustedSize, 
                    new Color(255, 0, 0, (int)(25 * displayEnemy.hurtTime * alpha)));
            }
        }

        font.render(matrices, name, x + size + centerPadding, y + padding, 10f, new Color(255, 255, 255, (int)(255 * alpha)).getRGB());
        font.render(matrices, name, x + size + centerPadding, y + padding, 10f, new Color(255, 0, 0, (int)(25 * displayEnemy.hurtTime * alpha)).getRGB());

        String hp = (hpDiff < 0 ? "-" : "+") + format.format(Math.abs(hpDiff)) + "hp";
        String absHp = "+" + format.format(Math.abs(absDiff));

        font.render(matrices, hp, x + fullWidth - font.getWidth(hp, 8f) - (absDiff != 0 ? font.getWidth(absHp, 8f) : 0) - padding, y + height - centerPadding - padding - font.getLineHeight(8f), 8f, healthColor.getRGB());
        font.render(matrices, hp, x + fullWidth - font.getWidth(hp, 8f) - (absDiff != 0 ? font.getWidth(absHp, 8f) : 0) - padding, y + height - centerPadding - padding - font.getLineHeight(8f), 8f, new Color(255, 0, 0, (int)(25 * displayEnemy.hurtTime * alpha)).getRGB());

        if (absDiff != 0)
            font.render(matrices, absHp, x + fullWidth - font.getWidth(absHp, 8f) - padding, y + height - centerPadding - padding - font.getLineHeight(8f), 8f, absorptionColor.getRGB());
        String popCount = ((int) totemPops) + " totem used";
        font.render(matrices, popCount, x + fullWidth - font.getWidth(popCount, 6f) - centerPadding, y + padding, 6f, new Color(255, 255, 255, (int)(255 * alpha)).getRGB());

        if (firstHealth == 0 || firstHealth != displayEnemy.getHealth()) {
            firstHealth = MathUtils.lerp(firstHealth == 0 ? displayEnemy.getHealth() : firstHealth, displayEnemy.getHealth(), 0.05f);
            secondHealth = MathUtils.lerp(secondHealth == 0 ? displayEnemy.getHealth() : secondHealth, displayEnemy.getHealth(), 0.005f);
            absorption = MathUtils.lerp(absorption == 0 ? displayEnemy.getAbsorptionAmount() : absorption, displayEnemy.getAbsorptionAmount(), 0.005f);
            armorBar = MathUtils.lerp(armorBar == 0 ? getArmorAmount(displayEnemy) : armorBar, getArmorAmount(displayEnemy), 0.005f);
        }

        float slowHealthWidth = (fullWidth - centerPadding) * firstHealth / displayEnemy.getMaxHealth();
        float fastHealthWidth = (fullWidth - centerPadding) * secondHealth / displayEnemy.getMaxHealth();
        float absorptionWidth = (fullWidth - centerPadding) * absorption / displayEnemy.getMaxAbsorption();
        Render2DEngine.drawRoundedRect(matrices, x + padding, y + height - thickness - padding, fastHealthWidth, thickness, 1, healthColor.darker().darker());
        Render2DEngine.drawRoundedRect(matrices, x + padding, y + height - thickness - padding, slowHealthWidth, thickness, 1, healthColor);
        Render2DEngine.drawRoundedGradient(matrices, x + padding, y + height - thickness - padding, absorptionWidth, thickness, 1, absorptionColor, new Color(255, 127, 255, (int)(125 * alpha)));

        int armorX = (int) (x + size + centerPadding);
        int armorY = (int) (y + padding + 10);
        matrices.push();
        matrices.translate(armorX, armorY, 0);
        int armorPosition = 0;

        for (EquipmentSlot slot : armorSlots) {
            ItemStack itemStack = displayEnemy.getEquippedStack(slot);

            if (!itemStack.isEmpty()) {
                context.drawItem(itemStack, armorPosition, 0, 0);
                armorPosition += 15;
            }
        }
        matrices.pop();
        
        matrices.pop();

        if (resetToNull) {
            enemy = null;
        }
    }

    public float getProtectionValue(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ArmorItem)) return 0;

        int protLevel = EnchantmentHelper.getLevel(
                mc.world.getRegistryManager().getOptional(Enchantments.PROTECTION.getRegistryRef())
                        .get().getEntry(Enchantments.PROTECTION.getValue()).get(), stack
        );

        return 1 + protLevel;
    }

    public float getArmorAmount(LivingEntity enemy) {
        float effectiveArmor = 0;
        for (ItemStack stack : enemy.getArmorItems()) {
            effectiveArmor += getProtectionValue(stack);
        }
        float maxEffectiveArmor = 100;
        return effectiveArmor / maxEffectiveArmor;
    }
    public TargetHud() {
        setDraggable(true);
        getSettingRepository().registerSettings(general, mode, xPos, yPos);
        xPos.setRenderCondition(() -> false);
        yPos.setRenderCondition(() -> false);
    }
}