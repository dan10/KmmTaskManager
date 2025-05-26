package com.danioliveira.taskmanager.navigation

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

object UuidNavType : NavType<Uuid>(isNullableAllowed = false) {

    override fun put(bundle: SavedState, key: String, value: Uuid) {
        bundle.write {
            putString(key, value.toString())
        }
    }

    override fun get(bundle: SavedState, key: String): Uuid? {
        return bundle.read {
            getString(key).takeIf { it.isNotEmpty() }?.let { parseValue(it) }
        }
    }

    override fun parseValue(value: String): Uuid {
        return Uuid.parseHexDash(value)
    }
}

fun uuidTypeMap(): Map<KType, NavType<Uuid>> {
    return mapOf(typeOf<Uuid>() to UuidNavType)
}