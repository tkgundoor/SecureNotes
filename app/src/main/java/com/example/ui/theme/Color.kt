package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// --- Editorial Aesthetic Palette ---
val EditorialBackground = Color(0xFFFDF7FF)       // Light, warm violet-ish paper background
val EditorialSurface = Color(0xFFFFFFFF)          // Crisp white for clean typography cards
val EditorialSurfaceVariant = Color(0xFFF3EDF7)   // Soft neutral for structural grouping & input fields
val EditorialPrimary = Color(0xFF6750A4)          // Elegant high-contrast deep purple
val EditorialSecondary = Color(0xFFE8DEF8)        // Refined lavender highlight
val EditorialBorder = Color(0xFFCAC4D0)           // Slim, editorial-grade card outline
val EditorialDarkText = Color(0xFF1D1B20)         // Slate/charcoal text for premium readability
val EditorialTextSecondary = Color(0xFF49454F)    // Muted grey for subheadings & metadata
val EditorialTitle = Color(0xFF21005D)            // Deepest indigo for majestic headings

// --- Editorial Alert & Status Palette ---
val PasswordsBg = Color(0xFFFFDAD6)               // Light rosy-pink for passwords
val PasswordsText = Color(0xFF410002)             // Deep crimson for password text
val RemindersBg = Color(0xFFD3E4FF)               // Soft periwinkle for reminders
val RemindersText = Color(0xFF001D36)             // Deep navy for reminder text

// --- Legacy Mapping for Retrofit Compatibility ---
// Mapping standard slate colors directly to Editorial Aesthetic equivalents
val SlateDark = EditorialBackground
val SlateCard = EditorialSurfaceVariant
val BluePrimary = EditorialPrimary
val TealAccent = Color(0xFFD0BCFF)                // Elegant light lavender tint
val CrimsonAlert = Color(0xFFBA1A1A)              // Editorial safety red
val EmeraldSafe = Color(0xFF136E35)               // Editorial success green
val GoldWarning = Color(0xFF7D5800)               // Editorial warning amber
val LavenderAccent = Color(0xFF6750A4)
val SlateGrey = EditorialBorder
val SlateTextSecondary = EditorialTextSecondary
