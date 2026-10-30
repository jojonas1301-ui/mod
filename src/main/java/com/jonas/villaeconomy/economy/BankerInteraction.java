package com.jonas.villaeconomy.economy;

import com.jonas.villaeconomy.village.Villages;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.server.level.ServerPlayer;

/**
 * Agachado + clique direito no banqueiro abre o menu do banco.
 */
public final class BankerInteraction {
	private BankerInteraction() {
	}

	public static void register() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			if (!(entity instanceof Villager villager) || !Villages.isBanker(villager)) {
				return InteractionResult.PASS;
			}

			if (!(player instanceof ServerPlayer serverPlayer) || !player.isShiftKeyDown()) {
				return InteractionResult.PASS;
			}

			BankService.menu(serverPlayer);
			return InteractionResult.SUCCESS;
		});
	}
}
