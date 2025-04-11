package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.rotation.RotationUtils;
import com.chorus.core.listener.impl.TickEventListener;
import com.chorus.impl.events.render.Render3DEvent;
import com.chorus.impl.modules.visual.AspectRatio;
import com.chorus.impl.modules.visual.TotemAnimation;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiFunction;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements QuickImports {
    @Shadow
    private float zoom;

    @Shadow
    private float zoomX;

    @Shadow
    private float zoomY;

    @Shadow
    private float viewDistance;

    @Shadow @Final
    private BufferBuilderStorage buffers;

    @Shadow private int floatingItemTimeLeft;

    @Invoker("getFov")
    public abstract float invokeGetActualFov(Camera camera, float tickDelta, boolean changingFOV);

    @Unique
    private static final float RAD_TO_DEG = 0.017453292f;

    @Accessor("floatingItemTimeLeft")
    public abstract void setFloatingItemTimeLeft(int timeLeft);

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        setFloatingItemTimeLeft(floatingItemTimeLeft == 40 ? (int) (floatingItemTimeLeft - (40 * (Chorus.getInstance().getModuleManager().getModule(TotemAnimation.class).time.getValue() * 0.01f))) : floatingItemTimeLeft);
    }
    // stole this mixin from thunderhack $$$$ (also fuck u pathos u fucked up the matrices)
    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    private void onWorldRender(RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        Camera camera = mc.gameRenderer.getCamera();
        MatrixStack matrixStack = new MatrixStack();
        RenderSystem.getModelViewStack().pushMatrix().mul(matrixStack.peek().getPositionMatrix());
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));

        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();

        Render3DEvent event = new Render3DEvent(
                Render3DEvent.Mode.PRE,
                matrixStack,
                tickCounter.getTickDelta(false),
                camera,
                (GameRenderer) (Object) this,
                projectionMatrix,
                mc.worldRenderer
        );

        Chorus.getInstance().getEventManager().post(event);

        RenderSystem.getModelViewStack().popMatrix();
    }

    @ModifyExpressionValue(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"))
    private HitResult findCrosshairTargetInject(HitResult original, Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta) {
        if (camera != mc.player) return original;


        return handleRotation(original, camera, (yaw, pitch) ->
                RotationUtils.rayTrace(yaw, pitch, (float) Math.max(blockInteractionRange, entityInteractionRange), tickDelta));
    }

    @ModifyExpressionValue(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getRotationVec(F)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d findCrosshairTargetInject(Vec3d original, Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta) {
        if (camera != mc.player) return original;


        return handleRotation(original, camera, this::getRotationVec);
    }

    @Unique
    private <T> T handleRotation(T original, Entity camera, BiFunction<Float, Float, T> rotationHandler) {
        if (camera != mc.player) return original;

        TickEventListener rotationManager = (TickEventListener) Chorus.getInstance().getListenerRepository().getListeners().get(0);
        if (!rotationManager.isRotating()) return rotationHandler.apply(camera.getYaw(), camera.getPitch());

        float[] rotation = rotationManager.getCurrentRotation();
        return rotationHandler.apply(rotation[0], rotation[1]);
    }


    @Unique
    private Vec3d getRotationVec(float yaw, float pitch) {
        float f = pitch * (float) (Math.PI/ 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrixHook(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
        if (Chorus.getInstance().getModuleManager().getModule(AspectRatio.class).isEnabled()) {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (zoom != 1.0f) {
                matrixStack.translate(zoomX, -zoomY, 0.0f);
                matrixStack.scale(zoom, zoom, 1.0f);
            }
            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float) (fovDegrees * 0.01745329238474369), (float) (Chorus.getInstance().getModuleManager().getModule(AspectRatio.class).getSettingRepository().getSetting("Aspect Ratio").getValue()), 0.05f, viewDistance * 4.0f));
            cir.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }
}