package pl.akp.healthybite.data.firebase

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import pl.akp.healthybite.domain.model.MealType

private const val TAG = "HealthyBiteFirestoreMap"

/** Raw field value via Java interop (avoids Kotlin overload resolution on [DocumentSnapshot.get]). */
fun DocumentSnapshot.getRawField(field: String): Any? = FirestoreRawGet.getField(this, field)

/**
 * Safe coercion for Firestore field values (types vary: Long vs Int vs Double vs String).
 */
fun Any?.asFirestoreString(): String? = when (this) {
    null -> null
    is String -> this
    is Long -> toString()
    is Int -> toString()
    is Double -> if (this % 1.0 == 0.0) toLong().toString() else toString()
    is Float -> if (this % 1f == 0f) toLong().toString() else toString()
    is Boolean -> toString()
    else -> toString()
}

fun Any?.asFirestoreInt(): Int? = when (this) {
    null -> null
    is Number -> this.toInt()
    is String -> this.trim().toIntOrNull()
    else -> null
}

fun Any?.asFirestoreLong(): Long? = when (this) {
    null -> null
    is Number -> this.toLong()
    is String -> this.trim().toLongOrNull()
    else -> null
}

fun Any?.asFirestoreBoolean(default: Boolean = false): Boolean = when (this) {
    is Boolean -> this
    is Number -> this.toInt() != 0
    is String -> this.equals("true", ignoreCase = true) || this == "1"
    else -> default
}

fun Any?.asMealTypeOrNull(): MealType? = when (this) {
    null -> null
    is String -> try {
        MealType.valueOf(this)
    } catch (_: IllegalArgumentException) {
        null
    }
    is Number -> MealType.entries.getOrNull(this.toInt())
    else -> try {
        MealType.valueOf(this.toString())
    } catch (_: IllegalArgumentException) {
        null
    }
}

fun DocumentSnapshot.getStringCoerced(field: String): String? =
    FirestoreRawGet.getField(this, field).asFirestoreString()

fun DocumentSnapshot.getIntCoerced(field: String, default: Int = 0): Int =
    FirestoreRawGet.getField(this, field).asFirestoreInt() ?: default

fun DocumentSnapshot.getLongCoerced(field: String, default: Long = 0L): Long =
    FirestoreRawGet.getField(this, field).asFirestoreLong() ?: default

fun logFirestoreMalformedItem(
    collection: String,
    docId: String,
    detail: String
) {
    Log.w(TAG, "Skipping malformed item: collection=$collection doc=$docId — $detail")
}
