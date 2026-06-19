package com.firefly.controller.data.model

data class ApiResponse(
    val success: Boolean,
    val error: String? = null
)

data class StatusResponse(
    val success: Boolean,
    val status: String = "",
    val timestamp: Long = 0,
    val activeTasks: List<TaskInfo> = emptyList(),
    val recentTasks: List<TaskInfo> = emptyList(),
    val session: SessionInfo? = null,
    val error: String? = null
)

data class TaskInfo(
    val id: String,
    val status: String,
    val task: String?,
    val createdAt: Long? = null,
    val endedAt: Long? = null,
    val progress: String? = null
)

data class SessionInfo(
    val tokens: Int?,
    val model: String?,
    val updated: Long?
)

data class CommandRequest(
    val command: String
)

data class CommandResponse(
    val success: Boolean,
    val message: String?,
    val command: String?,
    val error: String? = null
)

data class TaskResponse(
    val success: Boolean,
    val tasks: List<TaskInfo>
)
