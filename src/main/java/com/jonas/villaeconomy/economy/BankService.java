package com.jonas.villaeconomy.economy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;

/**
 * O banco da vila: depositar notas, sacar dinheiro e comprar itens com a carteira.
 */
public final class BankService {
	public record ShopEntry(String id, Item item, int count, long price) {
	}

	private static final Map<String, ShopEntry> SHOP = new LinkedHashMap<>();

	static {
		add("pao", Items.BREAD, 4, 20);
		add("flecha", Items.ARROW, 16, 30);
		add("ferro", Items.IRON_INGOT, 2, 60);
		add("carvao", Items.COAL, 8, 30);
		add("esmeralda", Items.EMERALD, 1, 120);
		add("ouro", Items.GOLD_INGOT, 1, 90);
		add("diamante", Items.DIAMOND, 1, 400);
		add("lapis", Items.LAPIS_LAZULI, 8, 60);
		add("blaze_rod", Items.BLAZE_ROD, 2, 200);
		add("perola", Items.ENDER_PEARL, 1, 250);
		add("pocao_cura", Items.SPLASH_POTION, 1, 300);
		add("netherite", Items.NETHERITE_SCRAP, 1, 1200);
	}

	private BankService() {
	}

	private static void add(String id, Item item, int count, long price) {
		SHOP.put(id, new ShopEntry(id, item, count, price));
	}

	public static Map<String, ShopEntry> shop() {
		return SHOP;
	}

	public static Component button(String label, String command, ChatFormatting color) {
		return Component.literal(label).withStyle(style -> style.withColor(color)
				.withClickEvent(new ClickEvent.RunCommand(command)));
	}

	public static void menu(ServerPlayer player) {
		long balance = Wallet.get(player);
		player.sendSystemMessage(Component.literal("§8§m--------------------------------"));
		player.sendSystemMessage(Component.literal("§6§l         BANCO DA VILA"));
		player.sendSystemMessage(Component.literal("§7Saldo na carteira: " + Notes.money(balance)));
		player.sendSystemMessage(Component.empty()
				.append(button("§a[Depositar tudo] ", "/banco depositar", ChatFormatting.GREEN))
				.append(button("§e[100] ", "/banco sacar 100", ChatFormatting.YELLOW))
				.append(button("§e[500] ", "/banco sacar 500", ChatFormatting.YELLOW))
				.append(button("§e[1.000]", "/banco sacar 1000", ChatFormatting.YELLOW)));
		player.sendSystemMessage(Component.empty()
				.append(button("§b[Loja] ", "/banco loja", ChatFormatting.AQUA))
				.append(button("§d[Missões] ", "/missao", ChatFormatting.LIGHT_PURPLE))
				.append(button("§2[Vila]", "/vila", ChatFormatting.DARK_GREEN)));
		player.sendSystemMessage(Component.literal("§8§m--------------------------------"));
	}

	public static void shopMenu(ServerPlayer player) {
		player.sendSystemMessage(Component.literal("§6§lLOJA DO BANCO §7(paga com a carteira)"));
		SHOP.forEach((id, entry) -> player.sendSystemMessage(Component.empty()
				.append(button("§a[Comprar] ", "/banco comprar " + id, ChatFormatting.GREEN))
				.append(Component.literal("§f" + entry.count() + "x ")
						.append(new ItemStack(entry.item()).getHoverName())
						.append(Component.literal(" §7- " + Notes.money(entry.price()))))));
	}

	/** Guarda todas as notas do inventário na carteira. */
	public static long depositAll(ServerPlayer player) {
		NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();
		long total = 0;

		for (ItemStack stack : items) {
			if (stack.isEmpty()) {
				continue;
			}

			int value = Notes.valueOf(stack);

			if (value > 0) {
				total += (long) value * stack.getCount();
				stack.setCount(0);
			}
		}

		if (total > 0) {
			Wallet.add(player, total);
			player.sendSystemMessage(Component.literal("§aDepositado " + Notes.money(total) + " §7na carteira."));
		} else {
			player.sendSystemMessage(Component.literal("§cVocê não tem notas no inventário."));
		}

		return total;
	}

	/** Saca dinheiro da carteira em notas. */
	public static boolean withdraw(ServerPlayer player, int amount) {
		if (amount <= 0) {
			player.sendSystemMessage(Component.literal("§cValor inválido."));
			return false;
		}

		int value = (amount / 10) * 10;

		if (value <= 0) {
			player.sendSystemMessage(Component.literal("§cO valor mínimo é 10."));
			return false;
		}

		if (Wallet.get(player) < value) {
			player.sendSystemMessage(Component.literal("§cSaldo insuficiente. Você tem " + Notes.money(Wallet.get(player))));
			return false;
		}

		List<ItemStack> notes = new ArrayList<>();
		int remaining = value;

		for (int denomination : Notes.VALUES) {
			while (remaining >= denomination) {
				notes.add(new ItemStack(Notes.itemFor(denomination)));
				remaining -= denomination;
			}
		}

		if (remaining > 0) {
			player.sendSystemMessage(Component.literal("§cNão é possível sacar " + value + " com essas notas."));
			return false;
		}

		Wallet.spend(player, value);

		for (ItemStack stack : notes) {
			if (!player.getInventory().add(stack)) {
				dropAt(player, stack);
			}
		}

		player.sendSystemMessage(Component.literal("§aSaque de " + Notes.money(value) + " §7em notas."));
		return true;
	}

	private static void dropAt(ServerPlayer player, ItemStack stack) {
		ItemEntity item = new ItemEntity(player.level(), player.getX(), player.getY() + 1.0D, player.getZ(), stack);
		player.level().addFreshEntity(item);
	}

	public static boolean buy(ServerPlayer player, String id, int quantity) {
		ShopEntry entry = SHOP.get(id);

		if (entry == null) {
			player.sendSystemMessage(Component.literal("§cEsse item não está na loja: §e" + id));
			return false;
		}

		int times = Math.max(1, Math.min(64, quantity));
		long total = entry.price() * times;

		if (!Wallet.spend(player, total)) {
			player.sendSystemMessage(Component.literal("§cSaldo insuficiente. Custa " + Notes.money(total)));
			return false;
		}

		ItemStack stack = new ItemStack(entry.item(), entry.count() * times);

		if (!player.getInventory().add(stack)) {
			dropAt(player, stack);
		}

		player.sendSystemMessage(Component.literal("§aComprado " + (entry.count() * times) + "x ")
				.append(new ItemStack(entry.item()).getHoverName())
				.append(Component.literal(" §7por " + Notes.money(total))));
		return true;
	}
}
