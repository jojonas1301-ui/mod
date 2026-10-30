package com.jonas.villaeconomy.economy;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.jonas.villaeconomy.registry.ModItems;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Utilidades de dinheiro: denominações, conversão de notas e formatação.
 */
public final class Notes {
	/** Valores existentes, do maior para o menor (usado para saque guloso). */
	public static final int[] VALUES = { 200, 100, 50, 20, 10 };

	private static final Map<Item, Integer> BY_ITEM = new HashMap<>();
	private static final DecimalFormat FORMAT;

	static {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
		symbols.setGroupingSeparator('.');
		FORMAT = new DecimalFormat("#,##0", symbols);
	}

	private Notes() {
	}

	public static String itemName(int value) {
		return "nota" + value;
	}

	public static int valueOf(Item item) {
		Integer cached = BY_ITEM.get(item);
		if (cached != null) {
			return cached;
		}

		for (Map.Entry<Integer, Item> entry : ModItems.notesByValue().entrySet()) {
			if (entry.getValue() == item) {
				BY_ITEM.put(item, entry.getKey());
				return entry.getKey();
			}
		}

		return 0;
	}

	public static int valueOf(ItemStack stack) {
		return stack.isEmpty() ? 0 : valueOf(stack.getItem());
	}

	public static boolean isNote(ItemStack stack) {
		return valueOf(stack) > 0;
	}

	public static Item itemFor(int value) {
		return ModItems.notesByValue().get(value);
	}

	public static ItemStack stackFor(int value) {
		Item item = itemFor(value);
		return item == null ? ItemStack.EMPTY : new ItemStack(item);
	}

	public static String format(long amount) {
		return FORMAT.format(amount);
	}

	public static String money(long amount) {
		return "§6$§e" + format(amount);
	}
}
