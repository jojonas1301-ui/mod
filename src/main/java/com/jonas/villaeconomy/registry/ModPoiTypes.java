package com.jonas.villaeconomy.registry;

import java.util.LinkedHashMap;
import java.util.Map;

import com.jonas.villaeconomy.VilaEconomy;

import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;

/**
 * Pontos de interesse (postos de trabalho) das novas profissões.
 */
public final class ModPoiTypes {
	private static final Map<String, ResourceKey<PoiType>> POIS = new LinkedHashMap<>();

	private ModPoiTypes() {
	}

	public static void register() {
		if (!POIS.isEmpty()) {
			return;
		}

		register("balcao_banqueiro", ModBlocks.BALCAO_BANQUEIRO);
		register("mesa_mineracao", ModBlocks.MESA_MINERACAO);
		register("alambique", ModBlocks.ALAMBIQUE);
		register("bancada_joalheiro", ModBlocks.BANCADA_JOALHEIRO);
		register("posto_guarda", ModBlocks.POSTO_GUARDA);
		register("podio_leilao", ModBlocks.PODIO_LEILAO);
		register("cofre_tesoureiro", ModBlocks.COFRE_TESOUREIRO);
		register("balcao_nether", ModBlocks.BALCAO_NETHER);
		register("forja_ouro", ModBlocks.FORJA_OURO);
		register("totem_reliquias", ModBlocks.TOTEM_RELIQUIAS);
	}

	private static void register(String name, net.minecraft.world.level.block.Block block) {
		PoiHelper.register(VilaEconomy.id(name), 1, 1, block);
		POIS.put(name, ResourceKey.create(net.minecraft.core.registries.Registries.POINT_OF_INTEREST_TYPE,
				VilaEconomy.id(name)));
	}

	public static ResourceKey<PoiType> key(String name) {
		return POIS.get(name);
	}

	public static java.util.function.Predicate<Holder<PoiType>> is(String name) {
		ResourceKey<PoiType> key = POIS.get(name);
		return holder -> holder.is(key);
	}
}
