package com.dodo.proxytoggle.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dodo.proxytoggle.domain.model.SavedProxy
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dodo.proxytoggle.R
import com.dodo.proxytoggle.presentation.theme.Dimens
import com.dodo.proxytoggle.presentation.theme.ProxyTheme
import com.dodo.proxytoggle.presentation.theme.proxyColors

@Composable
fun ProxyScreen(
    viewModel: ProxyViewModel,
    onDarkModeChanged: (Boolean) -> Unit
) {
    val state = viewModel.state
    val context = LocalContext.current
    val toastMessage = state.message?.let { stringResource(it.text) }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            Toast.makeText(
                context,
                toastMessage,
                if (message.long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
            viewModel.clearMessage()
        }
    }
    androidx.compose.runtime.SideEffect { onDarkModeChanged(state.darkMode) }

    ProxyScreenContent(
        state = state,
        onToggleDarkMode = viewModel::toggleDarkMode,
        onShowDialog = viewModel::showDialog,
        onDismissDialog = viewModel::dismissDialog,
        onToggleProxy = viewModel::toggleProxy,
        onHostChanged = viewModel::onHostChanged,
        onPortChanged = viewModel::onPortChanged,
        onSelectSavedProxy = viewModel::selectSavedProxy,
        onRemoveSavedProxy = viewModel::removeSavedProxy
    )
}

@Composable
private fun ProxyScreenContent(
    state: ProxyUiState,
    onToggleDarkMode: () -> Unit,
    onShowDialog: (ProxyDialog) -> Unit,
    onDismissDialog: () -> Unit,
    onToggleProxy: () -> Unit,
    onHostChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onSelectSavedProxy: (SavedProxy) -> Unit = {},
    onRemoveSavedProxy: (SavedProxy) -> Unit = {}
) {
    val colors = proxyColors(state.darkMode)
    val (background, foreground, muted, accent, outline) = colors
    val focusManager = LocalFocusManager.current
    val hostFocus = remember { FocusRequester() }
    val portFocus = remember { FocusRequester() }
    var historyExpanded by remember { mutableStateOf(false) }
    val connectDescription = stringResource(
        if (state.connected) R.string.disconnect_proxy else R.string.connect_proxy
    )
    val statusLabel = stringResource(
        if (state.connected) R.string.connected_lowercase else R.string.disconnected_lowercase
    )
    val statusDescription = stringResource(R.string.proxy_status_accessibility, statusLabel)

    LaunchedEffect(state.hostInvalid) {
        if (state.hostInvalid) hostFocus.requestFocus()
    }
    LaunchedEffect(state.portInvalid) {
        if (state.portInvalid) portFocus.requestFocus()
    }

    ProxyTheme(darkMode = state.darkMode, colors = colors) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
        ) {
            Image(
                painter = painterResource(R.drawable.illustration_security_network),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.matchParentSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = Dimens.ScreenHorizontalPadding,
                        end = Dimens.ScreenHorizontalPadding,
                        bottom = Dimens.ScreenBottomPadding
                    )
            ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(Dimens.TopBarHeight),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onToggleDarkMode) {
                            Icon(
                                painter = painterResource(R.drawable.ic_theme),
                                contentDescription = stringResource(
                                    if (state.darkMode) R.string.switch_to_light_theme else R.string.switch_to_dark_theme
                                ),
                                tint = accent
                            )
                        }
                        IconButton(onClick = { onShowDialog(ProxyDialog.INFO) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = stringResource(R.string.proxy_information),
                                tint = accent
                            )
                        }
                    }

                    Spacer(Modifier.height(Dimens.SpacerAboveConnectButton))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(Dimens.ConnectButtonBoxSize),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        ProxyToggle(
                            checked = state.connected,
                            onToggle = onToggleProxy,
                            accent = accent,
                            muted = muted,
                            outline = outline,
                            background = background,
                            modifier = Modifier.semantics {
                                contentDescription = connectDescription
                            }
                        )
                    }

                    Row(
                modifier = Modifier.padding(top = Dimens.ScreenBottomPadding)
                    .fillMaxWidth()
                    .height(Dimens.StatusRowHeight)
                    .semantics {
                        contentDescription = statusDescription
                    }
                    .clickable { onShowDialog(ProxyDialog.STATUS) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        if (state.connected) R.string.connected else R.string.disconnected
                    ),
                    color = if (state.connected) accent else muted,
                    fontSize = Dimens.StatusTextSize,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = Dimens.StatusTextLetterSpacing,
                    textAlign = TextAlign.Center
                )
                Box(
                    Modifier.size(width = Dimens.StatusDropdownIconWidth, height = Dimens.StatusDropdownIconHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_drop_down),
                        contentDescription = null,
                        tint = muted
                    )
                }
            }

                    Spacer(Modifier.height(Dimens.SpacerBeforeHostField))
                    Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                value = state.host,
                onValueChange = onHostChanged,
                label = { Text(stringResource(R.string.ip_address)) },
                trailingIcon = if (state.history.isNotEmpty()) {
                    {
                        IconButton(onClick = { historyExpanded = !historyExpanded }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_drop_down),
                                contentDescription = stringResource(R.string.saved_proxies),
                                tint = if (historyExpanded) accent else muted
                            )
                        }
                    }
                } else null,
                isError = state.hostInvalid,
                supportingText = if (state.hostInvalid) {
                    { Text(stringResource(R.string.invalid_host)) }
                } else null,
                singleLine = true,
                textStyle = TextStyle(fontSize = Dimens.TextFieldFontSize),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = accent,
                    focusedTextColor = foreground,
                    unfocusedTextColor = foreground,
                    focusedBorderColor = accent,
                    unfocusedBorderColor = outline,
                    focusedLabelColor = accent,
                    unfocusedLabelColor = outline
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.TextFieldMinHeight).focusRequester(hostFocus)
            )
                    DropdownMenu(
                        expanded = historyExpanded && state.history.isNotEmpty(),
                        onDismissRequest = { historyExpanded = false }
                    ) {
                        state.history.forEach { proxy ->
                            DropdownMenuItem(
                                text = { Text(proxy.label) },
                                onClick = {
                                    onSelectSavedProxy(proxy)
                                    historyExpanded = false
                                },
                                trailingIcon = {
                                    IconButton(onClick = { onRemoveSavedProxy(proxy) }) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_close),
                                            contentDescription = stringResource(
                                                R.string.remove_saved_proxy,
                                                proxy.label
                                            ),
                                            tint = muted
                                        )
                                    }
                                }
                            )
                        }
                    }
                    }

                    Spacer(Modifier.height(Dimens.SpacerBeforePortField))
                    OutlinedTextField(
                value = state.port,
                onValueChange = { value ->
                    if (value.length <= Dimens.PortMaxLength && value.all(Char::isDigit)) {
                        onPortChanged(value)
                    }
                },
                label = { Text(stringResource(R.string.port)) },
                isError = state.portInvalid,
                supportingText = if (state.portInvalid) {
                    { Text(stringResource(R.string.invalid_port)) }
                } else null,
                singleLine = true,
                textStyle = TextStyle(fontSize = Dimens.TextFieldFontSize),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = accent,
                    focusedTextColor = foreground,
                    unfocusedTextColor = foreground,
                    focusedBorderColor = accent,
                    unfocusedBorderColor = outline,
                    focusedLabelColor = accent,
                    unfocusedLabelColor = outline
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.TextFieldMinHeight).focusRequester(portFocus)
            )
                    Spacer(Modifier.height(Dimens.SpacerBeforeCopyright))
                    Text(
                        text = stringResource(R.string.copyright_notice),
                        color = muted,
                        fontSize = Dimens.CopyrightTextSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(Dimens.SpacerAfterPortField))
                }
        }
        ProxyDialog(state = state, onDismissDialog = onDismissDialog)
    }
}

/**
 * A wider toggle than Material's fixed-size Switch, drawn to keep the thumb round.
 */
@Composable
private fun ProxyToggle(
    checked: Boolean,
    onToggle: () -> Unit,
    accent: Color,
    muted: Color,
    outline: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) {
            Dimens.ToggleWidth - Dimens.ToggleThumbSize - Dimens.ToggleThumbPadding * 2
        } else {
            0.dp
        },
        label = "toggleThumbOffset"
    )
    val trackColor by animateColorAsState(
        if (checked) accent.copy(alpha = 0.35f) else background,
        label = "toggleTrackColor"
    )
    val borderColor by animateColorAsState(if (checked) accent else outline, label = "toggleBorderColor")
    val thumbColor by animateColorAsState(if (checked) accent else muted, label = "toggleThumbColor")

    Box(
        modifier = modifier
            .size(width = Dimens.ToggleWidth, height = Dimens.ToggleHeight)
            .clip(CircleShape)
            .background(trackColor)
            .border(Dimens.ToggleBorderWidth, borderColor, CircleShape)
            .toggleable(value = checked, role = Role.Switch, onValueChange = { onToggle() })
            .padding(Dimens.ToggleThumbPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            Modifier
                .offset(x = thumbOffset)
                .size(Dimens.ToggleThumbSize)
                .background(thumbColor, CircleShape)
        )
    }
}

@Composable
private fun ProxyDialog(state: ProxyUiState, onDismissDialog: () -> Unit) {
    val dialog = state.dialog ?: return
    val context = LocalContext.current
    val command = stringResource(R.string.adb_grant_command)
    val commandLabel = stringResource(R.string.adb_command_label)
    val message = when (dialog) {
        ProxyDialog.PERMISSION -> stringResource(R.string.permission_message, command)
        ProxyDialog.INFO -> stringResource(R.string.info_message)
        ProxyDialog.STATUS -> when {
            state.connected -> stringResource(
                R.string.status_using_proxy,
                state.currentProxy.orEmpty()
            )
            state.currentProxy.isNullOrBlank() -> stringResource(R.string.status_no_proxy)
            else -> stringResource(R.string.status_other_proxy, state.currentProxy)
        }
    }
    AlertDialog(
        onDismissRequest = onDismissDialog,
        title = if (dialog == ProxyDialog.PERMISSION) {
            { Text(stringResource(R.string.allow_proxy_control)) }
        } else null,
        text = {
            Column {
                Text(message)
                Spacer(Modifier.height(Dimens.SpacerBeforeCopyright))
                Text(
                    text = stringResource(R.string.copyright_notice),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = if (dialog == ProxyDialog.PERMISSION) {
            {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(commandLabel, command))
                    Toast.makeText(context, R.string.adb_command_copied, Toast.LENGTH_SHORT).show()
                    onDismissDialog()
                }) { Text(stringResource(R.string.copy_command)) }
            }
        } else {
            { TextButton(onClick = onDismissDialog) { Text(stringResource(R.string.close)) } }
        },
        dismissButton = if (dialog == ProxyDialog.PERMISSION) {
            { TextButton(onClick = onDismissDialog) { Text(stringResource(R.string.close)) } }
        } else null
    )
}

private fun previewState(darkMode: Boolean, connected: Boolean) = ProxyUiState(
    host = "192.168.1.10",
    port = "8080",
    darkMode = darkMode,
    connected = connected,
    currentProxy = if (connected) "192.168.1.10:8080" else null,
    history = listOf(SavedProxy("192.168.1.10", 8080), SavedProxy("10.0.0.5", 8888))
)

@Preview(name = "Light - Disconnected", showBackground = true)
@Composable
private fun ProxyScreenLightDisconnectedPreview() {
    ProxyScreenContent(
        state = previewState(darkMode = false, connected = false),
        onToggleDarkMode = {},
        onShowDialog = {},
        onDismissDialog = {},
        onToggleProxy = {},
        onHostChanged = {},
        onPortChanged = {}
    )
}

@Preview(name = "Dark - Connected", showBackground = true)
@Composable
private fun ProxyScreenDarkConnectedPreview() {
    ProxyScreenContent(
        state = previewState(darkMode = true, connected = true),
        onToggleDarkMode = {},
        onShowDialog = {},
        onDismissDialog = {},
        onToggleProxy = {},
        onHostChanged = {},
        onPortChanged = {}
    )
}
