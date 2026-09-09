package com.hieu.ui_pinterest.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hieu.ui_pinterest.model.Pin
import com.hieu.ui_pinterest.ui.component.PinterestFeed


@Composable
fun PinterestScreen(pins: List<Pin>, modifier: Modifier = Modifier) {
    PinterestFeed(pins = pins, modifier = modifier)
}