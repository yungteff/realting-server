package com.realting;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.realting.DiscordBot.JavaCord;
import com.realting.engine.GameEngine;
import com.realting.engine.task.TaskManager;
import com.realting.engine.task.impl.ServerTimeUpdateTask;
import com.realting.model.container.impl.Shop.ShopManager;
import com.realting.model.definitions.ItemDefinition;
import com.realting.model.definitions.NPCDrops;
import com.realting.model.definitions.NpcDefinition;
import com.realting.model.definitions.WeaponInterfaces;
import com.realting.net.PipelineFactory;
import com.realting.net.security.ConnectionHandler;
import com.realting.util.FileUtils;
import com.realting.world.clip.region.RegionClipping;
import com.realting.world.content.CustomObjects;
import com.realting.world.content.Lottery;
import com.realting.world.content.PlayerPunishment;
import com.realting.world.content.Scoreboards;
import com.realting.world.content.WellOfGoodwill;
import com.realting.world.content.clan.ClanChatManager;
import com.realting.world.content.combat.effect.CombatPoisonEffect.CombatPoisonData;
import com.realting.world.content.combat.strategy.CombatStrategies;
import com.realting.world.content.dialogue.DialogueManager;
import com.realting.world.content.grandexchange.GrandExchangeOffers;
import com.realting.model.entity.character.npc.NPC;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;

/**
 * Credit: lare96, Gabbe
 */
public final class GameLoader {

	private final ExecutorService serviceLoader = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("GameLoadingThread").build());
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("GameThread").build());
	private final GameEngine engine;
	private final int port;

	protected GameLoader(int port) {
		this.port = port;
		this.engine = new GameEngine();
	}

	public void init() {
		Preconditions.checkState(!serviceLoader.isShutdown(), "The bootstrap has been bound already!");
		executeServiceLoad();
		serviceLoader.shutdown();
	}

	public void finish() throws IOException, InterruptedException {
		if (!serviceLoader.awaitTermination(15, TimeUnit.MINUTES))
			throw new IllegalStateException("The background service load took too long!");
		ExecutorService networkExecutor = Executors.newCachedThreadPool();
		ServerBootstrap serverBootstrap = new ServerBootstrap (new NioServerSocketChannelFactory(networkExecutor, networkExecutor));
        serverBootstrap.setPipelineFactory(new PipelineFactory(new HashedWheelTimer()));
        serverBootstrap.bind(new InetSocketAddress(port));
		executor.scheduleAtFixedRate(engine, 0, GameSettings.ENGINE_PROCESSING_CYCLE_RATE, TimeUnit.MILLISECONDS);
		TaskManager.submit(new ServerTimeUpdateTask());
	}

	private void executeServiceLoad() {
		/*if (GameSettings.MYSQL_ENABLED) {
			serviceLoader.execute(() -> MySQLController.init());
		}*/
		FileUtils.createSaveDirectories();

		if (GameServer.getConfiguration().isDiscordBotEnabled()) {
			try {
				JavaCord.init().get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		serviceLoader.execute(() -> ConnectionHandler.init());
		serviceLoader.execute(() -> PlayerPunishment.init());
		serviceLoader.execute(() -> RegionClipping.init());
		serviceLoader.execute(() -> CustomObjects.init());
		serviceLoader.execute(() -> ItemDefinition.init());
		serviceLoader.execute(() -> Lottery.init());
		serviceLoader.execute(() -> GrandExchangeOffers.init());
		serviceLoader.execute(() -> Scoreboards.init());
		serviceLoader.execute(() -> WellOfGoodwill.init());
		serviceLoader.execute(() -> ClanChatManager.init());
		serviceLoader.execute(() -> CombatPoisonData.init());
		serviceLoader.execute(() -> CombatStrategies.init());
		serviceLoader.execute(() -> NpcDefinition.parseNpcs().load());
		serviceLoader.execute(() -> NPCDrops.load());
		serviceLoader.execute(() -> WeaponInterfaces.parseInterfaces().load());
		serviceLoader.execute(() -> ShopManager.load());
		serviceLoader.execute(() -> DialogueManager.parseDialogues().load());
		serviceLoader.execute(() -> NPC.init());
	}

	public GameEngine getEngine() {
		return engine;
	}
}