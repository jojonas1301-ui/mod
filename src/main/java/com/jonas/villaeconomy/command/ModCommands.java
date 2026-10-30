package com.jonas.villaeconomy.command;

import java.util.List;
import java.util.Optional;

import com.jonas.villaeconomy.economy.BankService;
import com.jonas.villaeconomy.economy.Notes;
import com.jonas.villaeconomy.economy.Wallet;
import com.jonas.villaeconomy.mission.Mission;
import com.jonas.villaeconomy.mission.MissionBoard;
import com.jonas.villaeconomy.mission.MissionService;
import com.jonas.villaeconomy.village.VillageManager;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;

/**
 * Comandos do mod: /carteira, /banco, /missao e /vila.
 */
public final class ModCommands {
	private ModCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(carteira());
			dispatcher.register(banco());
			dispatcher.register(missao());
			dispatcher.register(vila());
		});
	}

	private static LiteralArgumentBuilder<CommandSourceStack> carteira() {
		return Commands.literal("carteira").executes(context -> {
			ServerPlayer p = context.getSource().getPlayerOrException();
			p.sendSystemMessage(Component.literal("§6§lCARTEIRA"));
			p.sendSystemMessage(Component.literal("§7Dinheiro: " + Notes.money(Wallet.get(p))));
			p.sendSystemMessage(Component.literal("§7Notas no inventário: " + Notes.money(countNotes(p))));
			p.sendSystemMessage(Component.empty()
					.append(BankService.button("§a[Depositar tudo] ", "/banco depositar", ChatFormatting.GREEN))
					.append(BankService.button("§b[Abrir banco]", "/banco", ChatFormatting.AQUA)));
			return 1;
		});
	}

	private static LiteralArgumentBuilder<CommandSourceStack> banco() {
		return Commands.literal("banco")
				.executes(context -> {
					BankService.menu(context.getSource().getPlayerOrException());
					return 1;
				})
				.then(Commands.literal("saldo").executes(context -> {
					Wallet.sendBalance(context.getSource().getPlayerOrException());
					return 1;
				}))
				.then(Commands.literal("depositar").executes(context -> {
					BankService.depositAll(context.getSource().getPlayerOrException());
					return 1;
				}))
				.then(Commands.literal("sacar")
						.then(Commands.argument("valor", IntegerArgumentType.integer(10, 1000000))
								.executes(context -> {
									BankService.withdraw(context.getSource().getPlayerOrException(),
											IntegerArgumentType.getInteger(context, "valor"));
									return 1;
								})))
				.then(Commands.literal("loja").executes(context -> {
					BankService.shopMenu(context.getSource().getPlayerOrException());
					return 1;
				}))
				.then(Commands.literal("comprar")
						.then(Commands.argument("item", StringArgumentType.word())
								.executes(context -> {
									BankService.buy(context.getSource().getPlayerOrException(),
											StringArgumentType.getString(context, "item"), 1);
									return 1;
								})
								.then(Commands.argument("quantidade", IntegerArgumentType.integer(1, 64))
										.executes(context -> {
											BankService.buy(context.getSource().getPlayerOrException(),
													StringArgumentType.getString(context, "item"),
													IntegerArgumentType.getInteger(context, "quantidade"));
											return 1;
										}))));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> missao() {
		return Commands.literal("missao")
				.executes(context -> {
					listar(context.getSource().getPlayerOrException());
					return 1;
				})
				.then(Commands.literal("aceitar")
						.then(Commands.argument("indice", IntegerArgumentType.integer(1, 3))
								.executes(context -> {
									aceitar(context.getSource().getPlayerOrException(),
											IntegerArgumentType.getInteger(context, "indice") - 1);
									return 1;
								})))
				.then(Commands.literal("entregar")
						.then(Commands.argument("indice", IntegerArgumentType.integer(1, 5))
								.executes(context -> {
									MissionService.deliver(context.getSource().getPlayerOrException(),
											IntegerArgumentType.getInteger(context, "indice") - 1);
									return 1;
								})))
				.then(Commands.literal("abandonar")
						.then(Commands.argument("indice", IntegerArgumentType.integer(1, 5))
								.executes(context -> {
									abandonar(context.getSource().getPlayerOrException(),
											IntegerArgumentType.getInteger(context, "indice") - 1);
									return 1;
								})));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> vila() {
		return Commands.literal("vila").executes(context -> {
			ServerPlayer player = context.getSource().getPlayerOrException();
			VillageManager.describeVillage(player);
			return 1;
		});
	}

	private static void listar(ServerPlayer player) {
		List<Mission> ativas = MissionService.activeMissions(player);

		player.sendSystemMessage(Component.literal("§6§lMISSÕES ATIVAS §7(" + ativas.size() + "/5)"));

		if (ativas.isEmpty()) {
			player.sendSystemMessage(Component.literal("§7Nenhuma missão. Fale com um banqueiro."));
		}

		for (int i = 0; i < ativas.size(); i++) {
			Mission mission = ativas.get(i);
			player.sendSystemMessage(Component.literal("§e" + (i + 1) + ". ").append(mission.describe()));

			if (mission.type() == com.jonas.villaeconomy.mission.MissionType.ENTREGAR) {
				player.sendSystemMessage(Component.empty()
						.append(Component.literal("   "))
						.append(BankService.button("§a[Entregar]", "/missao entregar " + (i + 1),
								ChatFormatting.GREEN))
						.append(Component.literal(" "))
						.append(BankService.button("§c[Abandonar]", "/missao abandonar " + (i + 1),
								ChatFormatting.RED)));
			}
		}

		Optional<Villager> banqueiro = MissionService.bankerOf(player);

		if (banqueiro.isPresent()) {
			Villager banker = banqueiro.get();
			MissionBoard board = MissionService.boardOf(banker, player.level().getGameTime());
			player.sendSystemMessage(Component.literal("§6§lMISSÕES DO BANQUEIRO"));
			player.sendSystemMessage(MissionService.boardMessage(banker));

			for (int i = 0; i < board.missions().size(); i++) {
				Mission mission = board.missions().get(i);
				player.sendSystemMessage(Component.empty()
						.append(BankService.button("§a[Aceitar] ", "/missao aceitar " + (i + 1),
								ChatFormatting.GREEN))
						.append(Component.literal("§e" + (i + 1) + ". " + mission.stars() + " §f"))
						.append(mission.describeTarget())
						.append(Component.literal(" §7→ " + Notes.money(mission.reward()))));
			}
		} else {
			player.sendSystemMessage(Component.literal("§7Chegue perto de um banqueiro para ver novas missões."));
		}
	}

	private static void aceitar(ServerPlayer player, int index) {
		Optional<Villager> banqueiro = MissionService.bankerOf(player);

		if (banqueiro.isEmpty()) {
			player.sendSystemMessage(Component.literal("§cNenhum banqueiro por perto (32 blocos)."));
			return;
		}

		MissionService.accept(player, banqueiro.get(), index);
	}

	private static void abandonar(ServerPlayer player, int index) {
		List<Mission> ativas = MissionService.activeMissions(player);

		if (index < 0 || index >= ativas.size()) {
			player.sendSystemMessage(Component.literal("§cMissão inválida."));
			return;
		}

		Mission removed = ativas.remove(index);
		MissionService.setActiveMissions(player, ativas);
		player.sendSystemMessage(Component.literal("§7Missão abandonada: ").append(removed.describe()));
	}

	private static long countNotes(ServerPlayer player) {
		long total = 0;

		for (var stack : player.getInventory().getNonEquipmentItems()) {
			int value = Notes.valueOf(stack);

			if (value > 0) {
				total += (long) value * stack.getCount();
			}
		}

		return total;
	}

}
