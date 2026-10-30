package com.jonas.villaeconomy.village;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.jonas.villaeconomy.registry.ModProfessions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

/**
 * Utilidades sobre vilas e banqueiros.
 */
public final class Villages {
	private Villages() {
	}

	public static VillagerProfession professionOf(Villager villager) {
		return villager.getVillagerData().profession().value();
	}

	public static boolean isProfessor(Villager villager, VillagerProfession profession) {
		return professionOf(villager) == profession;
	}

	public static boolean isBanker(Villager villager) {
		VillagerProfession profession = professionOf(villager);
		return profession == ModProfessions.BANQUEIRO || profession == ModProfessions.BANQUEIRO_NETHER;
	}

	public static boolean isUnemployed(Villager villager) {
		VillagerProfession profession = professionOf(villager);
		return profession == net.minecraft.world.entity.npc.villager.VillagerProfession.NONE.value()
				|| profession == net.minecraft.world.entity.npc.villager.VillagerProfession.NITWIT.value();
	}

	public static List<Villager> villagersNear(ServerPlayer player, double radius) {
		return player.level().getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(radius));
	}

	public static List<Villager> villagersNear(net.minecraft.server.level.ServerLevel level,
			net.minecraft.core.BlockPos pos, double radius) {
		return level.getEntitiesOfClass(Villager.class, new net.minecraft.world.phys.AABB(pos).inflate(radius));
	}

	public static Optional<Villager> nearestBanker(ServerPlayer player, double radius) {
		return villagersNear(player, radius).stream()
				.filter(Villages::isBanker)
				.min(Comparator.comparingDouble(villager -> villager.distanceToSqr(player)));
	}

	/** Nível da vila de acordo com o número de moradores por perto. */
	public static int levelFor(int villagers) {
		if (villagers >= 34) {
			return 6;
		} else if (villagers >= 26) {
			return 5;
		} else if (villagers >= 18) {
			return 4;
		} else if (villagers >= 12) {
			return 3;
		} else if (villagers >= 7) {
			return 2;
		} else if (villagers >= 3) {
			return 1;
		}

		return 0;
	}

	public static String levelName(int level) {
		return switch (level) {
		case 1 -> "Acampamento";
		case 2 -> "Povoado";
		case 3 -> "Vila";
		case 4 -> "Vila Grande";
		case 5 -> "Cidade";
		case 6 -> "Metrópole";
		default -> "Sem vila";
		};
	}
}
