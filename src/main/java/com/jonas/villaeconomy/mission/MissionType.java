package com.jonas.villaeconomy.mission;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

/**
 * Tipos de missão dadas pelos banqueiros.
 */
public enum MissionType implements StringRepresentable {
	/** Matar criaturas. */
	MATAR("matar"),
	/** Minerar blocos. */
	MINERAR("minerar"),
	/** Entregar itens ao banqueiro. */
	ENTREGAR("entregar");

	private final String id;

	MissionType(String id) {
		this.id = id;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}

	public Component label() {
		return Component.translatable("villaeconomy.mission.type." + this.id);
	}

	public static MissionType byName(String name) {
		for (MissionType type : values()) {
			if (type.id.equals(name)) {
				return type;
			}
		}

		return MATAR;
	}
}
