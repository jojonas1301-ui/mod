package com.jonas.villaeconomy;

import com.jonas.villaeconomy.command.ModCommands;
import com.jonas.villaeconomy.economy.BankerInteraction;
import com.jonas.villaeconomy.economy.Wallet;
import com.jonas.villaeconomy.entity.ModEntityTypes;
import com.jonas.villaeconomy.loot.PiglinBarterLoot;
import com.jonas.villaeconomy.mission.MissionService;
import com.jonas.villaeconomy.registry.ModBlocks;
import com.jonas.villaeconomy.registry.ModCreativeTab;
import com.jonas.villaeconomy.registry.ModItems;
import com.jonas.villaeconomy.registry.ModPoiTypes;
import com.jonas.villaeconomy.registry.ModProfessions;
import com.jonas.villaeconomy.registry.ModVillagerTypes;
import com.jonas.villaeconomy.worldgen.ModWorldgen;
import com.jonas.villaeconomy.village.VillageManager;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vilas &amp; Câmbio - sistema de economia, vilas por níveis e novas profissões para o Minecraft.
 */
public class VilaEconomy implements ModInitializer {
	public static final String MOD_ID = "villaeconomy";
	public static final Logger LOGGER = LoggerFactory.getLogger("VilasECambio");

	@Override
	public void onInitialize() {
		// ordem importa: blocos -> itens -> poi -> profissões -> entidades -> mundo
		ModBlocks.register();
		ModItems.register();
		ModPoiTypes.register();
		ModVillagerTypes.register();
		ModProfessions.register();
		ModEntityTypes.register();
		ModWorldgen.register();
		ModCreativeTab.register();

		Wallet.register();
		MissionService.register();
		ModCommands.register();
		VillageManager.register();
		BankerInteraction.register();
		PiglinBarterLoot.register();

		LOGGER.info("[Vilas & Câmbio] carregado com {} profissões novas e {} blocos de trabalho",
				ModProfessions.professions().size(), ModBlocks.all().size());
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
