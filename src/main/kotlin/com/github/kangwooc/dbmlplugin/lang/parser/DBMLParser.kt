package com.github.kangwooc.dbmlplugin.lang.parser

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLElementTypes
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTokenTypes
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class DBMLParser : PsiParser {

    override fun parse(root: IElementType, builder: PsiBuilder) = builder.build(root) {
        while (!builder.eof()) {
            parseStatement(builder)
        }
    }

    private fun parseStatement(builder: PsiBuilder) {
        skipTrivia(builder)
        if (builder.eof()) return

        val tokenType = builder.tokenType
        val tokenText = builder.tokenText?.lowercase()

        if (tokenType == DBMLTokenTypes.KEYWORD && tokenText != null) {
            when (tokenText) {
                "table", "tablepartial" -> parseTableDecl(builder)
                "enum" -> parseBlockLikeStatement(builder, DBMLElementTypes.ENUM_DECL)
                "ref" -> parseBlockOrInlineStatement(builder, DBMLElementTypes.REF_DECL)
                "project" -> parseBlockLikeStatement(builder, DBMLElementTypes.PROJECT_DECL)
                "tablegroup" -> parseBlockLikeStatement(builder, DBMLElementTypes.TABLEGROUP_DECL)
                "note", "notes" -> parseBlockOrInlineStatement(builder, DBMLElementTypes.NOTE_DECL)
                else -> parseUnknownStatement(builder)
            }
            return
        }

        parseUnknownStatement(builder)
    }

    private fun parseTableDecl(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // keyword

        while (!builder.eof()) {
            if (builder.tokenType == DBMLTokenTypes.BRACE && builder.tokenText == "{") {
                parseTableBody(builder)
                consumeTrailingTrivia(builder)
                marker.done(DBMLElementTypes.TABLE_DECL)
                return
            }
            if (isHardTerminator(builder)) {
                break
            }
            builder.advanceLexer()
        }

        consumeStatementTail(builder)
        marker.done(DBMLElementTypes.TABLE_DECL)
    }

    private fun parseTableBody(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // {

        while (!builder.eof()) {
            skipTrivia(builder)
            if (builder.eof()) break
            if (builder.tokenType == DBMLTokenTypes.BRACE && builder.tokenText == "}") {
                builder.advanceLexer()
                break
            }
            parseTableItem(builder)
        }

        marker.done(DBMLElementTypes.TABLE_BODY)
    }

    private fun parseTableItem(builder: PsiBuilder) {
        val keyword = currentKeyword(builder)
        when {
            keyword == "indexes" -> parseIndexesBlock(builder)
            keyword == "note" || keyword == "notes" -> parseNoteBlock(builder)
            keyword == "primary" && peekNextKeyword(builder) == "key" -> parsePrimaryKeyBlock(builder)
            keyword == "index" -> parseIndexDef(builder)
            else -> parseColumnDef(builder)
        }
    }

    private fun parseColumnDef(builder: PsiBuilder) {
        val marker = builder.mark()
        parseColumnName(builder)
        parseColumnType(builder)
        parseColumnAttributes(builder)
        consumeStatementTail(builder)
        marker.done(DBMLElementTypes.COLUMN_DEF)
    }

    private fun parseIndexesBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // indexes

        if (builder.tokenType == DBMLTokenTypes.BRACE && builder.tokenText == "{") {
            parseIndexesBody(builder)
            marker.done(DBMLElementTypes.INDEXES_BLOCK)
            return
        }

        consumeStatementTail(builder)
        marker.done(DBMLElementTypes.INDEXES_BLOCK)
    }

    private fun parseIndexesBody(builder: PsiBuilder) {
        builder.advanceLexer() // {
        while (!builder.eof()) {
            skipTrivia(builder)
            if (builder.eof()) return
            if (builder.tokenType == DBMLTokenTypes.BRACE && builder.tokenText == "}") {
                builder.advanceLexer()
                return
            }
            parseIndexDef(builder)
        }
    }

    private fun parseIndexDef(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer()
        consumeStatementTail(builder)
        marker.done(DBMLElementTypes.INDEX_DEF)
    }

    private fun parsePrimaryKeyBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // primary
        if (currentKeyword(builder) == "key") {
            builder.advanceLexer()
        }

        if (builder.tokenType == DBMLTokenTypes.BRACE && builder.tokenText == "{") {
            tryConsumeBlock(builder)
            marker.done(DBMLElementTypes.PRIMARY_KEY_BLOCK)
            return
        }

        consumeStatementTail(builder)
        marker.done(DBMLElementTypes.PRIMARY_KEY_BLOCK)
    }

    private fun parseNoteBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // note/notes

        if (tryConsumeBlock(builder)) {
            marker.done(DBMLElementTypes.NOTE_BLOCK)
            return
        }

        consumeInlineStatement(builder)
        marker.done(DBMLElementTypes.NOTE_BLOCK)
    }

    private fun parseBlockLikeStatement(builder: PsiBuilder, elementType: IElementType) {
        val marker = builder.mark()
        builder.advanceLexer() // keyword

        var parsedBlock = false
        while (!builder.eof()) {
            if (tryConsumeBlock(builder)) {
                parsedBlock = true
                break
            }
            if (isHardTerminator(builder)) {
                break
            }
            builder.advanceLexer()
        }

        if (!parsedBlock) {
            consumeStatementTail(builder)
        } else {
            consumeTrailingTrivia(builder)
        }

        marker.done(elementType)
    }

    private fun parseBlockOrInlineStatement(builder: PsiBuilder, elementType: IElementType) {
        val marker = builder.mark()
        builder.advanceLexer() // keyword

        if (tryConsumeBlock(builder)) {
            consumeTrailingTrivia(builder)
            marker.done(elementType)
            return
        }

        consumeInlineStatement(builder)
        marker.done(elementType)
    }

    private fun parseUnknownStatement(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer()
        consumeStatementTail(builder)
        marker.done(DBMLElementTypes.UNKNOWN_STMT)
    }

    private fun consumeInlineStatement(builder: PsiBuilder) {
        while (!builder.eof()) {
            if (tryConsumeBlock(builder)) {
                return
            }
            if (isStatementTerminator(builder)) {
                consumeStatementTail(builder)
                return
            }
            builder.advanceLexer()
        }
    }

    private fun consumeStatementTail(builder: PsiBuilder) {
        while (!builder.eof()) {
            val tokenType = builder.tokenType
            if (tokenType == DBMLTokenTypes.SEMICOLON) {
                builder.advanceLexer()
                return
            }
            if (tokenType == TokenType.WHITE_SPACE && builder.tokenText?.contains('\n') == true) {
                builder.advanceLexer()
                return
            }
            if (tokenType == DBMLTokenTypes.BRACE && builder.tokenText == "}") {
                return
            }
            builder.advanceLexer()
        }
    }

    private fun tryConsumeBlock(builder: PsiBuilder): Boolean {
        if (builder.tokenType != DBMLTokenTypes.BRACE || builder.tokenText != "{") {
            return false
        }
        builder.advanceLexer()
        var depth = 1
        while (!builder.eof() && depth > 0) {
            if (builder.tokenType == DBMLTokenTypes.BRACE) {
                val brace = builder.tokenText
                if (brace == "{") depth++
                if (brace == "}") depth--
            }
            builder.advanceLexer()
        }
        return true
    }

    private fun isStatementTerminator(builder: PsiBuilder): Boolean {
        val tokenType = builder.tokenType
        if (tokenType == DBMLTokenTypes.SEMICOLON) return true
        if (tokenType == TokenType.WHITE_SPACE && builder.tokenText?.contains('\n') == true) return true
        return tokenType == DBMLTokenTypes.BRACE && builder.tokenText == "}"
    }

    private fun isHardTerminator(builder: PsiBuilder): Boolean {
        val tokenType = builder.tokenType
        if (tokenType == DBMLTokenTypes.SEMICOLON) return true
        return tokenType == DBMLTokenTypes.BRACE && builder.tokenText == "}"
    }

    private fun skipTrivia(builder: PsiBuilder) {
        while (!builder.eof()) {
            val tokenType = builder.tokenType
            if (tokenType != TokenType.WHITE_SPACE && tokenType != DBMLTokenTypes.COMMENT) return
            builder.advanceLexer()
        }
    }

    private fun consumeTrailingTrivia(builder: PsiBuilder) {
        while (!builder.eof()) {
            val tokenType = builder.tokenType
            if (tokenType != TokenType.WHITE_SPACE && tokenType != DBMLTokenTypes.COMMENT) return
            builder.advanceLexer()
        }
    }

    private fun parseColumnName(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer()
        marker.done(DBMLElementTypes.COLUMN_NAME)
    }

    private fun parseColumnType(builder: PsiBuilder) {
        skipTrivia(builder)
        val marker = builder.mark()
        var consumed = false

        while (!builder.eof()) {
            if (builder.tokenType == DBMLTokenTypes.BRACKET && builder.tokenText == "[") break
            if (isStatementTerminator(builder)) break
            if (builder.tokenType == DBMLTokenTypes.BRACE && builder.tokenText == "{") break
            builder.advanceLexer()
            consumed = true
        }

        if (consumed) {
            marker.done(DBMLElementTypes.COLUMN_TYPE)
        } else {
            marker.drop()
        }
    }

    private fun parseColumnAttributes(builder: PsiBuilder) {
        skipTrivia(builder)
        if (builder.tokenType != DBMLTokenTypes.BRACKET || builder.tokenText != "[") return
        val marker = builder.mark()
        consumeBracketList(builder)
        marker.done(DBMLElementTypes.COLUMN_ATTR_LIST)
    }

    private fun currentKeyword(builder: PsiBuilder): String? {
        if (builder.tokenType != DBMLTokenTypes.KEYWORD) return null
        return builder.tokenText?.lowercase()
    }

    private fun peekNextKeyword(builder: PsiBuilder): String? {
        val marker = builder.mark()
        builder.advanceLexer()
        val keyword = currentKeyword(builder)
        marker.rollbackTo()
        return keyword
    }

    private fun consumeBracketList(builder: PsiBuilder) {
        if (builder.tokenType != DBMLTokenTypes.BRACKET || builder.tokenText != "[") return
        builder.advanceLexer()
        var depth = 1
        while (!builder.eof() && depth > 0) {
            if (builder.tokenType == DBMLTokenTypes.BRACKET) {
                val bracket = builder.tokenText
                if (bracket == "[") depth++
                if (bracket == "]") depth--
            }
            builder.advanceLexer()
        }
    }
}

private inline fun PsiBuilder.build(root: IElementType, action: () -> Unit) =
    mark().also { marker ->
        action()
        marker.done(root)
    }.let { treeBuilt }
