package com.jonas.villaeconomy.registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jonas.villaeconomy.VilaEconomy;
import com.jonas.villaeconomy.economy.NotaItem;
import com.jonas.villaeconomy.economy.Notes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/**
 * Notas de dinheiro: 10, 20, 50, 100 e 200.
 */
public final class ModItems {
	private static final Map<Integer, Item> NOTES = new LinkedHashMap<>();

	private ModItems() {
	}

	public static void register() {
		if (!NOTES.isEmpty()) {
			return;
		}

		for (int value : Notes.VALUES) {
			ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, VilaEconomy.id(Notes.itemName(value)));
			Item item = Registry.register(BuiltInRegistries.ITEM, key,
					new NotaItem(new Item.Properties().setId(key).stacksTo(64), value));
			NOTES.put(value, item);
		}
	}

	public static List<Item> notes() {
		return new ArrayList<>(NOTES.values());
	}

	public static Map<Integer, Item> notesByValue() {
		return NOTES;
	}
}
