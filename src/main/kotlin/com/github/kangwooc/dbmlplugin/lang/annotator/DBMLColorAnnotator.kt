package com.github.kangwooc.dbmlplugin.lang.annotator

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTokenTypes
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.ui.JBColor
import java.awt.Color
import javax.swing.Icon

class DBMLColorAnnotator : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is LeafPsiElement) return null
        if (element.text != "#") return null
        if (element.elementType != DBMLTokenTypes.OPERATOR) return null
        
        if (!isColorContext(element)) return null
        
        val colorCode = buildColorCodeFromTokens(element) ?: return null
        val color = parseColor(colorCode) ?: return null
        
        return LineMarkerInfo(
            element,
            element.textRange,
            ColorIcon(color),
            { "Color: $colorCode" },
            null,
            GutterIconRenderer.Alignment.LEFT,
            { "Color: $colorCode" }
        )
    }

    private fun isColorContext(element: PsiElement): Boolean {
        val parent = element.parent ?: return false
        val parentText = parent.text.lowercase()
        return parentText.contains("headercolor:") || parentText.contains("color:")
    }

    private fun buildColorCodeFromTokens(hashElement: LeafPsiElement): String? {
        var current: PsiElement? = hashElement.nextSibling
        val hexChars = StringBuilder("#")
        
        while (current != null && hexChars.length <= 7) {
            if (current is LeafPsiElement) {
                val text = current.text
                if (text.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
                    hexChars.append(text)
                } else {
                    break
                }
            }
            current = current.nextSibling
        }
        
        val code = hexChars.toString()
        val hexOnly = code.substring(1)
        return if (hexOnly.length == 3 || hexOnly.length == 6) code else null
    }

    private fun parseColor(colorCode: String): Color? {
        return try {
            if (!colorCode.startsWith("#")) return null
            
            val hex = colorCode.substring(1)
            when (hex.length) {
                3 -> {
                    val r = hex[0].toString().repeat(2).toInt(16)
                    val g = hex[1].toString().repeat(2).toInt(16)
                    val b = hex[2].toString().repeat(2).toInt(16)
                    Color(r, g, b)
                }
                6 -> Color.decode(colorCode)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private class ColorIcon(private val color: Color) : Icon {
        override fun paintIcon(c: java.awt.Component?, g: java.awt.Graphics?, x: Int, y: Int) {
            g?.color = color
            g?.fillRect(x, y, 12, 12)
            g?.color = JBColor.GRAY
            g?.drawRect(x, y, 12, 12)
        }

        override fun getIconWidth() = 12
        override fun getIconHeight() = 12
    }
}
