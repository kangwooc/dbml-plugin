package com.github.kangwooc.dbmlplugin.lang.highlighting

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTokenTypes
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.TokenType
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DBMLSyntaxHighlighterTest {

    private val highlighter = DBMLSyntaxHighlighter()

    @Test
    fun `returns keyword attributes`() {
        val keys = highlighter.getTokenHighlights(DBMLTokenTypes.KEYWORD)
        assertSingleKey("DBML_KEYWORD", keys)
    }

    @Test
    fun `returns comment attributes`() {
        val keys = highlighter.getTokenHighlights(DBMLTokenTypes.COMMENT)
        assertSingleKey("DBML_COMMENT", keys)
    }

    @Test
    fun `returns number attributes`() {
        val keys = highlighter.getTokenHighlights(DBMLTokenTypes.NUMBER)
        assertSingleKey("DBML_NUMBER", keys)
    }

    @Test
    fun `returns string attributes`() {
        val keys = highlighter.getTokenHighlights(DBMLTokenTypes.STRING)
        assertSingleKey("DBML_STRING", keys)
    }

    @Test
    fun `returns brace attributes`() {
        val keys = highlighter.getTokenHighlights(DBMLTokenTypes.BRACE)
        assertSingleKey("DBML_BRACE", keys)
    }

    @Test
    fun `returns operator attributes`() {
        val keys = highlighter.getTokenHighlights(DBMLTokenTypes.OPERATOR)
        assertSingleKey("DBML_OPERATOR", keys)
    }

    @Test
    fun `returns bad character attributes`() {
        val keys = highlighter.getTokenHighlights(TokenType.BAD_CHARACTER)
        assertSingleKey("DBML_BAD_CHAR", keys)
    }

    @Test
    fun `returns empty for identifiers`() {
        val keys = highlighter.getTokenHighlights(DBMLTokenTypes.IDENTIFIER)
        assertArrayEquals(emptyArray<TextAttributesKey>(), keys)
    }

    private fun assertSingleKey(expectedExternalName: String, keys: Array<TextAttributesKey>) {
        assertEquals(1, keys.size)
        assertEquals(expectedExternalName, keys[0].externalName)
    }
}
