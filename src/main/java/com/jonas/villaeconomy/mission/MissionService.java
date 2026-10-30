package com.jonas.villaeconomy.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jonas.villaeconomy.VilaEconomy;
import com.jonas.villaeconomy.economy.Notes;
import com.jonas.villaeconomy.economy.Wallet;
import com.jonas.villaeconomy.village.Villages;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;

/**
 * Missões dos banqueiros: novas missões a cada 30 minutos e recompensa em dinheiro.
 */
public final class MissionService {
	/** 30 minutos em ticks. */
	public static final long REFRESH_TICKS = 20L * 60L * 30L;
	/** Quantas missões cada banqueiro oferece. */
	public static final int BOARD_SIZE = 3;

	public static final AttachmentType<MissionBoard> BOARD = AttachmentRegistry.createPersistent(
			VilaEconomy.id("banco_missoes"), MissionBoard.CODEC);
	public static final AttachmentType<List<Mission>> ACTIVE = AttachmentRegistry.createPersistent(
			VilaEconomy.id("missoes_ativas"), Mission.LIST_CODEC);

	private static int tickCounter;

	private MissionService() {
	}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DEATH.register(MissionService::onDeath);
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (player instanceof ServerPlayer serverPlayer) {
				onMine(serverPlayer, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(MissionService::tick);
	}

	private static void tick(MinecraftServer server) {
		if (++tickCounter % 200 != 0) {
			return;
		}

		long gameTime = server.overworld().getGameTime();

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			Villages.nearestBanker(player, 32.0D).ifPresent(banker -> {
				MissionBoard board = boardOf(banker, gameTime);

				if (board.isExpired(gameTime)) {
					MissionBoard refreshed = refresh(banker, gameTime, (ServerLevel) player.level());
					player.sendSystemMessage(Component.literal("§6[Banco] §eNovas missões chegaram em ")
							.append(banker.getName())
							.append(Component.literal("§e! Use §f/missao §epara ver.")));
					VilaEconomy.LOGGER.debug("Quadro de missões atualizado: {}", refreshed.missions().size());
				}
			});
		}
	}

	private static void onDeath(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source) {
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			return;
		}

		String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
		onKill(player, id);
	}

	public static void onKill(ServerPlayer player, String entityId) {
		progress(player, MissionType.MATAR, entityId);
	}

	public static void onMine(ServerPlayer player, String blockId) {
		progress(player, MissionType.MINERAR, blockId);
	}

	private static void progress(ServerPlayer player, MissionType type, String targetId) {
		List<Mission> active = activeMissions(player);

		if (active.isEmpty()) {
			return;
		}

		List<Mission> updated = new ArrayList<>();
		boolean changed = false;

		for (Mission mission : active) {
			if (mission.type() == type && mission.target().equals(targetId) && !mission.isComplete()) {
				Mission next = mission.withProgress(mission.progress() + 1);
				changed = true;

				if (next.isComplete()) {
					pay(player, next);
				} else {
					updated.add(next);
				}
			} else {
				updated.add(mission);
			}
		}

		if (changed) {
			setActiveMissions(player, updated);
		}
	}

	public static void pay(ServerPlayer player, Mission mission) {
		Wallet.add(player, mission.reward());
		player.sendSystemMessage(Component.literal("§a✔ Missão concluída! §7Recompensa: §6$§e"
				+ Notes.format(mission.reward()) + " §7(Carteira: §6$§e" + Notes.format(Wallet.get(player)) + "§7)"));
	}

	public static List<Mission> activeMissions(ServerPlayer player) {
		return new ArrayList<>(player.getAttachedOrCreate(ACTIVE, List::of));
	}

	public static void setActiveMissions(ServerPlayer player, List<Mission> missions) {
		player.setAttached(ACTIVE, List.copyOf(missions));
	}

	public static MissionBoard boardOf(Villager banker, long gameTime) {
		MissionBoard board = banker.getAttachedOrCreate(BOARD, () -> new MissionBoard(List.of(), 0L));

		if (board.isExpired(gameTime)) {
			board = refresh(banker, gameTime, (ServerLevel) banker.level());
		}

		return board;
	}

	public static MissionBoard refresh(Villager banker, long gameTime, ServerLevel level) {
		int villageLevel = Villages.levelFor(Villages.villagersNear(level, banker.blockPosition(), 48.0D).size());
		List<Mission> missions = MissionCatalog.generate(level.getRandom(), Math.max(2, villageLevel));
		MissionBoard board = new MissionBoard(missions, gameTime + REFRESH_TICKS);
		banker.setAttached(BOARD, board);
		return board;
	}

	public static boolean accept(ServerPlayer player, Villager banker, int index) {
		MissionBoard board = boardOf(banker, player.level().getGameTime());
		List<Mission> available = board.missions();

		if (index < 0 || index >= available.size()) {
			player.sendSystemMessage(Component.literal("§cMissão inválida."));
			return false;
		}

		Mission mission = available.get(index);
		List<Mission> mine = activeMissions(player);

		if (mine.size() >= 5) {
			player.sendSystemMessage(Component.literal("§cVocê já está com 5 missões ativas."));
			return false;
		}

		mine.add(mission);
		setActiveMissions(player, mine);
		player.sendSystemMessage(Component.literal("§aMissão aceita: ").append(mission.describe()));
		return true;
	}

	public static boolean deliver(ServerPlayer player, int index) {
		List<Mission> mine = activeMissions(player);

		if (index < 0 || index >= mine.size()) {
			player.sendSystemMessage(Component.literal("§cVocê não tem essa missão."));
			return false;
		}

		Mission mission = mine.get(index);

		if (mission.type() != MissionType.ENTREGAR) {
			player.sendSystemMessage(Component.literal("§cEssa missão se completa sozinha (matar/minerar)."));
			return false;
		}

		var holder = BuiltInRegistries.ITEM.get(net.minecraft.resources.Identifier.tryParse(mission.target()));

		if (holder.isEmpty()) {
			player.sendSystemMessage(Component.literal("§cItem da missão não existe mais."));
			return false;
		}

		ItemStack wanted = new ItemStack(holder.get().value());
		int have = count(player, wanted.getItem());

		if (have < mission.amount()) {
			player.sendSystemMessage(Component.literal("§cFaltam itens: §e" + have + "/" + mission.amount()));
			return false;
		}

		consume(player, wanted.getItem(), mission.amount());
		mine.remove(index);
		setActiveMissions(player, mine);
		pay(player, mission);
		return true;
	}

	/** Conta quantos itens iguais o jogador tem no inventário. */
	public static int count(ServerPlayer player, net.minecraft.world.item.Item item) {
		int total = 0;

		for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
			if (!stack.isEmpty() && stack.is(item)) {
				total += stack.getCount();
			}
		}

		return total;
	}

	/** Remove itens do inventário do jogador. */
	public static void consume(ServerPlayer player, net.minecraft.world.item.Item item, int amount) {
		int remaining = amount;

		for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
			if (remaining <= 0) {
				break;
			}

			if (!stack.isEmpty() && stack.is(item)) {
				int taken = Math.min(remaining, stack.getCount());
				stack.shrink(taken);
				remaining -= taken;
			}
		}
	}

	public static Optional<Villager> bankerOf(ServerPlayer player) {
		return Villages.nearestBanker(player, 32.0D);
	}

	public static Component boardMessage(Villager banker) {
		MissionBoard board = boardOf(banker, banker.level().getGameTime());
		long minutes = Math.max(0, (board.refreshAt() - banker.level().getGameTime()) / 1200L);
		return Component.literal("§7Troca em §e" + minutes + " min").withStyle(ChatFormatting.GRAY);
	}
}
