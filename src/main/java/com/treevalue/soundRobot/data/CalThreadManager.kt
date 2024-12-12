package com.treevalue.soundRobot.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object CalThreadManager : ThreadManager(Dispatchers.Default + SupervisorJob())
