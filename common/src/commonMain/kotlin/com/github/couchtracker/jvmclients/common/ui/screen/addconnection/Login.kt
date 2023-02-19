@file:OptIn(ExperimentalAnimationApi::class)

package com.github.couchtracker.jvmclients.common.ui.screen.addconnection

import androidx.compose.animation.*
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.github.couchtracker.jvmclients.common.data.CouchTrackerServer
import com.github.couchtracker.jvmclients.common.data.api.AuthenticationInfo
import com.github.couchtracker.jvmclients.common.utils.blend
import com.github.couchtracker.jvmclients.common.utils.rememberStateFlow
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import mu.KotlinLogging

private sealed interface LoginState {

    val login: String
    val password: String
    fun withLogin(login: String): LoginState = Initial(login, password)
    fun withPassword(password: String): LoginState = Initial(login, password)

    data class Initial(
        override val login: String = "",
        override val password: String = "",
    ) : LoginState {
        fun load() = Loading(login, password)
    }

    data class Loading(
        override val login: String,
        override val password: String,
    ) : LoginState

    data class Error(
        override val login: String,
        override val password: String,
        val error: Exception,
    ) : LoginState {
        val errorMessage = when (error) {
            is ClientRequestException -> if (error.response.status == HttpStatusCode.Unauthorized) {
                "Incorrect login and/or password"
            } else null

            else -> null
        }

        fun retry() = Loading(login, password)
    }

    data class Ok(
        override val login: String,
        override val password: String,
        val authenticationInfo: AuthenticationInfo,
    ) : LoginState
}

private val logger = KotlinLogging.logger("Login")

@Composable
fun Login(
    modifier: Modifier,
    back: () -> Unit,
    server: CouchTrackerServer,
    onLogin: (CouchTrackerServer, AuthenticationInfo) -> Unit,
) {
    var state by remember { mutableStateOf<LoginState>(LoginState.Initial()) }
    val stateTransition = updateTransition(targetState = state)

    val stateFlow = rememberStateFlow(state) {
        it.mapLatest { ls ->
            if (ls is LoginState.Loading) {
                try {
                    val authenticationInfo = server.login(ls.login, ls.password)
                    LoginState.Ok(ls.login, ls.password, authenticationInfo)
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        logger.error(e) { "Error logging into server '${server.address}' with login '${ls.login}'" }
                    }
                    LoginState.Error(ls.login, ls.password, e)
                }
            } else null
        }
    }
    LaunchedEffect(Unit) {
        stateFlow.collectLatest { s ->
            if (s != null && s.login == state.login && s.password == state.password) {
                if (s is LoginState.Ok) {
                    onLogin(server, s.authenticationInfo)
                } else {
                    state = s
                }
            }
        }
    }

    Column(
        modifier.fillMaxSize().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AddConnectionStyle.BoxElement {
            Text(
                "Login to ${server.address}",
                style = MaterialTheme.typography.body2,
            )
        }
        AddConnectionStyle.Element {
            Column {
                val focusManager = LocalFocusManager.current
                val focusRequester = FocusRequester()
                OutlinedTextField(
                    state.login,
                    { state = state.withLogin(it) },
                    Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true,
                    label = { Text("Username or email") },
                    isError = state is LoginState.Error,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email
                    ),
                    keyboardActions = KeyboardActions {
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.AccountCircle, null)
                    },
                    shape = MaterialTheme.shapes.medium,
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                var isPasswordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    state.password,
                    { state = state.withPassword(it) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    isError = state is LoginState.Error,
                    singleLine = true,
                    visualTransformation = if (!isPasswordVisible) PasswordVisualTransformation()
                    else VisualTransformation.None,
                    trailingIcon = {
                        IconButton({ isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Password Visibility"
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Password, null)
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    shape = MaterialTheme.shapes.medium,
                )
            }
        }
        stateTransition.AnimatedContent(
            transitionSpec = {
                (fadeIn() with fadeOut()).using(SizeTransform(clip = false))
            },
        ) { state ->
            if (state is LoginState.Error) {
                AddConnectionStyle.ErrorMessage(state.errorMessage)
            } else Spacer(Modifier.fillMaxWidth())
        }
        AddConnectionStyle.Element(contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FloatingActionButton(
                    { back() },
                    Modifier.size(40.dp),
                    backgroundColor = MaterialTheme.colors.primary.blend(MaterialTheme.colors.background, 0.8f),
                    contentColor = MaterialTheme.colors.primary,
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, "Back")
                }
                Spacer(Modifier.width(16.dp))

                when (val s = state) {
                    is LoginState.Initial ->
                        FloatingActionButton(
                            { state = s.load() },
                            backgroundColor = MaterialTheme.colors.primary,
                        ) { Icon(Icons.Default.Done, "Next") }

                    is LoginState.Loading -> FloatingActionButton(
                        { },
                        backgroundColor = MaterialTheme.colors.primary,
                    ) {
                        CircularProgressIndicator(
                            Modifier.size(24.dp),
                            color = LocalContentColor.current
                        )
                    }

                    is LoginState.Error -> FloatingActionButton(
                        { state = s.retry() },
                        backgroundColor = MaterialTheme.colors.error,
                    ) {
                        Icon(Icons.Filled.Refresh, "Retry")
                    }

                    is LoginState.Ok -> throw IllegalStateException()
                }

                Spacer(Modifier.width(56.dp))
            }
        }
    }
}