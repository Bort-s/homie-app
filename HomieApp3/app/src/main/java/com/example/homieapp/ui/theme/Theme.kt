package com.example.homieapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.example.homieapp.R

@Composable
private fun darkColorSchemeFromXml() = darkColorScheme(
    background = colorResource(id = R.color.dark_background),
    surface = colorResource(id = R.color.dark_surface),
    primary = colorResource(id = R.color.dark_primary_text),
    secondary = colorResource(id = R.color.dark_secondary_text),
)

@Composable
private fun lightColorSchemeFromXml() = lightColorScheme(
    background = colorResource(id = R.color.light_background),
    surface = colorResource(id = R.color.light_surface),
    primary = colorResource(id = R.color.light_primary_text),
    secondary = colorResource(id = R.color.light_secondary_text),
)

@Composable
fun HomieAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorSchemeFromXml()
        else -> lightColorSchemeFromXml()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}