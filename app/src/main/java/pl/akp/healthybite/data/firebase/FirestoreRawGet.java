package pl.akp.healthybite.data.firebase;

import androidx.annotation.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Forces the Java {@link DocumentSnapshot#get(String)} overload that returns a raw {@link Object}.
 * Kotlin call sites can otherwise bind to {@code get(field, Class)} or other overloads and trigger
 * unsafe casts (e.g. Long stored in Firestore read as String).
 */
public final class FirestoreRawGet {
    private FirestoreRawGet() {}

    @Nullable
    public static Object getField(DocumentSnapshot snapshot, String field) {
        return snapshot.get(field);
    }
}
