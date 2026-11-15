package com.cst3115.enterprise.techfix.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val TechFixColors = lightColorScheme(
    primary = Color(0xFF2457D6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EEFF),
    onPrimaryContainer = Color(0xFF153577),

    secondary = Color(0xFF126B57),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDF5EB),
    onSecondaryContainer = Color(0xFF084D3C),

    background = Color(0xFFF4F6FA),
    onBackground = Color(0xFF14213D),

    surface = Color.White,
    onSurface = Color(0xFF14213D),
    surfaceVariant = Color(0xFFEAF0F7),
    onSurfaceVariant = Color(0xFF526078),

    outline = Color(0xFF718096),
    outlineVariant = Color(0xFFDCE3ED),

    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFFEDEA),
    onErrorContainer = Color(0xFF7D1712)
)

@Composable
fun TechFixTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TechFixColors,
        typography = Typography(),
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}