package com.jonas.villaeconomy.worldgen;

import com.jonas.villaeconomy.VilaEconomy;

import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Registro da feature das vilas do Nether e sua colocação nos biomas do Nether.
 */
public final class ModWorldgen {
	public static final ResourceKey<PlacedFeature> NETHER_VILLAGE_PLACED =
			ResourceKey.create(Registries.PLACED_FEATURE, VilaEconomy.id("nether_village"));

	private ModWorldgen() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.FEATURE_TYPE, VilaEconomy.id("nether_village"),
				(MapCodec<? extends net.minecraft.world.level.levelgen.feature.Feature>) MapCodec
						.unit(NetherVillageFeature.INSTANCE));

		BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(),
				GenerationStep.Decoration.SURFACE_STRUCTURES, NETHER_VILLAGE_PLACED);
	}
}
