package com.urikai.app.ui.theme

import com.urikai.app.game.ScanlineStrength

object ScanlineStyle {

    fun alpha(strength: ScanlineStrength): Float = when (strength) {
        ScanlineStrength.OFF -> 0f
        ScanlineStrength.LOW -> 0.20f
        ScanlineStrength.HIGH -> 0.52f
    }

    fun step(strength: ScanlineStrength): Float = when (strength) {
        ScanlineStrength.OFF -> 4f
        ScanlineStrength.LOW -> 4f
        ScanlineStrength.HIGH -> 2.5f
    }

    fun stroke(strength: ScanlineStrength): Float = when (strength) {
        ScanlineStrength.OFF -> 1f
        ScanlineStrength.LOW -> 1f
        ScanlineStrength.HIGH -> 1.5f
    }
}
