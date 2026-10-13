package com.example.moneymanager.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.moneymanager.R

// Kinetic Ledger (PRD §5): Space Grotesk (SIL OFL) is the single UI voice —
// headers, labels, body. Figures stay in Paper Mono (tabular).
// PlayfairItalic kept only for legacy previews; new screens use SpaceGrotesk.
val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_400, FontWeight.Normal),
    Font(R.font.space_grotesk_500, FontWeight.Medium),
    Font(R.font.space_grotesk_600, FontWeight.SemiBold),
    Font(R.font.space_grotesk_700, FontWeight.Bold)
)

val PlayfairItalic = FontFamily(
    Font(R.font.playfair_italic_medium, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.playfair_italic_semibold, FontWeight.SemiBold, FontStyle.Italic)
)

val PaperMono = FontFamily(
    Font(R.font.paper_mono_regular, FontWeight.Normal),
    Font(R.font.paper_mono_medium, FontWeight.Medium),
    Font(R.font.paper_mono_semi_bold, FontWeight.SemiBold),
    Font(R.font.paper_mono_bold, FontWeight.Bold)
)
