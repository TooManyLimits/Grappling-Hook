package io.github.moonlight_maya.limits_grapple;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerPlayerVelocityHelper {

	//Concurrent since these might be updated on the network thread, and read on the main thread
	private static final Map<ServerPlayerEntity, Vec3d> OLD_POSITIONS = new ConcurrentHashMap<>(), NEW_POSITIONS = new ConcurrentHashMap<>();

	public static void updatePlayer(ServerPlayerEntity player, PlayerMoveC2SPacket packet) {
		if (!packet.changesPosition())
			return;
		Vec3d oldPos = NEW_POSITIONS.getOrDefault(player, player.getPos());
		Vec3d newPos;
		if (oldPos == null) {
			newPos = new Vec3d(packet.getX(player.getX()), packet.getY(player.getY()), packet.getZ(player.getZ()));
		} else {
			newPos = new Vec3d(packet.getX(oldPos.x), packet.getY(oldPos.y), packet.getZ(oldPos.z));
		}
		if (!newPos.equals(oldPos))
			OLD_POSITIONS.put(player, oldPos);
		NEW_POSITIONS.put(player, newPos);
	}

	/**
	 * May not be synced on each tick, but should have correct direction at least, which is all we need
	 */
	public static Vec3d getVelocity(ServerPlayerEntity player) {
		Vec3d oldPos = OLD_POSITIONS.get(player);
		Vec3d newPos = NEW_POSITIONS.get(player);
		if (oldPos == null || newPos == null)
			return Vec3d.ZERO;
		return newPos.subtract(oldPos);
	}

}
