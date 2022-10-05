package com.realting.world.content.player.skill.agility

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.world.content.dialogue.DialogueManager
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc

/**
 * Messy as fuck, what ever
 * @author Gabriel Hannason
 */
enum class ObstacleData(val `object`: Int, private val mustWalk: Boolean) {
    /* GNOME COURSE */
    LOG(2295, true) {
        override fun cross(player: Player?) {
            player!!.skillAnimation = 762
            player.updateFlag.flag(Flag.APPEARANCE)
            player.moveTo(Position(2474, 3436))
            player.packetSender.sendMessage("You attempt to walk across the log..")
            TaskManager.submit(object : Task(1, player, false) {
                var tick = 7
                public override fun execute() {
                    tick--
                    player.movementQueue.walkStep(0, -1)
                    if (tick <= 0) stop()
                }

                override fun stop() {
                    setEventRunning(false)
                    player.setCrossedObstacle(0, true).setCrossingObstacle(false).skillAnimation = -1
                    Agility.addExperience(player, 60)
                    player.updateFlag.flag(Flag.APPEARANCE)
                    player.packetSender.sendMessage("You manage to safely make your way across the log.")
                }
            })
        }
    },
    NET(2285, false) {
        override fun cross(player: Player?) {
            player!!.performAnimation(Animation(828))
            player.packetSender.sendMessage("You climb the net..")
            TaskManager.submit(object : Task(1, player, false) {
                var tick = 0
                public override fun execute() {
                    if (tick == 2) {
                        player.moveTo(Position(2473, 3423, 1))
                        Agility.addExperience(player, 40)
                    } else if (tick == 3) {
                        player.setCrossedObstacle(1, true).isCrossingObstacle = false
                        stop()
                    }
                    tick++
                }
            })
        }
    },
    BRANCH(2313, false) {
        override fun cross(player: Player?) {
            player!!.performAnimation(Animation(828))
            player.packetSender.sendMessage("You climb the branch..")
            TaskManager.submit(object : Task(2, player, false) {
                public override fun execute() {
                    player.moveTo(Position(2473, 3420, 2))
                    Agility.addExperience(player, 42)
                    player.setCrossedObstacle(2, true).isCrossingObstacle = false
                    stop()
                }
            })
        }
    },
    ROPE(2312, true) {
        override fun cross(player: Player?) {
            player!!.skillAnimation = 762
            player.updateFlag.flag(Flag.APPEARANCE)
            player.packetSender.sendMessage("You attempt to walk across the rope..")
            TaskManager.submit(object : Task(1, player, false) {
                var tick = 0
                public override fun execute() {
                    tick++
                    player.movementQueue.walkStep(1, 0)
                    if (tick >= 6) stop()
                }

                override fun stop() {
                    setEventRunning(false)
                    player.setCrossedObstacle(3, true).setCrossingObstacle(false).skillAnimation = -1
                    Agility.addExperience(player, 25)
                    player.updateFlag.flag(Flag.APPEARANCE)
                    player.packetSender.sendMessage("You manage to safely walk across the rope.")
                }
            })
        }
    },
    BRANCH_2(2314, false) {
        override fun cross(player: Player?) {
            player!!.performAnimation(Animation(828))
            player.packetSender.sendMessage("You climb the branch..")
            TaskManager.submit(object : Task(2, player, false) {
                public override fun execute() {
                    player.moveTo(Position(player.position.x, player.position.y, 0))
                    Agility.addExperience(player, 42)
                    player.setCrossedObstacle(4, true).isCrossingObstacle = false
                    stop()
                }
            })
        }
    },
    NETS_2(2286, false) {
        override fun cross(player: Player?) {
            if (player!!.position.y != 3425) {
                player.isCrossingObstacle = false
                return
            }
            player.packetSender.sendMessage("You climb the net..")
            player.performAnimation(Animation(828))
            TaskManager.submit(object : Task(2, player, false) {
                public override fun execute() {
                    player.moveTo(Position(player.position.x, player.position.y + 2, 0))
                    Agility.addExperience(player, 15)
                    player.setCrossedObstacle(5, true).isCrossingObstacle = false
                    stop()
                }
            })
        }
    },
    PIPE_1(4058, true) {
        override fun cross(player: Player?) {
            player!!.moveTo(Position(2487, 3430))
            player.packetSender.sendMessage("You attempt to go through the pipe..")
            TaskManager.submit(object : Task(1, player, false) {
                var tick = 0
                public override fun execute() {
                    if (tick < 3 || tick >= 4) {
                        if (player.skillAnimation != 844) {
                            player.skillAnimation = 844
                            player.updateFlag.flag(Flag.APPEARANCE)
                        }
                    } else {
                        if (player.skillAnimation != -1) {
                            player.skillAnimation = -1
                            player.updateFlag.flag(Flag.APPEARANCE)
                        }
                    }
                    tick++
                    player.movementQueue.walkStep(0, 1)
                    if (tick >= 4) stop()
                }

                override fun stop() {
                    setEventRunning(false)
                    player.moveTo(Position(2487, 3437))
                    player.setCrossedObstacle(6, true).setCrossingObstacle(false).skillAnimation = -1
                    player.clickDelay.reset()
                    player.updateFlag.flag(Flag.APPEARANCE)
                    if (Agility.passedAllObstacles(player)) {
                        DialogueManager.start(player, DialogueManager.getDialogues()[57 + Misc.getRandom(2)])
                        player.inventory.add(2996, 2)
                        Agility.addExperience(player, 60)
                    } else {
                        DialogueManager.start(player, DialogueManager.getDialogues()[56])
                    }
                    Agility.resetProgress(player)
                    player.packetSender.sendMessage("You manage to make your way through the pipe.")
                }
            })
        }
    },
    PIPE_2(154, true) {
        override fun cross(player: Player?) {
            player!!.moveTo(Position(2484, 3430))
            player.packetSender.sendMessage("You attempt to go through the pipe..")
            TaskManager.submit(object : Task(1, player, false) {
                var tick = 0
                public override fun execute() {
                    if (tick < 3 || tick >= 4) {
                        if (player.skillAnimation != 844) {
                            player.skillAnimation = 844
                            player.updateFlag.flag(Flag.APPEARANCE)
                        }
                    } else {
                        if (player.skillAnimation != -1) {
                            player.skillAnimation = -1
                            player.updateFlag.flag(Flag.APPEARANCE)
                        }
                    }
                    tick++
                    player.movementQueue.walkStep(0, 1)
                    if (tick >= 4) stop()
                }

                override fun stop() {
                    setEventRunning(false)
                    player.moveTo(Position(2483, 3437))
                    player.setCrossedObstacle(6, true).setCrossingObstacle(false).skillAnimation = -1
                    player.clickDelay.reset()
                    player.updateFlag.flag(Flag.APPEARANCE)
                    if (Agility.passedAllObstacles(player)) {
                        DialogueManager.start(player, DialogueManager.getDialogues()[57 + Misc.getRandom(2)])
                        player.inventory.add(2996, 2)
                        Agility.addExperience(player, 220)
                    } else {
                        DialogueManager.start(player, DialogueManager.getDialogues()[56])
                    }
                    player.packetSender.sendMessage("You manage to make your way through the pipe.")
                    Agility.resetProgress(player)
                }
            })
        }
    },  /* BARBARIAN OUTPOST COURSE */
    ROPE_SWING(2282, true) {
        override fun cross(player: Player?) {
            Agility.resetProgress(player)
            player!!.packetSender.sendMessage("You attempt to swing on the ropeswing..")
            player.moveTo(Position(2551, 3554))
            player.performAnimation(Animation(751))
            val success = Agility.isSucessive(player)
            TaskManager.submit(object : Task(1, player, false) {
                var tick = 0
                public override fun execute() {
                    tick++
                    if (tick == 1) player.moveTo(Position(player.position.x, 3553, 0))
                    if (!success) {
                        player.moveTo(Position(2550, 9950, 0))
                        Agility.addExperience(player, 18)
                        player.dealDamage(Hit(null, Misc.getRandom(50), Hitmask.RED, CombatIcon.NONE))
                        player.packetSender.sendMessage("You failed to swing your way across.")
                        stop()
                        return
                    }
                    if (tick >= 3) {
                        player.moveTo(Position(player.position.x, 3549, 0))
                        stop()
                    }
                }

                override fun stop() {
                    setEventRunning(false)
                    player.setCrossedObstacle(0, if (success) true else false).isCrossingObstacle = false
                    Agility.addExperience(player, 75 * 3)
                    player.packetSender.sendMessage("You manage to swing yourself across.")
                }
            })
        }
    },
    LOG_2(2294, true) {
        override fun cross(player: Player?) {
            val fail = !Agility.isSucessive(player)
            player!!.packetSender.sendMessage("You attempt to walk-over the log..")
            player.skillAnimation = 762
            player.moveTo(Position(2550, 3546, 0))
            player.updateFlag.flag(Flag.APPEARANCE)
            TaskManager.submit(object : Task(1, player, true) {
                var tick = 0
                public override fun execute() {
                    tick++
                    player.movementQueue.walkStep(-1, 0)
                    if (tick >= 9 || player == null) stop()
                    if (tick == 5 && fail) {
                        stop()
                        tick = 0
                        player.movementQueue.reset()
                        player.performAnimation(Animation(764))
                        TaskManager.submit(object : Task(1, player, true) {
                            var tick2 = 0
                            public override fun execute() {
                                if (tick2 == 0) {
                                    player.moveTo(Position(2546, 3547))
                                    player.dealDamage(Hit(null, Misc.getRandom(50), Hitmask.RED, CombatIcon.NONE))
                                }
                                tick2++
                                player.skillAnimation = 772
                                player.updateFlag.flag(Flag.APPEARANCE)
                                player.movementQueue.walkStep(0, 1)
                                if (tick2 >= 4) {
                                    player.packetSender.sendMessage("You are unable to make your way across the log.")
                                    player.setCrossedObstacle(1, false).setCrossingObstacle(false).skillAnimation = -1
                                    Agility.addExperience(player, 5)
                                    player.updateFlag.flag(Flag.APPEARANCE)
                                    stop()
                                    return
                                }
                            }
                        })
                    }
                }

                override fun stop() {
                    setEventRunning(false)
                    if (!fail) {
                        player.setCrossedObstacle(1, true).setCrossingObstacle(false).skillAnimation = -1
                        Agility.addExperience(player, if (fail) 5 * 3 else 60 * 3)
                        player.updateFlag.flag(Flag.APPEARANCE)
                        player.moveTo(Position(2541, 3546))
                        player.packetSender.sendMessage("You safely make your way across the log.")
                    }
                }
            })
        }
    },
    NET_3(2284, false) {
        override fun cross(player: Player?) {
            player!!.performAnimation(Animation(828))
            player.packetSender.sendMessage("You climb the net..")
            TaskManager.submit(object : Task(2, player, false) {
                public override fun execute() {
                    player.moveTo(Position(2537 + Misc.getRandom(1), 3546 + Misc.getRandom(1), 1))
                    Agility.addExperience(player, 30 * 3)
                    player.setCrossedObstacle(2, true).setSkillAnimation(-1).isCrossingObstacle = false
                    stop()
                }
            })
        }
    },
    BALANCE_LEDGE(2302, true) {
        override fun cross(player: Player?) {
            if (player!!.position.x != 2536) {
                player.isCrossingObstacle = false
                return
            }
            player.packetSender.sendMessage("You attempt to make your way across the ledge..")
            val fallDown = !Agility.isSucessive(player)
            player.isCrossingObstacle = true
            player.moveTo(Position(2536, 3547, 1))
            TaskManager.submit(object : Task(1, player, false) {
                var tick = 0
                public override fun execute() {
                    tick++
                    player.skillAnimation = 756
                    player.updateFlag.flag(Flag.APPEARANCE)
                    player.movementQueue.walkStep(-1, 0)
                    if (tick == 3 && fallDown) {
                        player.performAnimation(Animation(761))
                        stop()
                        TaskManager.submit(object : Task(1) {
                            public override fun execute() {
                                player.moveTo(Position(2535, 3546, 0))
                                player.dealDamage(Hit(null, Misc.getRandom(50), Hitmask.RED, CombatIcon.NONE))
                                player.movementQueue.walkStep(0, -1)
                                player.setCrossedObstacle(3, false).skillAnimation = -1
                                player.updateFlag.flag(Flag.APPEARANCE)
                                Agility.addExperience(player, 6 * 3)
                                player.packetSender.sendMessage("You accidently slip and fall down!")
                                TaskManager.submit(object : Task(1) {
                                    public override fun execute() {
                                        player.isCrossingObstacle = false
                                        stop()
                                    }
                                })
                                stop()
                            }
                        })
                    } else if (tick == 4) {
                        player.setCrossedObstacle(3, true).setSkillAnimation(-1).isCrossingObstacle = false
                        player.updateFlag.flag(Flag.APPEARANCE)
                        Agility.addExperience(player, 40 * 3)
                        player.packetSender.sendMessage("You safely move across the ledge.")
                        stop()
                    }
                }
            })
        }
    },
    LADDER(3205, false) {
        override fun cross(player: Player?) {
            player!!.performAnimation(Animation(827))
            player.packetSender.sendMessage("You climb down the ladder...")
            TaskManager.submit(object : Task(1, player, false) {
                public override fun execute() {
                    player.moveTo(Position(2532, 3546, 0))
                    player.setCrossedObstacle(4, true).isCrossingObstacle = false
                    stop()
                }
            })
        }
    },
    RAMP(1948, false) {
        override fun cross(player: Player?) {
            if (player!!.position.x != 2535 && player.position.x != 2538 && player.position.x != 2542 && player.position.x != 2541) {
                player.packetSender.sendMessage("You cannot jump over the wall from this side!")
                player.isCrossingObstacle = false
                return
            }
            val first = player.position.x == 2535
            val oneStep = player.position.x == 2537 || player.position.x == 2542
            player.positionToFace = player.interactingObject.position.copy()
            player.packetSender.sendMessage("You attempt to jump over the wall...")
            player.performAnimation(Animation(1115))
            TaskManager.submit(object : Task(1, player, false) {
                public override fun execute() {
                    player.packetSender.sendClientRightClickRemoval()
                    player.moveTo(Position(player.position.x + if (oneStep) 1 else 2, 3553))
                    player.setCrossingObstacle(false).setCrossedObstacle(if (first) 5 else 6, true)
                    if (player.position.x == 2543 && player.position.y == 3553) {
                        if (Agility.passedAllObstacles(player)) {
                            DialogueManager.start(player, 57)
                            player.inventory.add(2996, 4)
                            Agility.addExperience(player, 265)
                            Agility.resetProgress(player)
                        } else {
                            DialogueManager.start(player, 56)
                        }
                        player.packetSender.sendMessage("You manage to jump over the wall.")
                    }
                    stop()
                }
            })
        }
    },
    ROPESWING_LADDER(1759, false) {
        override fun cross(player: Player?) {
            player!!.performAnimation(Animation(827))
            player.packetSender.sendMessage("You climb the ladder..")
            TaskManager.submit(object : Task(1) {
                public override fun execute() {
                    player.isCrossingObstacle = false
                    player.moveTo(
                        Position(
                            if (player.position.x > 2610) 2209 else 2546,
                            if (player.position.y < 3550) 5348 else 9951,
                            0
                        )
                    )
                    stop()
                }
            })
        }
    },  /* WILD COURSE */
    GATE_1(2309, true) {
        override fun cross(player: Player?) {
            player!!.moveTo(Position(2998, 3917))
            player.skillAnimation = 762
            player.updateFlag.flag(Flag.APPEARANCE)
            player.packetSender.sendMessage("You enter the gate and begin walking across the narrow path..")
            TaskManager.submit(object : Task(1, player, true) {
                var tick = 0
                public override fun execute() {
                    tick++
                    player.movementQueue.walkStep(0, 1)
                    if (player.position.y == 3930 || tick >= 15) {
                        player.moveTo(Position(2998, 3931, 0))
                        stop()
                    }
                }

                override fun stop() {
                    setEventRunning(false)
                    player.setCrossingObstacle(false).skillAnimation = -1
                    Agility.addExperience(player, 15)
                    player.updateFlag.flag(Flag.APPEARANCE)
                    Agility.resetProgress(player)
                    player.packetSender.sendMessage("You manage to make your way to the other side.")
                }
            })
        }
    },
    GATE_2(2308, true) {
        override fun cross(player: Player?) {
            player!!.packetSender.sendMessage("You enter the gate and begin walking across the narrow path..")
            player.moveTo(Position(2998, 3930))
            player.skillAnimation = 762
            player.updateFlag.flag(Flag.APPEARANCE)
            TaskManager.submit(object : Task(1, player, true) {
                var tick = 0
                public override fun execute() {
                    tick++
                    player.movementQueue.walkStep(0, -1)
                    if (player.position.y == 3917 || tick >= 15) {
                        player.moveTo(Position(2998, 3916))
                        stop()
                    }
                }

                override fun stop() {
                    setEventRunning(false)
                    player.setCrossingObstacle(false).skillAnimation = -1
                    Agility.addExperience(player, 15)
                    player.updateFlag.flag(Flag.APPEARANCE)
                    Agility.resetProgress(player)
                    player.packetSender.sendMessage("You manage to make your way to the other side.")
                }
            })
        }
    },
    GATE_3(2308, true) {
        override fun cross(player: Player?) {
            player!!.packetSender.sendMessage("You enter the gate and begin walking across the narrow path..")
            player.moveTo(Position(2998, 3930))
            player.skillAnimation = 762
            player.updateFlag.flag(Flag.APPEARANCE)
            TaskManager.submit(object : Task(1, player, true) {
                var tick = 0
                public override fun execute() {
                    tick++
                    player.movementQueue.walkStep(0, -1)
                    if (player.position.y == 3917 || tick >= 15) {
                        player.moveTo(Position(2998, 3916))
                        stop()
                    }
                }

                override fun stop() {
                    setEventRunning(false)
                    player.setCrossingObstacle(false).skillAnimation = -1
                    Agility.addExperience(player, 15)
                    player.updateFlag.flag(Flag.APPEARANCE)
                    Agility.resetProgress(player)
                    player.packetSender.sendMessage("You manage to make your way to the other side.")
                }
            })
        }
    },
    PIPE_3(2288, true) {
        override fun cross(player: Player?) {
            player!!.moveTo(Position(3004, 3937))
            player.skillAnimation = 844
            player.updateFlag.flag(Flag.APPEARANCE)
            player.packetSender.sendMessage("You attempt to squeeze through the pipe..")
            TaskManager.submit(object : Task(1, player, true) {
                var tick = 0
                public override fun execute() {
                    tick++
                    player.movementQueue.walkStep(0, 1)
                    if (tick == 4) player.moveTo(Position(3004, 3947)) else if (tick == 7) stop()
                }

                override fun stop() {
                    setEventRunning(false)
                    player.setCrossedObstacle(0, true).setCrossedObstacle(1, true).setCrossedObstacle(2, true)
                        .setCrossingObstacle(false).skillAnimation = -1
                    Agility.addExperience(player, 175)
                    player.updateFlag.flag(Flag.APPEARANCE)
                    player.packetSender.sendMessage("You manage to squeeze through the pipe.")
                }
            })
        }
    },
    ROPE_SWING_2(2283, true) {
        override fun cross(player: Player?) {
            if (player!!.position.y > 3953) {
                player.packetSender.sendMessage("You must be positioned infront of the Ropeswing to do that.")
                player.isCrossingObstacle = false
                return
            }
            player.packetSender.sendMessage("You attempt to swing on the ropeswing..")
            player.moveTo(Position(3005, 3953))
            player.performAnimation(Animation(751))
            player.positionToFace = Position(3005, 3960, 0)
            val fail = !Agility.isSucessive(player)
            TaskManager.submit(object : Task(1, player, true) {
                var tick = 0
                public override fun execute() {
                    tick++
                    if (tick == 2) {
                        if (fail) {
                            player.moveTo(Position(3004, 10356))
                            player.dealDamage(Hit(null, Misc.getRandom(60), Hitmask.RED, CombatIcon.NONE))
                            Agility.addExperience(player, 40)
                            player.packetSender.sendMessage("You failed to swing your way across.")
                            stop()
                            return
                        } else {
                            player.positionToFace = Position(3005, 3960, 0)
                            player.moveTo(Position(player.position.x, 3954, 0))
                        }
                    }
                    if (tick >= 3) {
                        player.moveTo(Position(player.position.x, 3958, 0))
                        player.positionToFace = Position(3005, 3960, 0)
                        stop()
                    }
                }

                override fun stop() {
                    setEventRunning(false)
                    player.packetSender.sendMessage("You manage to swing yourself across.")
                    player.setCrossedObstacle(3, if (fail) false else true).isCrossingObstacle = false
                    Agility.addExperience(player, if (fail) 10 else 250)
                }
            })
        }
    },
    STEPPING_STONES(9326, true) {
        override fun cross(player: Player?) {
            player!!.updateFlag.flag(Flag.APPEARANCE)
            player.packetSender.sendMessage("You attempt to pass the stones..")
            TaskManager.submit(object : Task(1, player, true) {
                var tick = 1
                public override fun execute() {
                    tick++
                    player.performAnimation(Animation(769))
                    if (tick == 4 || tick == 7 || tick == 10 || tick == 13 || tick == 16) {
                        player.moveTo(Position(player.position.x - 1, player.position.y))
                    } else if (tick >= 17) {
                        player.moveTo(Position(2996, 3960, 0))
                        //Agility.addExperience(player, 250);
                        stop()
                    }
                }

                override fun stop() {
                    setEventRunning(false)
                    player.setCrossedObstacle(4, true).isCrossingObstacle = false
                    Agility.addExperience(player, 300)
                    player.packetSender.sendMessage("You manage to pass the stones.")
                }
            })
        }
    },
    BALANCE_LEDGE_2(2297, true) {
        override fun cross(player: Player?) {
            player!!.moveTo(Position(3001, 3945, 0))
            player.skillAnimation = 762
            player.updateFlag.flag(Flag.APPEARANCE)
            val fail = !Agility.isSucessive(player)
            player.packetSender.sendMessage("You attempt to make your way over the log..")
            TaskManager.submit(object : Task(1, player, true) {
                var tick = 0
                public override fun execute() {
                    tick++
                    player.movementQueue.walkStep(-1, 0)
                    if (tick >= 7) stop() else if (fail && tick >= 3) {
                        player.moveTo(Position(3000, 10346))
                        player.dealDamage(Hit(null, Misc.getRandom(60), Hitmask.RED, CombatIcon.NONE))
                        stop()
                    }
                }

                override fun stop() {
                    setEventRunning(false)
                    player.setCrossedObstacle(5, if (fail) false else true).setCrossingObstacle(false).skillAnimation =
                        -1
                    Agility.addExperience(player, if (fail) 10 else 250)
                    player.updateFlag.flag(Flag.APPEARANCE)
                    player.packetSender.sendMessage("You manage to safely make your way over the log.")
                }
            })
        }
    },
    CLIMB_WALL(2994, false) {
        override fun cross(player: Player?) {
            player!!.performAnimation(Animation(828))
            player.packetSender.sendMessage("You attempt to climb up the wall..")
            TaskManager.submit(object : Task(2, player, false) {
                public override fun execute() {
                    player.packetSender.sendClientRightClickRemoval()
                    player.moveTo(Position(2996, 3933, 0))
                    stop()
                }

                override fun stop() {
                    setEventRunning(false)
                    player.setCrossedObstacle(6, true).isCrossingObstacle = false
                    Agility.addExperience(player, 100)
                    if (Agility.passedAllObstacles(player)) {
                        DialogueManager.start(player, 57)
                        player.inventory.add(2996, 6)
                        Agility.addExperience(player, 350)
                    } else {
                        DialogueManager.start(player, 56)
                    }
                    player.packetSender.sendMessage("You manage to climb up the wall.")
                    Agility.resetProgress(player)
                }
            })
        }
    },
    LADDER_2(14758, false) {
        override fun cross(player: Player?) {
            player!!.performAnimation(Animation(827))
            player.setEntityInteraction(null)
            player.isCrossingObstacle = false
            TaskManager.submit(object : Task(1) {
                public override fun execute() {
                    player.moveTo(Position(3005, 10362, 0))
                    stop()
                }
            })
        }
    },

    /**MISC */
    RED_DRAGON_LOG_BALANCE(5088, false) {
        override fun cross(player: Player?) {
            player!!.setCrossingObstacle(true).skillAnimation = 762
            player.updateFlag.flag(Flag.APPEARANCE)
            val moveX = if (player.position.x > 2683) 2686 else 2683
            player.moveTo(Position(moveX, 9506))
            TaskManager.submit(object : Task(1, player, true) {
                var tick = 0
                public override fun execute() {
                    if (tick < 4) player.movementQueue.walkStep(if (moveX == 2683) +1 else -1, 0) else if (tick == 4) {
                        player.setSkillAnimation(-1).isCrossingObstacle = false
                        player.updateFlag.flag(Flag.APPEARANCE)
                        Agility.addExperience(player, 32)
                        stop()
                    }
                    tick++
                }
            })
        }
    };

    fun mustWalk(): Boolean {
        return mustWalk
    }

    open fun cross(player: Player?) {}

    companion object {
        fun forId(`object`: Int): ObstacleData? {
            if (`object` == 2993 || `object` == 2328 || `object` == 2995 || `object` == 2994) return CLIMB_WALL else if (`object` == 2307) return GATE_2 else if (`object` == 5088 || `object` == 5090) return RED_DRAGON_LOG_BALANCE
            for (obstacleData in values()) {
                if (obstacleData.`object` == `object`) return obstacleData
            }
            return null
        }
    }
}