package com.github.kangwooc.dbmlplugin.lang.highlighting

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTokenTypes
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DBMLLexerTest {

    @Test
    fun `lexes table declaration`() {
        val text = """
            table users {
              id int [pk]
              created_at datetime [default: now()]
              note: 'example'
            }
        """.trimIndent()

        val tokens = lex(text).filter { it.type != TokenType.WHITE_SPACE }

        val expectedSequence = listOf(
            Token(DBMLTokenTypes.KEYWORD, "table"),
            Token(DBMLTokenTypes.IDENTIFIER, "users"),
            Token(DBMLTokenTypes.BRACE, "{"),
            Token(DBMLTokenTypes.IDENTIFIER, "id"),
            Token(DBMLTokenTypes.IDENTIFIER, "int"),
            Token(DBMLTokenTypes.BRACKET, "["),
            Token(DBMLTokenTypes.KEYWORD, "pk"),
            Token(DBMLTokenTypes.BRACKET, "]"),
            Token(DBMLTokenTypes.IDENTIFIER, "created_at"),
            Token(DBMLTokenTypes.IDENTIFIER, "datetime"),
            Token(DBMLTokenTypes.BRACKET, "["),
            Token(DBMLTokenTypes.KEYWORD, "default"),
            Token(DBMLTokenTypes.OPERATOR, ":"),
            Token(DBMLTokenTypes.KEYWORD, "now"),
            Token(DBMLTokenTypes.PAREN, "("),
            Token(DBMLTokenTypes.PAREN, ")"),
            Token(DBMLTokenTypes.BRACKET, "]"),
            Token(DBMLTokenTypes.KEYWORD, "note"),
            Token(DBMLTokenTypes.OPERATOR, ":"),
            Token(DBMLTokenTypes.STRING, "'example'"),
            Token(DBMLTokenTypes.BRACE, "}")
        )

        assertEquals("Unexpected token count", expectedSequence.size, tokens.size)
        expectedSequence.zip(tokens).forEachIndexed { index, (expected, actual) ->
            assertEquals("Token $index type mismatch", expected.type, actual.type)
            assertEquals("Token $index text mismatch", expected.text, actual.text)
        }
    }

    @Test
    fun `lexes comments and numbers`() {
        val text = "// comment\nref: users.id > posts.user_id /* block */ 42"

        val tokens = lex(text).filter { it.type != TokenType.WHITE_SPACE }

        assertTrue(tokens.first().type === DBMLTokenTypes.COMMENT)
        assertEquals("// comment", tokens.first().text)
        assertTrue(tokens.any { it.type === DBMLTokenTypes.NUMBER && it.text == "42" })
    }

    @Test
    fun `lexes block comment and whitespace`() {
        val text = "table x {\n  /* block */ name varchar\n}"

        val tokens = lex(text)

        assertTrue(tokens.any { it.type === DBMLTokenTypes.COMMENT && it.text == "/* block */" })
        assertTrue(tokens.any { it.type === TokenType.WHITE_SPACE && it.text.contains("\n") })
    }

    @Test
    fun `lexes escaped string and operators`() {
        val text = "note: 'don\\'t' ref: users.id > posts.user_id ?"

        val tokens = lex(text).filter { it.type != TokenType.WHITE_SPACE }

        val stringToken = tokens.firstOrNull { it.type === DBMLTokenTypes.STRING }
        assertNotNull("Expected escaped string token", stringToken)
        assertEquals("'don\\'t'", stringToken!!.text)
        assertTrue(tokens.any { it.type === DBMLTokenTypes.OPERATOR && it.text == ":" })
        assertTrue(tokens.any { it.type === DBMLTokenTypes.OPERATOR && it.text == ">" })
        assertTrue(tokens.any { it.type === TokenType.BAD_CHARACTER && it.text == "?" })
    }

    @Test
    fun `lexes project and schema table`() {
        val text = """
            Project MyProject {
              database_type: 'PostgreSQL'
              Note: '설명'
            }

            Table core.user {
              id int
            }
        """.trimIndent()

        val tokens = lex(text).filter { it.type != TokenType.WHITE_SPACE }

        val projectToken = tokens.firstOrNull { it.text.equals("project", ignoreCase = true) }
        assertNotNull("Project keyword not found", projectToken)
        assertEquals(DBMLTokenTypes.KEYWORD, projectToken!!.type)

        val databaseTypeToken = tokens.firstOrNull { it.text == "database_type" }
        assertNotNull("database_type keyword missing", databaseTypeToken)
        assertEquals(DBMLTokenTypes.KEYWORD, databaseTypeToken!!.type)

        val schemaIndex = tokens.indexOfFirst { it.text == "core" }
        assertTrue(schemaIndex >= 0)
        assertEquals(DBMLTokenTypes.DOT, tokens[schemaIndex + 1].type)
        assertEquals(DBMLTokenTypes.IDENTIFIER, tokens[schemaIndex + 2].type)
    }

    @Test
    fun `lexes triple quoted note`() {
        val text = """
            Note {
              '''
              multi line
              note
              '''
            }
        """.trimIndent()

        val tokens = lex(text)
        val stringToken = tokens.firstOrNull { it.type === DBMLTokenTypes.STRING && it.text.startsWith("'''") }
        assertNotNull("Triple quoted string not parsed", stringToken)
        assertTrue(stringToken!!.text.contains("multi line"))
    }

    @Test
    fun `lexes backtick expression`() {
        val text = "default: `now()`"

        val tokens = lex(text).filter { it.type != TokenType.WHITE_SPACE }
        val defaultToken = tokens.firstOrNull { it.text == "default" }
        assertNotNull(defaultToken)
        assertEquals(DBMLTokenTypes.KEYWORD, defaultToken!!.type)

        val stringToken = tokens.firstOrNull { it.type === DBMLTokenTypes.STRING }
        assertNotNull("Backtick expression not treated as string", stringToken)
        assertEquals("`now()`", stringToken!!.text)
    }

    @Test
    fun `lexes table partial injection`() {
        val text = """
            Table users {
              ~base_template
              email varchar [unique]
            }
        """.trimIndent()

        val tokens = lex(text).filter { it.type != TokenType.WHITE_SPACE }
        assertTrue(tokens.any { it.type === DBMLTokenTypes.OPERATOR && it.text == "~" })
        val uniqueToken = tokens.firstOrNull { it.text == "unique" }
        assertNotNull(uniqueToken)
        assertEquals(DBMLTokenTypes.KEYWORD, uniqueToken!!.type)
    }

    @Test
    fun `lexes tablegroup keyword`() {
        val text = "TableGroup management { users }"
        
        val tokens = lex(text).filter { it.type != TokenType.WHITE_SPACE }
        
        val tablegroupToken = tokens.firstOrNull { it.text.equals("TableGroup", ignoreCase = true) }
        assertNotNull("TableGroup keyword not found", tablegroupToken)
        assertEquals(DBMLTokenTypes.KEYWORD, tablegroupToken!!.type)
    }

    private fun lex(text: String): List<Token> {
        val lexer = DBMLLexer()
        lexer.start(text, 0, text.length, 0)

        val tokens = mutableListOf<Token>()
        while (lexer.tokenType != null) {
            tokens += Token(lexer.tokenType!!, text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return tokens
    }

    private data class Token(val type: IElementType, val text: String)
}
