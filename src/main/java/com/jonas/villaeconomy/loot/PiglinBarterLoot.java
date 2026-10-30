package com.jonas.villaeconomy.loot;

import com.jonas.villaeconomy.economy.Notes;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;

/**
 * Piglins: além das trocas normais de ouro, agora também dão dinheiro, blocos de
 * ouro e relíquias do Nether quando você joga ouro para eles.
 */
public final class PiglinBarterLoot {
	private PiglinBarterLoot() {
	}

	public static void register() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!key.equals(BuiltInLootTables.PIGLIN_BARTERING)) {
				return;
			}

			tableBuilder.withPool(LootPool.lootPool()
					.add(LootItem.lootTableItem(Items.GOLD_BLOCK).setWeight(6))
					.add(LootItem.lootTableItem(Notes.itemFor(200)).setWeight(4))
					.add(LootItem.lootTableItem(Notes.itemFor(100)).setWeight(8))
					.add(LootItem.lootTableItem(Notes.itemFor(50)).setWeight(10))
					.add(LootItem.lootTableItem(Items.ANCIENT_DEBRIS).setWeight(2))
					.add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(3))
					.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(14)));
		});
	}
}
