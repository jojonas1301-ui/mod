package com.jonas.villaeconomy.village;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.jonas.villaeconomy.mission.MissionService;
import com.jonas.villaeconomy.registry.ModProfessions;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

/**
 * Cuida do crescimento das vilas: conta os moradores, sobe de nível, coloca
 * profissões novas e garante que toda vila tenha um banqueiro.
 */
public final class VillageManager {
	private static final int RADIUS = 48;
	private static final Map<UUID, Integer> LAST_LEVEL = new HashMap<>();
	private static int tickCounter;

	private VillageManager() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(VillageManager::tick);
	}

	private static void tick(MinecraftServer server) {
		if (++tickCounter % 100 != 0) {
			return;
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			updateVillage(player);
		}
	}

	public static int updateVillage(ServerPlayer player) {
		List<Villager> villagers = Villages.villagersNear(player, RADIUS);
		int level = Villages.levelFor(villagers.size());
		int previous = LAST_LEVEL.getOrDefault(player.getUUID(), -1);

		if (level > previous && previous >= 0) {
			player.sendSystemMessage(Component.literal("§6§l[Vila] §eA vila subiu para o nível §6" + level
					+ "§e (" + Villages.levelName(level) + ")! §7Novas profissões liberadas."));
		}

		LAST_LEVEL.put(player.getUUID(), level);

		if (level < 2) {
			return level;
		}

		boolean changed = ensureBanker(villagers, level);
		changed |= assignProfession(villagers, level, player);

		if (changed) {
			Villages.villagersNear(player, RADIUS).forEach(villager -> {
				if (villager.level() instanceof ServerLevel serverLevel) {
					villager.refreshBrain(serverLevel);
				}
			});
		}

		return level;
	}

	/** Toda vila precisa de um banqueiro. */
	private static boolean ensureBanker(List<Villager> villagers, int level) {
		boolean hasBanker = villagers.stream().anyMatch(Villages::isBanker);

		if (hasBanker || level < 2) {
			return false;
		}

		for (Villager villager : villagers) {
			if (Villages.isUnemployed(villager) && !villager.isBaby()) {
				profess(villager, ModProfessions.BANQUEIRO);
				return true;
			}
		}

		return false;
	}

	/** Dá profissões novas conforme o nível da vila. */
	private static boolean assignProfession(List<Villager> villagers, int level, ServerPlayer player) {
		Set<VillagerProfession> present = new LinkedHashSet<>();

		for (Villager villager : villagers) {
			present.add(Villages.professionOf(villager));
		}

		Villager free = null;

		for (Villager villager : villagers) {
			if (Villages.isUnemployed(villager) && !villager.isBaby()) {
				free = villager;
				break;
			}
		}

		if (free == null) {
			return false;
		}

		for (VillagerProfession profession : ModProfessions.overworldProfessions()) {
			int minLevel = ModProfessions.minLevel(profession);

			if (minLevel > level || present.contains(profession)) {
				continue;
			}

			if (player.level().dimension() == net.minecraft.world.level.Level.NETHER) {
				continue;
			}

			profess(free, profession);
			player.sendSystemMessage(Component.literal("§7[Vila] Um morador virou §f")
					.append(profession.name())
					.append(Component.literal("§7 (nível " + minLevel + "+).")));
			return true;
		}

		return false;
	}

	public static void profess(Villager villager, VillagerProfession profession) {
		Holder<VillagerProfession> holder = BuiltInRegistries.VILLAGER_PROFESSION.wrapAsHolder(profession);
		villager.setVillagerData(villager.getVillagerData().withProfession(holder).withLevel(1));
		villager.setVillagerXp(0);
		villager.setPersistenceRequired();
	}

	public static void describeVillage(ServerPlayer player) {
		List<Villager> villagers = Villages.villagersNear(player, RADIUS);
		int level = Villages.levelFor(villagers.size());
		List<VillagerProfession> present = new ArrayList<>();

		for (Villager villager : villagers) {
			VillagerProfession profession = Villages.professionOf(villager);

			if (profession != VillagerProfession.NONE.value() && profession != VillagerProfession.NITWIT.value()
					&& !present.contains(profession)) {
				present.add(profession);
			}
		}

		player.sendSystemMessage(Component.literal("§6§lVILA MAIS PRÓXIMA"));
		player.sendSystemMessage(Component.literal("§7Moradores por perto: §f" + villagers.size()
				+ " §7| Nível: §6" + level + " §7(" + Villages.levelName(level) + ")"));
		player.sendSystemMessage(Component.literal("§7Banqueiro: "
				+ (villagers.stream().anyMatch(Villages::isBanker) ? "§aSim" : "§cNão")));

		if (!present.isEmpty()) {
			StringBuilder builder = new StringBuilder();

			for (VillagerProfession profession : present) {
				if (!builder.isEmpty()) {
					builder.append("§7, ");
				}

				builder.append("§f").append(profession.name().getString());
			}

			player.sendSystemMessage(Component.literal("§7Profissões na vila: " + builder));
		} else {
			player.sendSystemMessage(Component.literal("§7Nenhuma profissão por perto ainda."));
		}

		player.sendSystemMessage(Component.literal("§7Próximas profissões: ").append(nextUnlocks(level)));
		Villages.nearestBanker(player, RADIUS).ifPresent(banker ->
				player.sendSystemMessage(MissionService.boardMessage(banker)));
	}

	private static Component nextUnlocks(int level) {
		StringBuilder builder = new StringBuilder();

		for (VillagerProfession profession : ModProfessions.overworldProfessions()) {
			int minLevel = ModProfessions.minLevel(profession);

			if (minLevel > level) {
				if (!builder.isEmpty()) {
					builder.append("§7, ");
				}

				builder.append("§e").append(profession.name().getString()).append("§8(nível ").append(minLevel)
						.append(")");
			}
		}

		if (builder.isEmpty()) {
			return Component.literal("§aTudo liberado!");
		}

		return Component.literal(builder.toString());
	}
}
