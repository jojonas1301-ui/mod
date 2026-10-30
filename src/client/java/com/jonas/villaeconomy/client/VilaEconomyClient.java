package com.jonas.villaeconomy.client;

import com.jonas.villaeconomy.entity.ModEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class VilaEconomyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntityTypes.NETHER_VILLAGER, NetherVillagerRenderer::new);
	}
}
