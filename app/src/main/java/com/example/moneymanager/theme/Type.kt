package com.example.moneymanager.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.moneymanager.R

// iOS redesign families (bundled, both SIL OFL): Geist Sans for UI text,
// Paper Mono for figures/labels.
// Nessie touch: Playfair Display Italic (OFL) for hero words — amounts stay
// in PaperMono, exactly like the reference (serif words, sans figures).
val PlayfairItalic = FontFamily(
    Font(R.font.playfair_italic_medium, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.playfair_italic_semibold, FontWeight.SemiBold, FontStyle.Italic)
)

// iOS redesign families (bundled, both SIL OFL): Geist Sans for UI text,
// Paper Mono for figures/labels.
val Geist = FontFamily(
    Font(R.font.geist_regular, FontWeight.Normal),
    Font(R.font.geist_medium, FontWeight.Medium),
    Font(R.font.geist_semi_bold, FontWeight.SemiBold),
    Font(R.font.geist_bold, FontWeight.Bold)
)

val PaperMono = FontFamily(
    Font(R.font.paper_mono_regular, FontWeight.Normal),
    Font(R.font.paper_mono_medium, FontWeight.Medium),
    Font(R.font.paper_mono_semi_bold, FontWeight.SemiBold),
    Font(R.font.paper_mono_bold, FontWeight.Bold)
)
