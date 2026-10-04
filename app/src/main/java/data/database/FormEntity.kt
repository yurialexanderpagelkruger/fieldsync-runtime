package com.fieldsync.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forms")
data class FormEntity(
    @PrimaryKey val id: String,
    val deviceId: String,
    val agentName: String,
    val clientName: String,
    val formType: String,
    val payload: String,
    val status: String,
    val createdAt: String,
    val syncedAt: String?,
    val updatedAt: String,
    val syncAttempts: Int = 0
)
