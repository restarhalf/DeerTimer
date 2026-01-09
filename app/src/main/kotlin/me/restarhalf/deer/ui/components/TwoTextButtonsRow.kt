package me.restarhalf.deer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton

@Composable
fun TwoTextButtonsRow(
    modifier: Modifier = Modifier,
    spacing: Dp = 16.dp,
    leftText: String,
    onLeftClick: () -> Unit,
    leftEnabled: Boolean = true,
    leftPrimary: Boolean = false,
    rightText: String,
    onRightClick: () -> Unit,
    rightEnabled: Boolean = true,
    rightPrimary: Boolean = true,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        TextButton(
            text = leftText,
            enabled = leftEnabled,
            colors = if (leftPrimary) {
                ButtonDefaults.textButtonColorsPrimary()
            } else {
                ButtonDefaults.textButtonColors()
            },
            onClick = onLeftClick,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.size(spacing))
        TextButton(
            text = rightText,
            enabled = rightEnabled,
            colors = if (rightPrimary) {
                ButtonDefaults.textButtonColorsPrimary()
            } else {
                ButtonDefaults.textButtonColors()
            },
            onClick = onRightClick,
            modifier = Modifier.weight(1f)
        )
    }
}
