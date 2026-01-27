package com.github.kangwooc.dbmlplugin.lang.highlighting

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTokenTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class DBMLSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = DBMLLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
        DBMLTokenTypes.KEYWORD -> KEYWORD_KEYS
        DBMLTokenTypes.STRING -> STRING_KEYS
        DBMLTokenTypes.NUMBER -> NUMBER_KEYS
        DBMLTokenTypes.COMMENT -> COMMENT_KEYS
        DBMLTokenTypes.BRACE -> BRACE_KEYS
        DBMLTokenTypes.BRACKET -> BRACKET_KEYS
        DBMLTokenTypes.PAREN -> PAREN_KEYS
        DBMLTokenTypes.COMMA -> COMMA_KEYS
        DBMLTokenTypes.DOT -> DOT_KEYS
        DBMLTokenTypes.SEMICOLON -> SEMICOLON_KEYS
        DBMLTokenTypes.OPERATOR -> OPERATOR_KEYS
        TokenType.BAD_CHARACTER -> BAD_CHAR_KEYS
        else -> emptyArray()
    }

    companion object {
        private val KEYWORD = TextAttributesKey.createTextAttributesKey(
            "DBML_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )
        private val STRING = TextAttributesKey.createTextAttributesKey(
            "DBML_STRING",
            DefaultLanguageHighlighterColors.STRING
        )
        private val NUMBER = TextAttributesKey.createTextAttributesKey(
            "DBML_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
        )
        private val COMMENT = TextAttributesKey.createTextAttributesKey(
            "DBML_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )
        private val BRACE = TextAttributesKey.createTextAttributesKey(
            "DBML_BRACE",
            DefaultLanguageHighlighterColors.BRACES
        )
        private val BRACKET = TextAttributesKey.createTextAttributesKey(
            "DBML_BRACKET",
            DefaultLanguageHighlighterColors.BRACKETS
        )
        private val PAREN = TextAttributesKey.createTextAttributesKey(
            "DBML_PAREN",
            DefaultLanguageHighlighterColors.PARENTHESES
        )
        private val COMMA = TextAttributesKey.createTextAttributesKey(
            "DBML_COMMA",
            DefaultLanguageHighlighterColors.COMMA
        )
        private val DOT = TextAttributesKey.createTextAttributesKey(
            "DBML_DOT",
            DefaultLanguageHighlighterColors.DOT
        )
        private val SEMICOLON = TextAttributesKey.createTextAttributesKey(
            "DBML_SEMICOLON",
            DefaultLanguageHighlighterColors.SEMICOLON
        )
        private val OPERATOR = TextAttributesKey.createTextAttributesKey(
            "DBML_OPERATOR",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )
        private val BAD_CHAR = TextAttributesKey.createTextAttributesKey(
            "DBML_BAD_CHAR",
            DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE
        )

        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val STRING_KEYS = arrayOf(STRING)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val BRACE_KEYS = arrayOf(BRACE)
        private val BRACKET_KEYS = arrayOf(BRACKET)
        private val PAREN_KEYS = arrayOf(PAREN)
        private val COMMA_KEYS = arrayOf(COMMA)
        private val DOT_KEYS = arrayOf(DOT)
        private val SEMICOLON_KEYS = arrayOf(SEMICOLON)
        private val OPERATOR_KEYS = arrayOf(OPERATOR)
        private val BAD_CHAR_KEYS = arrayOf(BAD_CHAR)
    }
}
