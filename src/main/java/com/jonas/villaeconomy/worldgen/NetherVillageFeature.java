package com.jonas.villaeconomy.worldgen;

import com.jonas.villaeconomy.entity.ModEntityTypes;
import com.jonas.villaeconomy.registry.ModBlocks;
import com.jonas.villaeconomy.registry.ModProfessions;
import com.jonas.villaeconomy.registry.ModVillagerTypes;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.List;

/**
 * Gera uma pequena vila no Nether: praça de blackstone, muros, lanternas de alma,
 * balcão do banqueiro e aldeões do Nether (banqueiro, ferreiro de ouro e caçador
 * de relíquias). As trocas deles são pagas em ouro.
 */
public class NetherVillageFeature implements Feature {
	public static final NetherVillageFeature INSTANCE = new NetherVillageFeature();

	@Override
	public MapCodec<? extends Feature> codec() {
		return MapCodec.unit(INSTANCE);
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos pos) {
		com.jonas.villaeconomy.VilaEconomy.LOGGER.info("[Vilas & Câmbio] gerando vila do Nether em {}", pos);

		if (level.isEmptyBlock(pos.below()) || level.isEmptyBlock(pos.below(2))) {
			return false;
		}

		int radius = 6;
		BlockState blackstone = Blocks.BLACKSTONE.defaultBlockState();
		BlockState bricks = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
		BlockState polished = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
		BlockState air = Blocks.AIR.defaultBlockState();

		// limpa e constrói a praça
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				level.setBlock(pos.offset(dx, -1, dz), blackstone, 2);

				for (int dy = 0; dy < 4; dy++) {
					level.setBlock(pos.offset(dx, dy, dz), air, 2);
				}
			}
		}

		// muros com quatro portas
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				boolean border = Math.abs(dx) == radius || Math.abs(dz) == radius;

				if (!border) {
					continue;
				}

				if (Math.abs(dx) <= 1 && Math.abs(dz) == radius) {
					continue;
				}

				if (Math.abs(dz) <= 1 && Math.abs(dx) == radius) {
					continue;
				}

				level.setBlock(pos.offset(dx, 0, dz), polished, 2);
				level.setBlock(pos.offset(dx, 1, dz), bricks, 2);
				level.setBlock(pos.offset(dx, 2, dz), bricks, 2);
			}
		}

		// lanternas de alma nos cantos e no meio das paredes
		for (int dx = -radius + 1; dx <= radius - 1; dx += 2) {
			placeLantern(level, pos.offset(dx, 0, -radius + 1));
			placeLantern(level, pos.offset(dx, 0, radius - 1));
		}

		for (int dz = -radius + 1; dz <= radius - 1; dz += 2) {
			placeLantern(level, pos.offset(-radius + 1, 0, dz));
			placeLantern(level, pos.offset(radius - 1, 0, dz));
		}

		// balcão do banqueiro no centro, cercado de ouro
		level.setBlock(pos, ModBlocks.BALCAO_NETHER.defaultBlockState(), 2);
		level.setBlock(pos.offset(-2, 0, 0), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
		level.setBlock(pos.offset(2, 0, 0), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 2);
		level.setBlock(pos.offset(0, 0, -2), ModBlocks.FORJA_OURO.defaultBlockState(), 2);
		level.setBlock(pos.offset(0, 0, 2), ModBlocks.TOTEM_RELIQUIAS.defaultBlockState(), 2);

		// barraquinhas
		hut(level, pos.offset(-4, 0, -4), random);
		hut(level, pos.offset(4, 0, -4), random);
		hut(level, pos.offset(-4, 0, 4), random);
		hut(level, pos.offset(4, 0, 4), random);

		// moradores: gerar entidades durante a geração de chunk trava o servidor,
		// então o spawn é enfileirado para o fim do tick, quando o chunk já está pronto
		final int villagerLevel = 1 + random.nextInt(3);
		ServerLevel serverLevel = level.getLevel();

		if (serverLevel != null) {
			List<BlockPos> bankerPositions = List.of(
					pos.offset(0, 0, 1),
					pos.offset(-5, 0, -5),
					pos.offset(5, 0, 5));
			List<BlockPos> artisanPositions = List.of(
					pos.offset(0, 0, -3),
					pos.offset(0, 0, 3),
					pos.offset(-3, 0, 3),
					pos.offset(3, 0, -3));

			serverLevel.getServer().execute(() -> {
				spawnVillager(serverLevel, bankerPositions, ModProfessions.BANQUEIRO_NETHER, villagerLevel);
				spawnVillager(serverLevel, artisanPositions, ModProfessions.FERREIRO_OURO, villagerLevel);
				spawnVillager(serverLevel, artisanPositions, ModProfessions.CACADOR_RELIQUIAS, villagerLevel);
			});
		}

		com.jonas.villaeconomy.VilaEconomy.LOGGER.info("[Vilas & Câmbio] vila do Nether pronta em {}", pos);
		return true;
	}

	private void placeLantern(WorldGenLevel level, BlockPos pos) {
		level.setBlock(pos, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
	}

	private void hut(WorldGenLevel level, BlockPos center, RandomSource random) {
		BlockState bricks = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
		BlockState basalt = Blocks.BASALT.defaultBlockState();

		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (dx == 0 && dz == 0) {
					continue;
				}

				level.setBlock(center.offset(dx, 0, dz), bricks, 2);
				level.setBlock(center.offset(dx, 1, dz), bricks, 2);
			}
		}

		level.setBlock(center.offset(0, 2, 0), Blocks.SHROOMLIGHT.defaultBlockState(), 2);
		level.setBlock(center.offset(-2, 0, 0), basalt, 2);
		level.setBlock(center.offset(2, 0, 0), basalt, 2);
	}

	private void spawnVillager(ServerLevel serverLevel, List<BlockPos> positions, VillagerProfession profession, int level) {
		Holder<VillagerProfession> professionHolder = BuiltInRegistries.VILLAGER_PROFESSION.wrapAsHolder(profession);
		Holder<VillagerType> typeHolder = BuiltInRegistries.VILLAGER_TYPE.wrapAsHolder(ModVillagerTypes.NETHER);

		for (BlockPos pos : positions) {
			if (!serverLevel.isLoaded(pos)) {
				com.jonas.villaeconomy.VilaEconomy.LOGGER.warn("[Vilas & Câmbio] posição {} não carregada, aldeão não gerado", pos);
				continue;
			}

			var villager = ModEntityTypes.NETHER_VILLAGER.spawn(serverLevel, pos, EntitySpawnReason.STRUCTURE);

			if (villager == null) {
				com.jonas.villaeconomy.VilaEconomy.LOGGER.warn("[Vilas & Câmbio] falha ao gerar aldeão em {}", pos);
				continue;
			}

			com.jonas.villaeconomy.VilaEconomy.LOGGER.info("[Vilas & Câmbio] aldeão {} em {}",
					professionHolder.value().name().getString(), pos);

			villager.setVillagerData(villager.getVillagerData()
					.withType(typeHolder)
					.withProfession(professionHolder)
					.withLevel(level));
			villager.setVillagerXp(0);
			villager.setPersistenceRequired();
		}
	}
}
