package com.cil.shift.core.designsystem.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 28.sp,
    minFontSize: TextUnit = 10.sp,
    maxLines: Int = 1,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    style: TextStyle = LocalTextStyle.current
) {
    var fontSize by remember(text, maxFontSize) { mutableStateOf(maxFontSize) }
    var readyToDraw by remember(text, maxFontSize) { mutableStateOf(false) }

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        maxLines = maxLines,
        softWrap = false,
        textAlign = textAlign,
        letterSpacing = letterSpacing,
        overflow = TextOverflow.Ellipsis,
        style = style,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        onTextLayout = { result ->
            if (result.hasVisualOverflow && fontSize > minFontSize) {
                val nextSize = fontSize * 0.9f
                fontSize = if (nextSize.value < minFontSize.value) minFontSize else nextSize
            } else {
                readyToDraw = true
            }
        }
    )
}
