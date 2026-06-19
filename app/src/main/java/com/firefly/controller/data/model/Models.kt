package com.firefly.controller.data.model

data class SystemStats(
    val uptime: String = "",
    val cpu_load: String = "",
    val memory_total: Int = 0,
    val memory_used: Int = 0,
    val memory_percent: Int = 0,
    val disk_total: Double = 0.0,
    val disk_used: Double = 0.0,
    val disk_percent: Int = 0,
    val temperature: Double = 0.0,
    val battery: Int = 0,
    val battery_status: String = "unknown"
)

data class StatusResponse(
    val success: Boolean,
    val status: String = "",
    val timestamp: Long = 0,
    val system: SystemStats = SystemStats(),
    val activeTasks: List<TaskInfo> = emptyList(),
    val recentTasks: List<TaskInfo> = emptyList(),
    val session: SessionInfo? = null,
    val error: String? = null
)
