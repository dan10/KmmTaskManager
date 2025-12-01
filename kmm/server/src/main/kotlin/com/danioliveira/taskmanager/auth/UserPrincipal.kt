package com.danioliveira.taskmanager.auth

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UserPrincipal(
    val userId: Uuid,
)