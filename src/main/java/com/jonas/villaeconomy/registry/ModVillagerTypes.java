package com.jonas.villaeconomy.registry;

import com.jonas.villaeconomy.VilaEconomy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerType;

/**
 * Tipo de aldeão do Nether (visual próprio, mesmo comportamento de aldeão).
 */
public final class ModVillagerTypes {
	public static final ResourceKey<VillagerType> NETHER_KEY =
			ResourceKey.create(Registries.VILLAGER_TYPE, VilaEconomy.id("nether"));
	public static VillagerType NETHER;

	private ModVillagerTypes() {
	}

	public static void register() {
		if (NETHER != null) {
			return;
		}

		NETHER = Registry.register(BuiltInRegistries.VILLAGER_TYPE, NETHER_KEY, new VillagerType());
	}
}
