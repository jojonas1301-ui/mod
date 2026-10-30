package com.jonas.villaeconomy.mission;

import java.util.List;

import com.jonas.villaeconomy.economy.Notes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Uma missão: objetivo, progresso, recompensa e dificuldade (1 a 5).
 */
public record Mission(MissionType type, String target, int amount, int progress, int reward, int difficulty) {
	public static final Codec<Mission> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.xmap(MissionType::byName, MissionType::getSerializedName).fieldOf("tipo")
					.forGetter(Mission::type),
			Codec.STRING.fieldOf("alvo").forGetter(Mission::target),
			Codec.INT.fieldOf("quantidade").forGetter(Mission::amount),
			Codec.INT.fieldOf("progresso").forGetter(Mission::progress),
			Codec.INT.fieldOf("recompensa").forGetter(Mission::reward),
			Codec.INT.fieldOf("dificuldade").forGetter(Mission::difficulty)
	).apply(instance, Mission::new));

	public static final Codec<List<Mission>> LIST_CODEC = CODEC.listOf();

	public boolean isComplete() {
		return this.progress >= this.amount;
	}

	public Mission withProgress(int value) {
		return new Mission(this.type, this.target, this.amount, Math.min(value, this.amount), this.reward,
				this.difficulty);
	}

	public String stars() {
		return "§6" + "★".repeat(Math.max(1, Math.min(5, this.difficulty)))
				+ "§8" + "★".repeat(Math.max(0, 5 - Math.min(5, this.difficulty)));
	}

	public Component targetName() {
		Identifier id = Identifier.tryParse(this.target);

		if (id == null) {
			return Component.literal(this.target);
		}

		switch (this.type) {
		case MATAR:
			return BuiltInRegistries.ENTITY_TYPE.get(id)
					.<Component>map(holder -> holder.value().getDescription())
					.orElse(Component.literal(this.target));
		case MINERAR:
			return BuiltInRegistries.BLOCK.get(id)
					.<Component>map(holder -> holder.value().getName())
					.orElse(Component.literal(this.target));
		case ENTREGAR:
		default:
			return BuiltInRegistries.ITEM.get(id)
					.<Component>map(holder -> new ItemStack(holder.value()).getHoverName())
					.orElse(Component.literal(this.target));
		}
	}

	public Component describe() {
		return Component.translatable("villaeconomy.mission.line", this.stars(), this.targetName(), this.amount,
				Component.literal(this.progress + "/" + this.amount),
				Component.literal(Notes.money(this.reward)));
	}

	public Component describeTarget() {
		return Component.literal("§7" + this.amount + "x ").append(this.targetName().copy().withStyle(net.minecraft.ChatFormatting.WHITE));
	}
}
