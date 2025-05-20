package com.aviadkorakin.search_and_dropdown

import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * Emits the current text on every change, conflating rapid updates.
 */
fun EditText.textChanges(): Flow<CharSequence> = callbackFlow {
    // Listen for text changes
    val listener = doOnTextChanged { text, _, _, _ ->
        trySend(text ?: "")
    }
    // When the coroutine is cancelled, remove the listener
    awaitClose { removeTextChangedListener(listener) }
}.conflate()
