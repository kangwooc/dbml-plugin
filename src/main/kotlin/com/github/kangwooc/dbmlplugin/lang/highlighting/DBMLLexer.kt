package com.github.kangwooc.dbmlplugin.lang.highlighting

import com.github.kangwooc.dbmlplugin.lang.DBMLKeywords
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTokenTypes
import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class DBMLLexer : LexerBase() {

    private var buffer: CharSequence = ""
    private var endOffset: Int = 0
    private var position: Int = 0
    private var tokenStart: Int = 0
    private var tokenEnd: Int = 0
    private var tokenType: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.position = startOffset
        this.endOffset = endOffset
        tokenStart = startOffset
        tokenEnd = startOffset
        tokenType = null
        advance()
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = tokenType

    override fun getTokenStart(): Int = tokenStart

    override fun getTokenEnd(): Int = tokenEnd

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = endOffset

    override fun advance() {
        if (position >= endOffset) {
            tokenType = null
            return
        }

        tokenStart = position
        val currentChar = buffer[position]

        when {
            currentChar.isWhitespace() -> lexWhitespace()
            currentChar == '/' && matchNext('/') -> lexLineComment()
            currentChar == '/' && matchNext('*') -> lexBlockComment()
            currentChar == '\'' -> lexSingleQuotedString()
            currentChar == '"' -> lexDoubleQuotedString()
            currentChar == '`' -> lexBacktickString()
            currentChar.isDigit() -> lexNumber()
            isIdentifierStart(currentChar) -> lexIdentifier()
            else -> lexSymbol()
        }
    }

    private fun lexWhitespace() {
        while (position < endOffset && buffer[position].isWhitespace()) {
            position++
        }
        tokenEnd = position
        tokenType = TokenType.WHITE_SPACE
    }

    private fun lexLineComment() {
        position += 2
        while (position < endOffset && buffer[position] != '\n' && buffer[position] != '\r') {
            position++
        }
        tokenEnd = position
        tokenType = DBMLTokenTypes.COMMENT
    }

    private fun lexBlockComment() {
        position += 2
        while (position < endOffset - 1) {
            if (buffer[position] == '*' && buffer[position + 1] == '/') {
                position += 2
                break
            }
            position++
        }
        if (position >= endOffset) {
            position = endOffset
        }
        tokenEnd = position
        tokenType = DBMLTokenTypes.COMMENT
    }

    private fun lexSingleQuotedString() {
        val quote = buffer[position]
        val isTriple = position + 2 < endOffset && buffer[position + 1] == quote && buffer[position + 2] == quote
        position += if (isTriple) 3 else 1
        var escaped = false
        while (position < endOffset) {
            if (isTriple && position + 2 < endOffset && buffer[position] == quote && buffer[position + 1] == quote && buffer[position + 2] == quote) {
                position += 3
                break
            }
            val ch = buffer[position]
            if (!isTriple) {
                if (escaped) {
                    escaped = false
                } else if (ch == '\\') {
                    escaped = true
                } else if (ch == quote) {
                    position++
                    break
                }
            }
            position++
        }
        tokenEnd = minOf(position, endOffset)
        tokenType = DBMLTokenTypes.STRING
    }

    private fun lexDoubleQuotedString() {
        val quote = buffer[position]
        position++
        var escaped = false
        while (position < endOffset) {
            val ch = buffer[position]
            if (escaped) {
                escaped = false
            } else if (ch == '\\') {
                escaped = true
            } else if (ch == quote) {
                position++
                break
            }
            position++
        }
        tokenEnd = position
        tokenType = DBMLTokenTypes.STRING
    }

    private fun lexBacktickString() {
        position++
        while (position < endOffset) {
            val ch = buffer[position]
            if (ch == '`') {
                position++
                break
            }
            position++
        }
        tokenEnd = position
        tokenType = DBMLTokenTypes.STRING
    }

    private fun lexNumber() {
        while (position < endOffset) {
            val ch = buffer[position]
            if (ch.isDigit() || ch == '_' || ch == '.') {
                position++
            } else {
                break
            }
        }
        tokenEnd = position
        tokenType = DBMLTokenTypes.NUMBER
    }

    private fun lexIdentifier() {
        position++
        while (position < endOffset && isIdentifierPart(buffer[position])) {
            position++
        }
        tokenEnd = position
        val text = buffer.subSequence(tokenStart, tokenEnd).toString().lowercase()
        tokenType = if (text in DBMLKeywords.ALL) DBMLTokenTypes.KEYWORD else DBMLTokenTypes.IDENTIFIER
    }

    private fun lexSymbol() {
        val ch = buffer[position]
        position++
        tokenEnd = position
        tokenType = when (ch) {
            '{', '}' -> DBMLTokenTypes.BRACE
            '(', ')' -> DBMLTokenTypes.PAREN
            '[', ']' -> DBMLTokenTypes.BRACKET
            ',' -> DBMLTokenTypes.COMMA
            ';' -> DBMLTokenTypes.SEMICOLON
            '.' -> DBMLTokenTypes.DOT
            '=', '+', '-', '*', ':', '@', '#', '<', '>', '~' -> DBMLTokenTypes.OPERATOR
            else -> TokenType.BAD_CHARACTER
        }
    }

    private fun matchNext(expected: Char): Boolean =
        position + 1 < endOffset && buffer[position + 1] == expected

    private fun isIdentifierStart(ch: Char): Boolean = ch == '_' || ch == '$' || ch.isLetter()

    private fun isIdentifierPart(ch: Char): Boolean = ch == '_' || ch == '$' || ch.isLetterOrDigit()
}
