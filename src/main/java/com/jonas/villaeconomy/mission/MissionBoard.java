package com.jonas.villaeconomy.mission;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Quadro de missões de um banqueiro: 3 missões disponíveis e quando elas trocam.
 */
public record MissionBoard(List<Mission> missions, long refreshAt) {
	public static final Codec<MissionBoard> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Mission.CODEC.listOf().fieldOf("missoes").forGetter(MissionBoard::missions),
			Codec.LONG.fieldOf("troca_em").forGetter(MissionBoard::refreshAt)
	).apply(instance, MissionBoard::new));

	public boolean isExpired(long gameTime) {
		return gameTime >= this.refreshAt || this.missions.isEmpty();
	}
}
