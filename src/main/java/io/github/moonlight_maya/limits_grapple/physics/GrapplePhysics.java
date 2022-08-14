package io.github.moonlight_maya.limits_grapple.physics;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class GrapplePhysics {

	public static final double ACCEL = 0.1;
	public static final double JERK = 0.03;
	public static final double LOOK_COMPONENT = 0.15;
	public static final double INPUT_COMPONENT = 0.25;
	public static final double MAX_SPEED = 2.0;
	public static final int DEFAULT_STABILITY = 30;

	/**
	 * Return 0 or lower if the grapple should break.
	 * The returned value will be passed back in next time as grappleStability.
	 */

	/*
	NOTES FOR MORNING LIMITS

	- Draw out actual physics stuffs in ms paint
	- Ensure that player never moves away from the grapple point while connected, otherwise it breaks
	 */

	public static int handlePhysics(LivingEntity user, Vec3d anchorPoint, int ticksActive, int grappleStability) {
		if (user instanceof ClientPlayerEntity clientPlayerEntity) {
			double timeMul = 1 + (JERK / 20 * ticksActive);

			Vec3d diffVec = anchorPoint.subtract(user.getPos()).normalize(); //Add velocity from the difference
			Vec3d lookVec = user.getRotationVector();
			double yaw = Math.toRadians(user.getYaw());
			Vec3d userForwardVec = new Vec3d(Math.sin(yaw), 0, Math.cos(yaw));
			Vec3d userSidewaysVec = new Vec3d(userForwardVec.z, 0, userForwardVec.x);

			Vec3d vel = diffVec.multiply(ACCEL + JERK / 20 * ticksActive); //Add velocity from the difference
			vel = vel.add(lookVec.multiply(LOOK_COMPONENT)); //Add velocity from look component
			//Add velocity from input component
			vel = vel.add(userForwardVec.multiply(clientPlayerEntity.input.forwardMovement)
					.add(userSidewaysVec.multiply(clientPlayerEntity.input.sidewaysMovement))
					.multiply(INPUT_COMPONENT));

			//Get total velocity and clamp it
			vel = vel.add(user.getVelocity());
			double newSpeed = Math.min(vel.length(), MAX_SPEED);
			vel = vel.normalize().multiply(newSpeed);

			user.setVelocity(vel); //Set velocity

			//Degrade stability
			if (lookVec.dotProduct(diffVec) < 0)
				grappleStability -= 5;
			if (vel.dotProduct(diffVec) < 0)
				grappleStability -= 1;
		}
		return grappleStability;
	}




}
