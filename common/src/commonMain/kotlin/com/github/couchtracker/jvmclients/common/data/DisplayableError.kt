package com.github.couchtracker.jvmclients.common.data

import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

data class DisplayableError(
    val specificMessage: String?,
    val specificIcon: ImageVector? = null,
    val retryAction: ErrorAction.Retry? = null,
    val resolveAction: ErrorAction.Resolve? = null,
) {
    val retryable get() = retryAction != null
    val message: String get() = specificMessage ?: "Unknown error"
    val icon: ImageVector get() = specificIcon ?: Icons.Filled.Error
    val action = resolveAction ?: retryAction

    companion object {
        fun fromException(
            t: Throwable,
            retry: (() -> Unit)? = null,
            resolveAuthentication: () -> Unit,
        ): DisplayableError {
            var retryAction = retry?.let { ErrorAction.Retry(retry) }
            var resolveAction: ErrorAction.Resolve? = null
            val message = when (t) {
                is ClientRequestException -> when (t.response.status) {
                    HttpStatusCode.Unauthorized -> {
                        retryAction = null
                        resolveAction = ErrorAction.Resolve("Re-authenticate", resolveAuthentication)
                        "Authentication error"
                    }

                    HttpStatusCode.NotFound -> "Item not found"
                    else -> "Download error"
                }

                else -> null
            }
            return DisplayableError(
                message,
                retryAction = retryAction,
                resolveAction = resolveAction,
            )
        }
    }
}

fun List<DisplayableError>.merge(): DisplayableError {
    val retries = mapNotNull { it.retryAction }
    return DisplayableError(
        firstNotNullOfOrNull { it.specificMessage },
        firstNotNullOfOrNull { it.specificIcon },
        if (retries.isEmpty()) null else ErrorAction.Retry { retries.forEach { it.action.invoke() } },
        firstNotNullOfOrNull { it.resolveAction },
    )
}

sealed class ErrorAction(
    val name: String,
    val action: () -> Unit,
) {
    class Retry(
        action: () -> Unit,
    ) : ErrorAction("Retry", action)

    class Resolve(
        name: String,
        action: () -> Unit,
    ) : ErrorAction(name, action)
}
