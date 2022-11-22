package com.realting

import com.google.common.base.Preconditions
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.realting.engine.GameEngine
import com.realting.engine.task.TaskManager
import com.realting.engine.task.impl.ServerTimeUpdateTask
import com.realting.model.container.impl.Shop.ShopManager
import com.realting.model.definitions.ItemDefinition
import com.realting.model.definitions.NPCDrops
import com.realting.model.definitions.NpcDefinition
import com.realting.model.definitions.WeaponInterfaces
import com.realting.model.entity.character.npc.NPC
import com.realting.net.PipelineFactory
import com.realting.net.security.ConnectionHandler
import com.realting.util.FileUtils
import com.realting.world.clip.region.RegionClipping
import com.realting.world.content.CustomObjects
import com.realting.world.content.PlayerPunishment
import com.realting.world.content.Scoreboards
import com.realting.world.content.WellOfGoodwill
import com.realting.world.content.clan.ClanChatManager
import com.realting.world.content.combat.effect.CombatPoisonEffect.CombatPoisonData
import com.realting.world.content.combat.strategy.CombatStrategies
import com.realting.world.content.dialogue.DialogueManager
import com.realting.world.content.grandexchange.GrandExchangeOffers
import com.realting.world.content.player.events.Lottery
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.jboss.netty.util.HashedWheelTimer
import java.io.IOException
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Credit: lare96, Gabbe
 */
open class GameLoader constructor(private val port: Int) {
    private val serviceLoader =
        Executors.newSingleThreadExecutor(ThreadFactoryBuilder().setNameFormat("GameLoadingThread").build())
    private val executor =
        Executors.newSingleThreadScheduledExecutor(ThreadFactoryBuilder().setNameFormat("GameThread").build())
    val engine: GameEngine = GameEngine()

    fun init() {
        Preconditions.checkState(!serviceLoader.isShutdown, "The bootstrap has been bound already!")
        executeServiceLoad()
        serviceLoader.shutdown()
    }

    @Throws(IOException::class, InterruptedException::class)
    fun finish() {
        check(serviceLoader.awaitTermination(15, TimeUnit.MINUTES)) { "The background service load took too long!" }
        val networkExecutor = Executors.newCachedThreadPool()
        val serverBootstrap = ServerBootstrap(NioServerSocketChannelFactory(networkExecutor, networkExecutor))
        serverBootstrap.pipelineFactory = PipelineFactory(HashedWheelTimer())
        serverBootstrap.bind(InetSocketAddress(port))
        executor.scheduleAtFixedRate(
            engine,
            0,
            GameSettings.ENGINE_PROCESSING_CYCLE_RATE.toLong(),
            TimeUnit.MILLISECONDS
        )
        TaskManager.submit(ServerTimeUpdateTask())
    }

    private fun executeServiceLoad() {
        /*if (GameSettings.MYSQL_ENABLED) {
			serviceLoader.execute(() -> MySQLController.init());
		}*/
        FileUtils.createSaveDirectories()
//        if (configuration!!.isDiscordBotEnabled) {
//            try {
//                JavaCord.init().get()
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            } catch (e: ExecutionException) {
//                e.printStackTrace()
//            }
//        }
        serviceLoader.execute { ConnectionHandler.init() }
        serviceLoader.execute { PlayerPunishment.init() }
        serviceLoader.execute { RegionClipping.init() }
        serviceLoader.execute { CustomObjects.init() }
        serviceLoader.execute { ItemDefinition.init() }
        serviceLoader.execute { Lottery.init() }
        serviceLoader.execute { GrandExchangeOffers.init() }
        serviceLoader.execute { Scoreboards.init() }
        serviceLoader.execute { WellOfGoodwill.init() }
        serviceLoader.execute { ClanChatManager.init() }
        serviceLoader.execute { CombatPoisonData.init() }
        serviceLoader.execute { CombatStrategies.init() }
        serviceLoader.execute { NpcDefinition.parseNpcs().load() }
        serviceLoader.execute { NPCDrops.load() }
        serviceLoader.execute { WeaponInterfaces.parseInterfaces().load() }
        serviceLoader.execute { ShopManager.load() }
        serviceLoader.execute { DialogueManager.parseDialogues().load() }
        serviceLoader.execute { NPC.init() }
    }
}