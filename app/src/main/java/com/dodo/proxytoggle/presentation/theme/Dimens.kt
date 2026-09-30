package com.dodo.proxytoggle.presentation.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Single source of truth for spacing, sizing and typography constants used on the proxy screen.
 */
object Dimens {
    // Screen padding
    val ScreenHorizontalPadding = 20.dp
    val ScreenBottomPadding = 8.dp

    // Top bar
    val TopBarHeight = 56.dp

    // Power button
    val SpacerAboveConnectButton = 78.dp
    val ConnectButtonBoxSize = 176.dp

    // Connect toggle
    val ToggleWidth = 88.dp
    val ToggleHeight = 44.dp
    val ToggleThumbSize = 34.dp
    val ToggleThumbPadding = 5.dp
    val ToggleBorderWidth = 2.dp

    // Status row
    val StatusRowHeight = 54.dp
    val StatusTextSize = 14.sp
    val StatusTextLetterSpacing = 0.22.em
    val StatusDropdownIconWidth = 44.dp
    val StatusDropdownIconHeight = 48.dp

    // Text fields
    val SpacerBeforeHostField = 18.dp
    val SpacerBeforePortField = 38.dp
    val SpacerAfterPortField = 64.dp
    val SpacerBeforeCopyright = 64.dp
    val CopyrightTextSize = 12.sp
    val TextFieldMinHeight = 72.dp
    val TextFieldFontSize = 20.sp
    val PortMaxLength = 5
}
