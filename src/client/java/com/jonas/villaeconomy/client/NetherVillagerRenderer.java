package com.jonas.villaeconomy.client;

import com.jonas.villaeconomy.VilaEconomy;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renderiza o aldeão do Nether com a textura própria (tom de piglin).
 */
public class NetherVillagerRenderer extends VillagerRenderer {
	private static final Identifier TEXTURE = VilaEconomy.id("textures/entity/villager/type/nether.png");

	public NetherVillagerRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(VillagerRenderState state) {
		return TEXTURE;
	}
}
