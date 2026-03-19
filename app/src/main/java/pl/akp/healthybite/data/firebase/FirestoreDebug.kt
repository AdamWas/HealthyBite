package pl.akp.healthybite.data.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException

private const val TAG = "HealthyBiteFirestore"

/**
 * Centralized Firestore logging and user-facing error text for permission and network issues.
 */
object FirestoreDebug {

    fun log(context: String, t: Throwable) {
        val codeSuffix =
            if (t is FirebaseFirestoreException) " code=${t.code}" else ""
        Log.e(TAG, "$context$codeSuffix", t)
    }

    fun userMessage(t: Throwable): String =
        when {
            t is FirebaseFirestoreException &&
                t.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "Cloud access denied (Firestore rules). Open Firebase Console → Firestore → Rules and allow access for development, then retry."
            t is ClassCastException ->
                "Stored settings or cloud data has an unexpected type (${t.message}). The app reset the session; sign in again. If this repeats, clear app storage."
            else -> t.message?.takeIf { it.isNotBlank() } ?: "Something went wrong"
        }
}
