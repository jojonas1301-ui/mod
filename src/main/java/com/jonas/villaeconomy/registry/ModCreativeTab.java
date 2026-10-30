package com.jonas.villaeconomy.registry;

import com.jonas.villaeconomy.VilaEconomy;
import com.jonas.villaeconomy.entity.ModEntityTypes;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Aba própria no criativo com as notas, os blocos de trabalho e o ovo do aldeão do Nether.
 */
public final class ModCreativeTab {
	public static final ResourceKey<CreativeModeTab> TAB_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, VilaEconomy.id("principal"));

	private ModCreativeTab() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.villaeconomy"))
				.icon(() -> new ItemStack(ModItems.notesByValue().get(100)))
				.displayItems((parameters, output) -> {
					ModItems.notes().forEach(output::accept);

					for (ModBlocks.Entry entry : ModBlocks.all()) {
						output.accept(entry.item());
					}

					output.accept(ModEntityTypes.NETHER_VILLAGER_SPAWN_EGG);
				})
				.build());
	}
}
