package dev.thebjoredcraft.gecko.server.resourcepack.bitmap

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import kotlin.math.absoluteValue

object GeckoBitmaps {

    private const val BG = '\uA441'

    private const val N1 = '\uE101'
    private const val N2 = '\uE102'
    private const val N4 = '\uE103'
    private const val N8 = '\uE104'
    private const val N16 = '\uE105'
    private const val N32 = '\uE106'
    private const val N64 = '\uE107'
    private const val N128 = '\uE108'
    private const val N256 = '\uE109'
    private const val N512 = '\uE110'

    private val CHAR_MAP: Map<Char, GeckoGlyph> = buildMap {
        for (i in 0 until 26) put(
            ('a'.code + i).toChar(),
            GeckoGlyph(('\uA411'.code + i).toChar(), 6)
        )
        for (i in 0 until 10) put(
            ('0'.code + i).toChar(),
            GeckoGlyph(('\uA42F'.code + i).toChar(), 6)
        )

        put('+', GeckoGlyph('\uA42B', 6))
        put('-', GeckoGlyph('\uA42C', 6))
        put('/', GeckoGlyph('\uA42D', 6))
        put('\\', GeckoGlyph('\uA42E', 6))
        put('_', GeckoGlyph('\uA439', 6))
        put('%', GeckoGlyph('\uA43A', 6))
        put('=', GeckoGlyph('\uA43B', 6))
        put('#', GeckoGlyph('\uA43C', 6))
        put('*', GeckoGlyph('\uA43D', 6))

        put('\u00B4', GeckoGlyph('\uA442', 7))  // ´ acute accent
        put('&', GeckoGlyph('\uA443', 7))
        put(')', GeckoGlyph('\uA444', 6))
        put('(', GeckoGlyph('\uA445', 6))
        put('^', GeckoGlyph('\uA446', 6))
        put(':', GeckoGlyph('\uA447', 6))
        put(',', GeckoGlyph('\uA449', 6))
        put('{', GeckoGlyph('\uA44A', 6))
        put('}', GeckoGlyph('\uA44B', 6))
        put('\u00B0', GeckoGlyph('\uA44C', 6))  // ° degree
        put('$', GeckoGlyph('\uA44D', 8))
        put('.', GeckoGlyph('\uA454', 6))
        put('"', GeckoGlyph('\uA455', 6))
        put('\u20AC', GeckoGlyph('\uA456', 9))  // € euro
        put('!', GeckoGlyph('\uA457', 6))
        put('`', GeckoGlyph('\uA458', 6))
        put('>', GeckoGlyph('\uA459', 6))
        put('<', GeckoGlyph('\uA45B', 6))
        put('|', GeckoGlyph('\uA45C', 6))
        put('?', GeckoGlyph('\uA45D', 6))
        put('\u00A7', GeckoGlyph('\uA45E', 8))  // § section
        put(';', GeckoGlyph('\uA45F', 6))
        put('\'', GeckoGlyph('\uA460', 6))
        put(']', GeckoGlyph('\uA461', 6))
        put('[', GeckoGlyph('\uA462', 6))
        put('~', GeckoGlyph('\uA463', 6))
    }

    fun text(
        text: String,
        foreground: TextColor,
        background: TextColor,
        affixAmount: Int = 0,
    ): Component {
        val glyphs = text.lowercase().mapNotNull { CHAR_MAP[it] }
        if (glyphs.isEmpty()) {
            return Component.empty()
        }


        val bgBuilder = StringBuilder()
        for ((index, glyph) in glyphs.withIndex()) {
            for (i in 0 until glyph.width) {
                if (i > 0) bgBuilder.append(N1)
                bgBuilder.append(BG)
            }
            if (index < glyphs.lastIndex) bgBuilder.append(N1)
        }

        repeat(affixAmount) {
            bgBuilder.append(N1).append(BG)
        }

        if (bgBuilder.isNotEmpty()) {
            bgBuilder.deleteCharAt(bgBuilder.length - 1)
        }

        val totalWidth = glyphs.sumOf { it.width } - 1
        val spacingBack = negativeSpacing(totalWidth)

        val glyphStr = glyphs.joinToString("") { it.char.toString() }

        return Component.text()
            .append(Component.text(bgBuilder.toString()).color(background))
            .append(Component.text(spacingBack))
            .append(Component.text(glyphStr).color(foreground))
            .build()
    }

    @Suppress("UNUSED_VALUE")
    private fun negativeSpacing(amount: Int): String {
        var remaining = amount.absoluteValue

        return buildString {
            if (remaining >= 512) {
                append(N512); remaining -= 512
            }
            if (remaining >= 256) {
                append(N256); remaining -= 256
            }
            if (remaining >= 128) {
                append(N128); remaining -= 128
            }
            if (remaining >= 64) {
                append(N64); remaining -= 64
            }
            if (remaining >= 32) {
                append(N32); remaining -= 32
            }
            if (remaining >= 16) {
                append(N16); remaining -= 16
            }
            if (remaining >= 8) {
                append(N8); remaining -= 8
            }
            if (remaining >= 4) {
                append(N4); remaining -= 4
            }
            if (remaining >= 2) {
                append(N2); remaining -= 2
            }
            if (remaining >= 1) {
                append(N1); remaining -= 1
            }
        }
    }
}
