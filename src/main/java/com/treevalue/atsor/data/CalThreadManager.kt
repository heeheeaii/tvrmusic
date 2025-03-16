package com.treevalue.atsor.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object CalThreadManager : ThreadManager(Dispatchers.Default + SupervisorJob())
