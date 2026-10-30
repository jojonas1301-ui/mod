package com.jonas.villaeconomy.mission;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.RandomSource;

/**
 * Gera as missões. Quanto maior a dificuldade, maior a recompensa.
 */
public final class MissionCatalog {
	private record Target(String id, int baseAmount) {
	}

	private static final List<Target> KILL_EASY = List.of(
			new Target("minecraft:zombie", 8),
			new Target("minecraft:skeleton", 8),
			new Target("minecraft:spider", 6),
			new Target("minecraft:creeper", 4),
			new Target("minecraft:drowned", 6));

	private static final List<Target> KILL_MEDIUM = List.of(
			new Target("minecraft:enderman", 5),
			new Target("minecraft:witch", 4),
			new Target("minecraft:pillager", 5),
			new Target("minecraft:husk", 8),
			new Target("minecraft:slime", 10));

	private static final List<Target> KILL_HARD = List.of(
			new Target("minecraft:blaze", 6),
			new Target("minecraft:piglin", 8),
			new Target("minecraft:hoglin", 5),
			new Target("minecraft:wither_skeleton", 5),
			new Target("minecraft:magma_cube", 10));

	private static final List<Target> MINE_EASY = List.of(
			new Target("minecraft:coal_ore", 16),
			new Target("minecraft:iron_ore", 12),
			new Target("minecraft:copper_ore", 16),
			new Target("minecraft:stone", 32),
			new Target("minecraft:oak_log", 24));

	private static final List<Target> MINE_MEDIUM = List.of(
			new Target("minecraft:deepslate_iron_ore", 16),
			new Target("minecraft:redstone_ore", 12),
			new Target("minecraft:lapis_ore", 10),
			new Target("minecraft:nether_quartz_ore", 20),
			new Target("minecraft:gold_ore", 10));

	private static final List<Target> MINE_HARD = List.of(
			new Target("minecraft:diamond_ore", 8),
			new Target("minecraft:ancient_debris", 4),
			new Target("minecraft:deepslate_diamond_ore", 8),
			new Target("minecraft:emerald_ore", 5),
			new Target("minecraft:obsidian", 12));

	private static final List<Target> DELIVER_EASY = List.of(
			new Target("minecraft:rotten_flesh", 24),
			new Target("minecraft:bone", 20),
			new Target("minecraft:string", 16),
			new Target("minecraft:wheat", 24),
			new Target("minecraft:coal", 16));

	private static final List<Target> DELIVER_MEDIUM = List.of(
			new Target("minecraft:gunpowder", 12),
			new Target("minecraft:iron_ingot", 12),
			new Target("minecraft:gold_ingot", 10),
			new Target("minecraft:leather", 16),
			new Target("minecraft:paper", 24));

	private static final List<Target> DELIVER_HARD = List.of(
			new Target("minecraft:blaze_rod", 8),
			new Target("minecraft:ender_pearl", 6),
			new Target("minecraft:netherite_scrap", 3),
			new Target("minecraft:diamond", 5),
			new Target("minecraft:gold_block", 4));

	private static final int[] BASE_REWARD = { 0, 90, 200, 380, 700, 1300 };

	private MissionCatalog() {
	}

	public static List<Mission> generate(RandomSource random, int villageLevel) {
		List<Mission> missions = new ArrayList<>();
		int maxDifficulty = Math.max(1, Math.min(5, villageLevel + 1));

		for (int i = 0; i < 3; i++) {
			int difficulty = 1 + random.nextInt(maxDifficulty);
			MissionType type = switch (random.nextInt(3)) {
			case 0 -> MissionType.MATAR;
			case 1 -> MissionType.MINERAR;
			default -> MissionType.ENTREGAR;
			};

			Target target = pick(random, type, difficulty);
			float variance = 0.85F + random.nextFloat() * 0.4F;
			int reward = Math.round(BASE_REWARD[difficulty] * variance * 0.1F) * 10;

			missions.add(new Mission(type, target.id(), target.baseAmount(), 0, reward, difficulty));
		}

		return missions;
	}

	private static Target pick(RandomSource random, MissionType type, int difficulty) {
		List<Target> pool = switch (type) {
		case MATAR -> difficulty <= 2 ? KILL_EASY : difficulty == 3 ? KILL_MEDIUM : KILL_HARD;
		case MINERAR -> difficulty <= 2 ? MINE_EASY : difficulty == 3 ? MINE_MEDIUM : MINE_HARD;
		case ENTREGAR -> difficulty <= 2 ? DELIVER_EASY : difficulty == 3 ? DELIVER_MEDIUM : DELIVER_HARD;
		};

		Target target = pool.get(random.nextInt(pool.size()));
		// missões mais difíceis pedem mais itens
		int amount = Math.max(1, Math.round(target.baseAmount() * (0.7F + difficulty * 0.15F)));
		return new Target(target.id(), amount);
	}
}
