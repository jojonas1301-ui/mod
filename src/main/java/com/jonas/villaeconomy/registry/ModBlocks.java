package com.jonas.villaeconomy.registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jonas.villaeconomy.VilaEconomy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Blocos de trabalho (workstations) usados pelas novas profissões.
 */
public final class ModBlocks {
	private static final Map<String, Entry> BLOCKS = new LinkedHashMap<>();

	public static Block BALCAO_BANQUEIRO;
	public static Block MESA_MINERACAO;
	public static Block ALAMBIQUE;
	public static Block BANCADA_JOALHEIRO;
	public static Block POSTO_GUARDA;
	public static Block PODIO_LEILAO;
	public static Block COFRE_TESOUREIRO;
	public static Block BALCAO_NETHER;
	public static Block FORJA_OURO;
	public static Block TOTEM_RELIQUIAS;

	private ModBlocks() {
	}

	public static void register() {
		if (!BLOCKS.isEmpty()) {
			return;
		}

		BALCAO_BANQUEIRO = workstation("balcao_banqueiro", Blocks.CRAFTING_TABLE);
		MESA_MINERACAO = workstation("mesa_mineracao", Blocks.SMITHING_TABLE);
		ALAMBIQUE = workstation("alambique", Blocks.BREWING_STAND);
		BANCADA_JOALHEIRO = workstation("bancada_joalheiro", Blocks.CARTOGRAPHY_TABLE);
		POSTO_GUARDA = workstation("posto_guarda", Blocks.FLETCHING_TABLE);
		PODIO_LEILAO = workstation("podio_leilao", Blocks.LECTERN);
		COFRE_TESOUREIRO = workstation("cofre_tesoureiro", Blocks.BARREL);
		BALCAO_NETHER = workstation("balcao_nether", Blocks.BLACKSTONE);
		FORJA_OURO = workstation("forja_ouro", Blocks.GILDED_BLACKSTONE);
		TOTEM_RELIQUIAS = workstation("totem_reliquias", Blocks.CHISELED_POLISHED_BLACKSTONE);
	}

	private static Block workstation(String name, Block copyOf) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, VilaEconomy.id(name));
		Block block = Registry.register(BuiltInRegistries.BLOCK, blockKey,
				new Block(BlockBehaviour.Properties.ofFullCopy(copyOf).setId(blockKey)));

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, VilaEconomy.id(name));
		BlockItem item = Registry.register(BuiltInRegistries.ITEM, itemKey,
				new BlockItem(block, new Item.Properties().setId(itemKey)));

		BLOCKS.put(name, new Entry(block, item));
		return block;
	}

	public static List<Entry> all() {
		return new ArrayList<>(BLOCKS.values());
	}

	public static Iterable<Entry> entries() {
		return BLOCKS.values();
	}

	public record Entry(Block block, BlockItem item) {
	}
}
