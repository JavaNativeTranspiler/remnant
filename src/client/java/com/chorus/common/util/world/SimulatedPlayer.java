package com.chorus.common.util.world;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

@Getter
public class SimulatedPlayer {
    private Vec3d position;
    private Vec3d velocity;
    private float fallDistance;

    private float yaw;
    private float pitch;

    private boolean onGround;
    private boolean sprinting;
    private boolean horizontalCollision;
    private boolean verticalCollision;
    private PlayerInput input;

    private Box boundingBox;

    private final World world;

    private final PlayerEntity entity;

    private static final double GRAVITY = 0.08;
    private static final float JUMP_VELOCITY = 0.42f;
    private static final float BASE_MOVEMENT_SPEED = 0.1f;

    public SimulatedPlayer(PlayerEntity entity) {
        this.world = entity.getWorld();
        this.entity = entity;
        this.position = entity.getPos();
        this.velocity = new Vec3d(0, 0, 0);
        this.yaw = entity.getYaw();
        this.pitch = entity.getPitch();
        this.onGround = entity.isOnGround();
        this.sprinting = entity.isSprinting();
        this.fallDistance = entity.fallDistance;
        this.horizontalCollision = entity.horizontalCollision;
        this.verticalCollision = entity.verticalCollision;
        this.input = new PlayerInput();
        this.boundingBox = new Box(-0.3, 0.0, -0.3, 0.3, 1.8, 0.3).offset(position);
    }

    public void tick() {
        this.input.update();


        if (!onGround) {
            this.velocity = this.velocity.add(0, -GRAVITY, 0).multiply(1, 0.98f, 1);
        } else {
            if (this.input.jumping) {
                // jump in the direction of yaw
                this.velocity = new Vec3d(0, JUMP_VELOCITY, 0);

                if (this.isSprinting()) {
                    float rads = (float) Math.toRadians(this.yaw);
                    this.velocity.add(new Vec3d(
                            (-MathHelper.sin(rads) * 0.2f), 0.0, (MathHelper.cos(rads) * 0.2f)));
                }
            }
        }
        Vec3d movementInput = new Vec3d(
                this.input.movementSideways * getMovementSpeed(),
                0,
                this.input.movementForward * getMovementSpeed()
        );

        simulateMovement(movementInput);

        this.boundingBox = new Box(-0.3, 0.0, -0.3, 0.3, 1.8, 0.3).offset(this.position);

        if (onGround) {
            // ground friction
            this.velocity = new Vec3d(
                    this.velocity.x * 0.91,
                    this.velocity.y,
                    this.velocity.z * 0.91
            );
        } else {
            // air friction
            this.velocity = new Vec3d(
                    this.velocity.x * 0.98,
                    this.velocity.y * 0.98,
                    this.velocity.z * 0.98
            );
        }
    }


    private void simulateMovement(Vec3d movementInput) {
        Vec3d transformedMovement = rotateMovement(movementInput);
        this.velocity = this.velocity.add(transformedMovement);


        move(this.velocity);
    }

    private void move(Vec3d velocity) {
        Vec3d newVelocity = includeCollisionsInVelocity(velocity);
        if (onGround) {
            fallDistance = 0;
        } else {
            if (newVelocity.y < 0)
                fallDistance -= (float) newVelocity.y;
        }

        this.position = this.position.add(newVelocity);

        this.horizontalCollision = !threePointEstimator(velocity.x, newVelocity.x) || !threePointEstimator(velocity.z, newVelocity.z);
        this.verticalCollision = !threePointEstimator(velocity.y, newVelocity.y);

        this.onGround = this.verticalCollision && velocity.y < 0;

        if (onGround) {
            this.velocity = new Vec3d(this.velocity.x, 0, this.velocity.z);
        }
    }

    private Vec3d includeCollisionsInVelocity(Vec3d movement) {
        Box box = new Box(-0.3, 0.0, -0.3, 0.3, 1.8, 0.3).offset(this.position);

        Vec3d adjustedMovement = (movement.lengthSquared() == 0.0) ? movement
                : Entity.adjustMovementForCollisions(this.entity, movement, box, this.world, List.of());

        return adjustedMovement;
    }


    private boolean threePointEstimator(double a, double b) {
        return Math.abs(a - b) < 0.003;
    }
    private Vec3d rotateMovement(Vec3d movement) {
        double sin = Math.sin(Math.toRadians(this.yaw));
        double cos = Math.cos(Math.toRadians(this.yaw));

        return new Vec3d(
                movement.x * cos - movement.z * sin,
                movement.y,
                movement.x * sin + movement.z * cos
        );
    }
    private float getMovementSpeed() {
        float averageBlockSlipperiness = 0.6f;
        if (this.onGround) {
            return BASE_MOVEMENT_SPEED * (0.21600002f  / (averageBlockSlipperiness * averageBlockSlipperiness * averageBlockSlipperiness));
        } else {
            float speed = 0.02f;
            if (this.isSprinting()) {
                speed = speed + 0.006f;
            }
            return speed;
        }
    }

    public void setInput(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean sprint) {
        this.input.forward = forward;
        this.input.backward = backward;
        this.input.left = left;
        this.input.right = right;
        this.input.jumping = jump;
        this.sprinting = sprint;
    }

    public class PlayerInput {
        public boolean forward;
        public boolean backward;
        public boolean left;
        public boolean right;
        public boolean jumping;

        public float movementForward;
        public float movementSideways;

        public void update() {
            // movement input
            // TODO use grim input system to estimate inputs clientside.
            if (forward != backward) {
                movementForward = forward ? 1.0f : -1.0f;
            } else {
                movementForward = 0.0f;
            }

            if (left != right) {
                movementSideways = left ? 1.0f : -1.0f;
            } else {
                movementSideways = 0.0f;
            }
        }
    }

}