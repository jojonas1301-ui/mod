package com.jonas.villaeconomy.entity;

import com.jonas.villaeconomy.VilaEconomy;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/**
 * Entidades do mod.
 */
public final class ModEntityTypes {
	public static final ResourceKey<EntityType<?>> NETHER_VILLAGER_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, VilaEconomy.id("nether_villager"));
	public static EntityType<NetherVillager> NETHER_VILLAGER;
	public static Item NETHER_VILLAGER_SPAWN_EGG;

	private ModEntityTypes() {
	}

	public static void register() {
		if (NETHER_VILLAGER != null) {
			return;
		}

		NETHER_VILLAGER = Registry.register(BuiltInRegistries.ENTITY_TYPE, NETHER_VILLAGER_KEY,
				FabricEntityType.Builder.createMob(NetherVillager::new, MobCategory.MISC,
						mob -> mob.defaultAttributes(Villager::createAttributes))
						.sized(0.6F, 1.95F)
						.fireImmune()
						.clientTrackingRange(10)
						.build(NETHER_VILLAGER_KEY));

		ResourceKey<Item> eggKey = ResourceKey.create(Registries.ITEM, VilaEconomy.id("nether_villager_spawn_egg"));
		NETHER_VILLAGER_SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, eggKey,
				new SpawnEggItem(new Item.Properties().setId(eggKey).spawnEgg(NETHER_VILLAGER)));
	}
}
