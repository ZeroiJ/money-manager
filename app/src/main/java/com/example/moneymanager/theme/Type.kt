package com.example.moneymanager.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.moneymanager.R

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semi_bold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

val PlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_mono_semi_bold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_mono_bold, FontWeight.Bold)
)

// iOS redesign families (bundled, both SIL OFL): Geist Sans for UI text,
// Paper Mono for figures/labels. Inter/PlexMono stay until Task 11 cleanup.
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
