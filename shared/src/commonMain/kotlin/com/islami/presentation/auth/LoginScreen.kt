package com.islami.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import islami_kmp.shared.generated.resources.Res
import islami_kmp.shared.generated.resources.ic_app
import org.jetbrains.compose.resources.painterResource
import com.islami.presentation.components.StarPattern
import com.islami.presentation.theme.IslamiBrand
import org.koin.compose.koinInject

/** Above this width the brand panel sits beside the form instead of above it. */
private val SplitLayoutBreakpoint = 900.dp

/** Signing in flips [SessionStateHolder] to SignedIn, which moves the app to Home. */
@Composable
fun LoginScreen(
    stateHolder: LoginStateHolder = koinInject()
) {
    val state by stateHolder.state.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (maxWidth >= SplitLayoutBreakpoint) {
            Row(modifier = Modifier.fillMaxSize()) {
                BrandPanel(compact = false, modifier = Modifier.weight(1f).fillMaxHeight())
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    LoginForm(state, stateHolder, modifier = Modifier.widthIn(max = 400.dp).padding(32.dp))
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrandPanel(compact = true, modifier = Modifier.fillMaxWidth())
                LoginForm(
                    state, stateHolder,
                    modifier = Modifier.widthIn(max = 440.dp).padding(horizontal = 24.dp, vertical = 32.dp)
                )
            }
        }
    }
}

// region Brand

@Composable
private fun BrandPanel(compact: Boolean, modifier: Modifier) {
    Box(modifier = modifier.background(IslamiBrand.Forest), contentAlignment = Alignment.Center) {
        StarPattern(
            color = IslamiBrand.Gold.copy(alpha = 0.08f),
            spacing = if (compact) 72.dp else 96.dp,
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = if (compact) 32.dp else 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 88.dp else 132.dp)
                    .clip(CircleShape)
                    .background(IslamiBrand.OnForest.copy(alpha = 0.06f))
                    .padding(if (compact) 12.dp else 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(Res.drawable.ic_app), contentDescription = "Islami logo")
            }
            Spacer(Modifier.height(if (compact) 16.dp else 28.dp))
            Text(
                "Islami Admin",
                style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                color = IslamiBrand.OnForest
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "لوحة تحكم إسلامي",
                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                color = IslamiBrand.Gold
            )
            if (!compact) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Manage azkar, duas, notifications and app releases for the Islami app.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = IslamiBrand.OnForest.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 360.dp)
                )
            }
        }
    }
}

// endregion

// region Form

@Composable
private fun LoginForm(state: LoginState, stateHolder: LoginStateHolder, modifier: Modifier) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val submit = { if (!state.isLoading) stateHolder.login(password) }

    LaunchedEffect(Unit) { emailFocus.requestFocus() }

    Column(modifier = modifier.fillMaxWidth()) {
        Text("Welcome back", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Sign in with your admin account",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = stateHolder::onEmailChanged,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            enabled = !state.isLoading,
            isError = state.error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().focusRequester(emailFocus)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                stateHolder.onPasswordChanged()
            },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }, modifier = Modifier.padding(end = 4.dp)) {
                    Text(if (showPassword) "Hide" else "Show")
                }
            },
            singleLine = true,
            enabled = !state.isLoading,
            isError = state.error != null,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().focusRequester(passwordFocus)
        )

        state.error?.let { error ->
            Spacer(Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = submit,
            enabled = !state.isLoading,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(12.dp))
                Text("Signing in…")
            } else {
                Text("Sign in", style = MaterialTheme.typography.titleSmall)
            }
        }

        Spacer(Modifier.height(32.dp))
        Text(
            "Admin access only. Ask the team for an account if you need one.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// endregion
