package com.jonas.villaeconomy.economy;

import com.jonas.villaeconomy.VilaEconomy;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Carteira do jogador: o dinheiro fica salvo no jogador (não em itens).
 */
public final class Wallet {
	public static final AttachmentType<Long> WALLET = AttachmentRegistry.createPersistent(
			VilaEconomy.id("carteira"), Codec.LONG);

	private Wallet() {
	}

	public static void register() {
		// só para forçar o carregamento da classe / registrar o attachment
		VilaEconomy.LOGGER.debug("Carteira registrada: {}", WALLET);
	}

	public static long get(Player player) {
		return player.getAttachedOrCreate(WALLET, () -> 0L);
	}

	public static void set(Player player, long value) {
		player.setAttached(WALLET, Math.max(0L, value));
	}

	public static void add(Player player, long amount) {
		set(player, get(player) + amount);
	}

	public static boolean spend(Player player, long amount) {
		long balance = get(player);

		if (balance < amount) {
			return false;
		}

		set(player, balance - amount);
		return true;
	}

	public static void sendBalance(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendSystemMessage(Component.literal("§6Carteira: " + Notes.money(get(player))));
		}
	}

	public static void actionBar(Player player, long amount) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendSystemMessage(
					Component.literal("§6$§e" + Notes.format(amount) + " §7na carteira"), true);
		}
	}
}
