package com.jonas.villaeconomy.registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.jonas.villaeconomy.VilaEconomy;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.Block;

/**
 * As profissões novas. Os aldeões são iguais aos da vanilla, só que com ofertas
 * de troca próprias (arquivos de datapack) e postos de trabalho próprios.
 */
public final class ModProfessions {
	private static final Map<String, VillagerProfession> PROFESSIONS = new LinkedHashMap<>();
	private static final Map<String, Integer> MIN_VILLAGE_LEVEL = new LinkedHashMap<>();

	public static VillagerProfession BANQUEIRO;
	public static VillagerProfession MINEIRO;
	public static VillagerProfession ALQUIMISTA;
	public static VillagerProfession JOALHEIRO;
	public static VillagerProfession GUARDA;
	public static VillagerProfession LEILOEIRO;
	public static VillagerProfession TESOUREIRO;
	public static VillagerProfession BANQUEIRO_NETHER;
	public static VillagerProfession FERREIRO_OURO;
	public static VillagerProfession CACADOR_RELIQUIAS;

	private ModProfessions() {
	}

	public static void register() {
		if (!PROFESSIONS.isEmpty()) {
			return;
		}

		BANQUEIRO = create("banqueiro", "balcao_banqueiro", SoundEvents.VILLAGER_WORK_CARTOGRAPHER,
				ImmutableSet.of(Items.GOLD_INGOT, Items.EMERALD), 2);
		MINEIRO = create("mineiro", "mesa_mineracao", SoundEvents.VILLAGER_WORK_TOOLSMITH,
				ImmutableSet.of(Items.IRON_PICKAXE, Items.RAW_IRON), 1);
		ALQUIMISTA = create("alquimista", "alambique", SoundEvents.VILLAGER_WORK_CLERIC,
				ImmutableSet.of(Items.BLAZE_POWDER, Items.GLASS_BOTTLE), 1);
		JOALHEIRO = create("joalheiro", "bancada_joalheiro", SoundEvents.VILLAGER_WORK_ARMORER,
				ImmutableSet.of(Items.DIAMOND, Items.AMETHYST_SHARD), 2);
		GUARDA = create("guarda", "posto_guarda", SoundEvents.VILLAGER_WORK_FLETCHER,
				ImmutableSet.of(Items.IRON_SWORD, Items.SHIELD), 3);
		LEILOEIRO = create("leiloeiro", "podio_leilao", SoundEvents.VILLAGER_WORK_LIBRARIAN,
				ImmutableSet.of(Items.EMERALD, Items.GOLD_BLOCK), 4);
		TESOUREIRO = create("tesoureiro", "cofre_tesoureiro", SoundEvents.VILLAGER_WORK_LEATHERWORKER,
				ImmutableSet.of(Items.GOLD_BLOCK, Items.DIAMOND_BLOCK), 5);

		// profissões do Nether (trocas em ouro)
		BANQUEIRO_NETHER = create("banqueiro_nether", "balcao_nether", SoundEvents.VILLAGER_WORK_CARTOGRAPHER,
				ImmutableSet.of(Items.GOLD_INGOT, Items.GOLD_NUGGET), 1);
		FERREIRO_OURO = create("ferreiro_ouro", "forja_ouro", SoundEvents.VILLAGER_WORK_ARMORER,
				ImmutableSet.of(Items.GOLD_INGOT, Items.GOLD_BLOCK), 1);
		CACADOR_RELIQUIAS = create("cacador_reliquias", "totem_reliquias", SoundEvents.VILLAGER_WORK_CLERIC,
				ImmutableSet.of(Items.GOLD_INGOT, Items.BLAZE_ROD), 1);
	}

	private static VillagerProfession create(String name, String poi, SoundEvent sound, ImmutableSet<Item> wanted,
			int minLevel) {
		Int2ObjectMap<ResourceKey<TradeSet>> trades = new Int2ObjectOpenHashMap<>();

		for (int level = 1; level <= 5; level++) {
			trades.put(level, ResourceKey.create(Registries.TRADE_SET,
					VilaEconomy.id(name + "/level_" + level)));
		}

		VillagerProfession profession = new VillagerProfession(
				Component.translatable("villaeconomy.profession." + name),
				ModPoiTypes.is(poi),
				ModPoiTypes.is(poi),
				wanted,
				ImmutableSet.<Block>of(),
				sound,
				trades);

		Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, VilaEconomy.id(name), profession);
		PROFESSIONS.put(name, profession);
		MIN_VILLAGE_LEVEL.put(name, minLevel);
		return profession;
	}

	public static VillagerProfession get(String name) {
		return PROFESSIONS.get(name);
	}

	public static List<VillagerProfession> professions() {
		return new ArrayList<>(PROFESSIONS.values());
	}

	public static Map<String, VillagerProfession> byName() {
		return PROFESSIONS;
	}

	public static int minLevel(VillagerProfession profession) {
		for (Map.Entry<String, VillagerProfession> entry : PROFESSIONS.entrySet()) {
			if (entry.getValue() == profession) {
				return MIN_VILLAGE_LEVEL.getOrDefault(entry.getKey(), 1);
			}
		}

		return 1;
	}

	/** Profissões do Nether (trocas em ouro). */
	public static List<VillagerProfession> netherProfessions() {
		return List.of(BANQUEIRO_NETHER, FERREIRO_OURO, CACADOR_RELIQUIAS);
	}

	/** Profissões do Overworld, na ordem em que aparecem nas vilas. */
	public static List<VillagerProfession> overworldProfessions() {
		return List.of(BANQUEIRO, MINEIRO, ALQUIMISTA, JOALHEIRO, GUARDA, LEILOEIRO, TESOUREIRO);
	}
}
