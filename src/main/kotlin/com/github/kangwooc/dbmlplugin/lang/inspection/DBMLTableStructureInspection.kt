package com.github.kangwooc.dbmlplugin.lang.inspection

import com.github.kangwooc.dbmlplugin.lang.highlighting.DBMLLexer
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLFile
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTokenTypes
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import java.util.ArrayDeque

class DBMLTableStructureInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is DBMLFile) return
                analyzeFile(file, holder)
            }
        }
    }

    private fun analyzeFile(file: PsiFile, holder: ProblemsHolder) {
        val text = file.viewProvider.document?.charsSequence ?: file.text
        if (text.isEmpty()) return

        val lexer = DBMLLexer()
        lexer.start(text, 0, text.length, 0)

        val contextStack = ArrayDeque<Context>()
        val tableStack = ArrayDeque<TableState>()
        var pendingScope: PendingScope? = null
        var anyBracketDepth = 0

        while (true) {
            val tokenType = lexer.tokenType ?: break
            val tokenStart = lexer.tokenStart
            val tokenEnd = lexer.tokenEnd

            when {
                tokenType == DBMLTokenTypes.KEYWORD -> {
                    val keyword = text.subSequence(tokenStart, tokenEnd).toString().lowercase()

                    if (pendingScope?.scopeType == ScopeType.PRIMARY_KEY_PENDING && keyword != KEYWORD_KEY) {
                        pendingScope = null
                    }

                    if (anyBracketDepth == 0) {
                        when (keyword) {
                            KEYWORD_TABLE -> pendingScope = PendingScope(ScopeType.TABLE, tokenStart)

                            KEYWORD_INDEXES -> if (tableStack.isNotEmpty()) {
                                pendingScope = PendingScope(ScopeType.TABLE_INDEXES, tokenStart)
                            }

                            KEYWORD_PRIMARY -> if (tableStack.isNotEmpty()) {
                                pendingScope = PendingScope(ScopeType.PRIMARY_KEY_PENDING, tokenStart)
                            }

                            KEYWORD_KEY -> if (pendingScope?.scopeType == ScopeType.PRIMARY_KEY_PENDING) {
                                pendingScope = PendingScope(ScopeType.TABLE_PRIMARY_KEY, pendingScope.highlightOffset)
                            }

                            KEYWORD_NOTE -> if (tableStack.isNotEmpty()) {
                                pendingScope = PendingScope(ScopeType.NOTE_BLOCK, tokenStart)
                            }

                            KEYWORD_INDEX -> if (tableStack.isNotEmpty() && contextStack.none { it.scope == ScopeType.TABLE_INDEXES }) {
                                registerProblem(
                                    holder,
                                    file,
                                    tokenStart,
                                    tokenEnd,
                                    "Wrap index declarations inside an indexes { } block"
                                )
                            }
                        }
                    }
                }

                tokenType == DBMLTokenTypes.BRACE -> {
                    val braceChar = text[tokenStart]
                    if (braceChar == '{') {
                        val pending = pendingScope
                        val scope = pending?.scopeType ?: ScopeType.OTHER
                        val activeTable = tableStack.lastOrNull()

                        when (scope) {
                            ScopeType.TABLE -> {
                                val tableState = TableState()
                                tableStack.addLast(tableState)
                                contextStack.addLast(Context(scope, tableState))
                            }

                            ScopeType.TABLE_INDEXES -> {
                                contextStack.addLast(Context(scope, activeTable))
                                if (pending != null && activeTable != null) {
                                    activeTable.ensureSectionOrder(
                                        SectionType.INDEXES,
                                        holder,
                                        file,
                                        pending.highlightOffset,
                                        SectionType.INDEXES.highlightEnd(pending.highlightOffset, file.textLength)
                                    )
                                    activeTable.indexesDepth++
                                }
                            }

                            ScopeType.TABLE_PRIMARY_KEY -> {
                                contextStack.addLast(Context(scope, activeTable))
                                if (pending != null && activeTable != null) {
                                    activeTable.ensureSectionOrder(
                                        SectionType.PRIMARY_KEY,
                                        holder,
                                        file,
                                        pending.highlightOffset,
                                        SectionType.PRIMARY_KEY.highlightEnd(pending.highlightOffset, file.textLength)
                                    )
                                    activeTable.primaryKeyDepth++
                                }
                            }

                            ScopeType.NOTE_BLOCK -> {
                                contextStack.addLast(Context(scope, activeTable))
                                if (pending != null && activeTable != null) {
                                    activeTable.ensureSectionOrder(
                                        SectionType.NOTE,
                                        holder,
                                        file,
                                        pending.highlightOffset,
                                        SectionType.NOTE.highlightEnd(pending.highlightOffset, file.textLength)
                                    )
                                    activeTable.noteDepth++
                                }
                            }

                            ScopeType.PRIMARY_KEY_PENDING -> contextStack.addLast(Context(ScopeType.OTHER, activeTable))

                            ScopeType.OTHER -> contextStack.addLast(Context(scope, activeTable))
                        }

                        pendingScope = null
                    } else {
                        val context = contextStack.pollLast() ?: Context(ScopeType.OTHER, null)
                        when (context.scope) {
                            ScopeType.TABLE -> tableStack.pollLast()
                            ScopeType.TABLE_INDEXES -> context.tableState?.decrementIndexes()
                            ScopeType.TABLE_PRIMARY_KEY -> context.tableState?.decrementPrimaryKey()
                            ScopeType.NOTE_BLOCK -> context.tableState?.decrementNote()
                            else -> Unit
                        }
                        pendingScope = null
                    }
                }

                tokenType == DBMLTokenTypes.BRACKET -> {
                    val bracketChar = text[tokenStart]
                    if (bracketChar == '[') {
                        anyBracketDepth++
                    } else if (bracketChar == ']' && anyBracketDepth > 0) {
                        anyBracketDepth--
                    }
                }

                tokenType == TokenType.WHITE_SPACE || tokenType == DBMLTokenTypes.COMMENT -> Unit

                else -> Unit
            }

            if (tokenType != DBMLTokenTypes.KEYWORD &&
                tokenType != TokenType.WHITE_SPACE &&
                tokenType != DBMLTokenTypes.COMMENT &&
                tokenType != DBMLTokenTypes.BRACE
            ) {
                pendingScope = pendingScope?.takeIf {
                    isTokenAllowedBetweenKeywordAndBrace(tokenType)
                }
            }

            lexer.advance()
        }
    }

    private fun registerProblem(
        holder: ProblemsHolder,
        file: PsiFile,
        startOffset: Int,
        endOffset: Int,
        message: String
    ) {
        val element = file.findElementAt(startOffset)
        if (element != null) {
            holder.registerProblem(element, message)
        } else if (endOffset > startOffset) {
            val range = TextRange(startOffset, endOffset.coerceAtMost(file.textLength))
            holder.registerProblem(file, range, message)
        }
    }

    private fun isTokenAllowedBetweenKeywordAndBrace(tokenType: com.intellij.psi.tree.IElementType): Boolean {
        return tokenType == TokenType.WHITE_SPACE ||
            tokenType == DBMLTokenTypes.IDENTIFIER ||
            tokenType == DBMLTokenTypes.STRING ||
            tokenType == DBMLTokenTypes.DOT ||
            tokenType == DBMLTokenTypes.COMMA ||
            tokenType == DBMLTokenTypes.OPERATOR ||
            tokenType == DBMLTokenTypes.BRACKET ||
            tokenType == DBMLTokenTypes.NUMBER ||
            tokenType == DBMLTokenTypes.COMMENT
    }

    private data class PendingScope(val scopeType: ScopeType, val highlightOffset: Int)

    private data class Context(val scope: ScopeType, val tableState: TableState?)

    private enum class ScopeType {
        TABLE,
        TABLE_INDEXES,
        PRIMARY_KEY_PENDING,
        TABLE_PRIMARY_KEY,
        NOTE_BLOCK,
        OTHER
    }

    private class TableState {
        private var lastSection: SectionType = SectionType.COLUMNS
        var indexesDepth: Int = 0
        var primaryKeyDepth: Int = 0
        var noteDepth: Int = 0

        fun ensureSectionOrder(
            section: SectionType,
            holder: ProblemsHolder,
            file: PsiFile,
            startOffset: Int,
            endOffset: Int
        ) {
            if (section.order < lastSection.order) {
                val element = file.findElementAt(startOffset)
                if (element != null) {
                    holder.registerProblem(
                        element,
                        "'${section.displayName}' section must appear before '${lastSection.displayName}' section."
                    )
                } else {
                    val range = TextRange(startOffset, endOffset.coerceAtMost(file.textLength))
                    holder.registerProblem(
                        file,
                        range,
                        "'${section.displayName}' section must appear before '${lastSection.displayName}' section."
                    )
                }
            } else {
                lastSection = section
            }
        }

        fun decrementIndexes() {
            if (indexesDepth > 0) indexesDepth--
        }

        fun decrementPrimaryKey() {
            if (primaryKeyDepth > 0) primaryKeyDepth--
        }

        fun decrementNote() {
            if (noteDepth > 0) noteDepth--
        }
    }

    private enum class SectionType(val order: Int, val displayName: String, private val highlightToken: String) {
        COLUMNS(0, "columns", ""),
        INDEXES(1, "indexes", "indexes"),
        PRIMARY_KEY(2, "primary key", "primary"),
        NOTE(3, "note", "note");

        fun highlightEnd(start: Int, textLength: Int): Int {
            val length = if (highlightToken.isNotEmpty()) highlightToken.length else displayName.takeWhile { it != ' ' }.ifEmpty { displayName }.length
            return (start + length).coerceAtMost(textLength)
        }
    }

    private companion object {
        private const val KEYWORD_TABLE = "table"
        private const val KEYWORD_INDEX = "index"
        private const val KEYWORD_INDEXES = "indexes"
        private const val KEYWORD_PRIMARY = "primary"
        private const val KEYWORD_KEY = "key"
        private const val KEYWORD_NOTE = "note"
    }
}
