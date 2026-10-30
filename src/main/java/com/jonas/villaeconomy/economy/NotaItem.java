package com.jonas.villaeconomy.economy;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Uma nota de dinheiro. Clique com o botão direito para guardar na carteira.
 * Agachado + clique guarda todas as notas iguais de uma vez.
 */
public class NotaItem extends Item {
	private final int value;

	public NotaItem(Properties properties, int value) {
		super(properties);
		this.value = value;
	}

	public int value() {
		return this.value;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		long deposited;

		if (player.isShiftKeyDown()) {
			deposited = depositAll(player);
		} else {
			held.shrink(1);
			deposited = this.value;
		}

		if (deposited <= 0) {
			return InteractionResult.SUCCESS;
		}

		Wallet.add(player, deposited);
		player.sendSystemMessage(Component.literal("§aDepositado " + Notes.money(deposited) + " §7na carteira. Saldo: ")
				.append(Component.literal(Notes.money(Wallet.get(player))).withStyle(ChatFormatting.GOLD)));
		return InteractionResult.SUCCESS;
	}

	private long depositAll(Player player) {
		NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();
		long total = 0;

		for (int i = 0; i < items.size(); i++) {
			ItemStack stack = items.get(i);

			if (!stack.isEmpty() && stack.getItem() == this) {
				total += (long) stack.getCount() * this.value;
				stack.setCount(0);
			}
		}

		return total;
	}
}
