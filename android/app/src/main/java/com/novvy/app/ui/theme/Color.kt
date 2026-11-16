package com.novvy.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand ────────────────────────────────────────────────────────────────
val Violet = Color(0xFF6C4CF1)
val VioletDark = Color(0xFF5A3BE0)
val Cyan = Color(0xFF22D3EE)
val Coral = Color(0xFFFF8A5B)
val Blue = Color(0xFF3B82F6)

// ── Semantic ─────────────────────────────────────────────────────────────
val SuccessGreen = Color(0xFF22C55E)
val WarningAmber = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)
val PausedOrange = Color(0xFFFB923C)

// ── Neutrals · light ──────────────────────────────────────────────────────
val LightBg = Color(0xFFF7F6FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEEECF8)
val LightOnSurface = Color(0xFF1A1A2E)
val LightOnSurfaceVariant = Color(0xFF5A5A72)
val LightOutline = Color(0xFFDAD7EA)

// ── Neutrals · dark ───────────────────────────────────────────────────────
val DarkBg = Color(0xFF0F0F17)
val DarkSurface = Color(0xFF17171F)
val DarkElevated = Color(0xFF1F1F29)
val DarkSurfaceVariant = Color(0xFF23232F)
val DarkOnSurface = Color(0xFFE8E8F0)
val DarkOnSurfaceVariant = Color(0xFFA0A0B8)
val DarkOutline = Color(0xFF33334A)

// ── Gradients ────────────────────────────────────────────────────────────
val GradientVioletCyan = listOf(Violet, Cyan)
val GradientVioletCoral = listOf(Violet, Coral)
val GradientCyanBlue = listOf(Cyan, Blue)
val GradientGreenCyan = listOf(SuccessGreen, Cyan)
val GradientRedViolet = listOf(Color(0xFFFF6B6B), Violet)
val GradientBlueCyan = listOf(Blue, Cyan)
val GradientVioletBlue = listOf(Violet, Blue)

// ── Glows (used at low alpha behind accents) ──────────────────────────────
val VioletGlow = Color(0xFF8B7BFF)
val CyanGlow = Color(0xFF67E8F9)
val AmberGlow = Color(0xFFFFC46B)
val GreenGlow = Color(0xFF5BE58A)