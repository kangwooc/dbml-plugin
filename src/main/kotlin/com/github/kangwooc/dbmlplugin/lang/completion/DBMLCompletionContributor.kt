package com.github.kangwooc.dbmlplugin.lang.completion

import com.github.kangwooc.dbmlplugin.lang.DBMLKeywords
import com.github.kangwooc.dbmlplugin.lang.highlighting.DBMLLexer
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLColumnAttrList
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLIndexesBlock
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLNoteBlock
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLPrimaryKeyBlock
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTableBody
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTokenTypes
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import java.util.ArrayDeque

class DBMLCompletionContributor : CompletionContributor(), DumbAware {

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val file = parameters.originalFile
        if (!file.isDbmlFile()) {
            super.fillCompletionVariants(parameters, result)
            return
        }

        when (detectContext(file, parameters.offset)) {
            CompletionContext.BRACKET_ATTRIBUTE -> addBracketAttributeCompletions(result)
            CompletionContext.TABLE_BODY -> addTableBodyCompletions(result)
            CompletionContext.INDEXES_BODY -> addIndexBodyCompletions(result)
            CompletionContext.INDEXES_ATTRIBUTE -> addIndexAttributeCompletions(result)
            CompletionContext.PRIMARY_KEY_BODY -> addPrimaryKeyCompletions(result)
            CompletionContext.NOTE_BODY -> addNoteBodyCompletions(result)
            CompletionContext.TABLE_GROUP_BODY -> addTableGroupBodyCompletions(result)
            CompletionContext.GLOBAL -> Unit
        }

        DBMLKeywords.ALL
            .sorted()
            .forEach { keyword ->
                var builder = LookupElementBuilder.create(keyword)
                    .withCaseSensitivity(false)

                if (keyword == "table") {
                    builder = builder.withInsertHandler(TABLE_DECLARATION_INSERT_HANDLER)
                }

                result.addElement(builder)
            }
    }

    private fun PsiFile.isDbmlFile(): Boolean {
        val extension = virtualFile?.extension ?: name.substringAfterLast('.', "")
        return extension.equals("dbml", ignoreCase = true)
    }

    private fun detectContext(file: PsiFile, offset: Int): CompletionContext {
        val text = file.viewProvider.document?.charsSequence ?: file.text
        if (text.isEmpty()) return CompletionContext.GLOBAL

        val clampedOffset = offset.coerceIn(0, text.length)
        val psiContext = detectPsiContext(file, clampedOffset)
        if (psiContext != null) return psiContext

        val currentStamp = file.viewProvider.document?.modificationStamp ?: file.viewProvider.modificationStamp
        val cached = file.getUserData(CONTEXT_CACHE_KEY)
        val cache = if (cached == null || cached.modificationStamp != currentStamp) {
            val segments = buildContextSegments(file)
            val refreshed = ContextCache(currentStamp, segments)
            file.putUserData(CONTEXT_CACHE_KEY, refreshed)
            refreshed
        } else {
            cached
        }

        return cache.contextAt(clampedOffset)
    }

    private fun detectPsiContext(file: PsiFile, offset: Int): CompletionContext? {
        val element = file.findElementAt(offset) ?: return null
        if (PsiTreeUtil.getParentOfType(element, DBMLColumnAttrList::class.java) != null) {
            return CompletionContext.BRACKET_ATTRIBUTE
        }
        if (PsiTreeUtil.getParentOfType(element, DBMLIndexesBlock::class.java) != null) {
            return CompletionContext.INDEXES_BODY
        }
        if (PsiTreeUtil.getParentOfType(element, DBMLPrimaryKeyBlock::class.java) != null) {
            return CompletionContext.PRIMARY_KEY_BODY
        }
        if (PsiTreeUtil.getParentOfType(element, DBMLNoteBlock::class.java) != null) {
            return CompletionContext.NOTE_BODY
        }
        if (PsiTreeUtil.getParentOfType(element, DBMLTableBody::class.java) != null) {
            return CompletionContext.TABLE_BODY
        }
        return null
    }

    private fun addBracketAttributeCompletions(result: CompletionResultSet) {
        ATTRIBUTE_SUGGESTIONS.forEach { suggestion ->
            result.addElement(
                LookupElementBuilder.create(suggestion.lookup)
                    .withPresentableText(suggestion.presentable)
                    .withTypeText("attribute", true)
                    .withCaseSensitivity(false)
            )
        }
    }

    private fun addTableBodyCompletions(result: CompletionResultSet) {
        COLUMN_TYPE_SUGGESTIONS.forEach { type ->
            result.addElement(
                LookupElementBuilder.create(type)
                    .withTypeText("column type", true)
                    .withCaseSensitivity(false)
            )
        }

        TABLE_SECTION_KEYWORDS.forEach { keyword ->
            var builder = LookupElementBuilder.create(keyword)
                .withTypeText("table keyword", true)
                .withCaseSensitivity(false)

            if (keyword in BLOCK_TABLE_SECTION_KEYWORDS) {
                builder = builder.withInsertHandler(BRACED_BLOCK_INSERT_HANDLER)
            }

            result.addElement(builder)
        }
    }

    private fun addIndexBodyCompletions(result: CompletionResultSet) {
        INDEX_BODY_SUGGESTIONS.forEach { suggestion ->
            result.addElement(
                LookupElementBuilder.create(suggestion)
                    .withTypeText("index", true)
                    .withCaseSensitivity(false)
            )
        }
    }

    private fun addIndexAttributeCompletions(result: CompletionResultSet) {
        INDEX_ATTRIBUTE_SUGGESTIONS.forEach { suggestion ->
            result.addElement(
                LookupElementBuilder.create(suggestion)
                    .withTypeText("index attribute", true)
                    .withCaseSensitivity(false)
            )
        }
    }

    private fun addPrimaryKeyCompletions(result: CompletionResultSet) {
        PRIMARY_KEY_SUGGESTIONS.forEach { suggestion ->
            result.addElement(
                LookupElementBuilder.create(suggestion)
                    .withTypeText("primary key", true)
                    .withCaseSensitivity(false)
            )
        }
    }

    private fun addNoteBodyCompletions(result: CompletionResultSet) {
        NOTE_BODY_SUGGESTIONS.forEach { suggestion ->
            result.addElement(
                LookupElementBuilder.create(suggestion)
                    .withTypeText("note", true)
                    .withCaseSensitivity(false)
            )
        }
    }

    private fun addTableGroupBodyCompletions(result: CompletionResultSet) {
        TABLE_GROUP_SUGGESTIONS.forEach { suggestion ->
            result.addElement(
                LookupElementBuilder.create(suggestion)
                    .withTypeText("table group", true)
                    .withCaseSensitivity(false)
            )
        }
    }

    private fun buildContextSegments(file: PsiFile): List<ContextSegment> {
        val text = file.viewProvider.document?.charsSequence ?: file.text
        if (text.isEmpty()) return listOf(ContextSegment(0, CompletionContext.GLOBAL))

        val lexer = DBMLLexer()
        lexer.start(text, 0, text.length, 0)

        var segments = mutableListOf<ContextSegment>()
        val scopeStack = ArrayDeque<ScopeType>()
        var pendingScope: ScopeType? = null
        var tableDepth = 0
        var indexesDepth = 0
        var anyBracketDepth = 0
        var tableAttributeDepth = 0
        var indexAttributeDepth = 0
        var primaryKeyDepth = 0
        var noteDepth = 0
        var tableGroupDepth = 0

        fun currentContext(): CompletionContext = when {
            noteDepth > 0 -> CompletionContext.NOTE_BODY
            indexAttributeDepth > 0 -> CompletionContext.INDEXES_ATTRIBUTE
            indexesDepth > 0 -> CompletionContext.INDEXES_BODY
            primaryKeyDepth > 0 -> CompletionContext.PRIMARY_KEY_BODY
            tableAttributeDepth > 0 -> CompletionContext.BRACKET_ATTRIBUTE
            tableDepth > 0 -> CompletionContext.TABLE_BODY
            tableGroupDepth > 0 -> CompletionContext.TABLE_GROUP_BODY
            else -> CompletionContext.GLOBAL
        }

        var lastContext = currentContext()
        segments += ContextSegment(0, lastContext)

        while (true) {
            val tokenType = lexer.tokenType ?: break
            val tokenStart = lexer.tokenStart
            val tokenEnd = lexer.tokenEnd

            when (tokenType) {
                DBMLTokenTypes.KEYWORD -> {
                    if (anyBracketDepth == 0) {
                        val keyword = text.subSequence(tokenStart, tokenEnd).toString().lowercase()
                        pendingScope = when (keyword) {
                            "table" -> ScopeType.TABLE
                            "enum" -> ScopeType.ENUM
                            "indexes" -> if (tableDepth > 0) ScopeType.TABLE_INDEXES else pendingScope
                            "primary" -> if (tableDepth > 0) ScopeType.PRIMARY_KEY_PENDING else pendingScope
                            "note" -> ScopeType.NOTE_BLOCK
                            "tablegroup" -> ScopeType.TABLE_GROUP
                            "key" -> when (pendingScope) {
                                ScopeType.PRIMARY_KEY_PENDING -> ScopeType.TABLE_PRIMARY_KEY
                                else -> pendingScope
                            }
                            else -> pendingScope
                        }
                    }
                }

                DBMLTokenTypes.BRACE -> {
                    val ch = text[tokenStart]
                    if (ch == '{') {
                        val scope = pendingScope ?: ScopeType.OTHER
                        scopeStack.addLast(scope)
                        when (scope) {
                            ScopeType.TABLE -> tableDepth++
                            ScopeType.TABLE_INDEXES -> indexesDepth++
                            ScopeType.TABLE_PRIMARY_KEY -> primaryKeyDepth++
                            ScopeType.NOTE_BLOCK -> noteDepth++
                            ScopeType.TABLE_GROUP -> tableGroupDepth++
                            else -> Unit
                        }
                    } else if (ch == '}') {
                        val popped = scopeStack.pollLast()
                        when (popped) {
                            ScopeType.TABLE -> tableDepth = (tableDepth - 1).coerceAtLeast(0)
                            ScopeType.TABLE_INDEXES -> indexesDepth = (indexesDepth - 1).coerceAtLeast(0)
                            ScopeType.TABLE_PRIMARY_KEY -> primaryKeyDepth = (primaryKeyDepth - 1).coerceAtLeast(0)
                            ScopeType.NOTE_BLOCK -> noteDepth = (noteDepth - 1).coerceAtLeast(0)
                            ScopeType.TABLE_GROUP -> tableGroupDepth = (tableGroupDepth - 1).coerceAtLeast(0)
                            else -> Unit
                        }
                    }
                    pendingScope = null
                }

                DBMLTokenTypes.BRACKET -> {
                    val ch = text[tokenStart]
                    if (ch == '[') {
                        anyBracketDepth++
                        if (indexesDepth > 0) {
                            indexAttributeDepth++
                        } else if (tableDepth > 0) {
                            tableAttributeDepth++
                        }
                    } else if (ch == ']') {
                        if (anyBracketDepth > 0) anyBracketDepth--
                        if (indexAttributeDepth > 0) {
                            indexAttributeDepth--
                        } else if (tableAttributeDepth > 0) {
                            tableAttributeDepth--
                        }
                    }
                }

                TokenType.WHITE_SPACE, DBMLTokenTypes.COMMENT -> Unit

                else -> {
                    if (!isTokenAllowedBetweenKeywordAndBrace(tokenType)) {
                        pendingScope = null
                    }
                }
            }

            if (tokenType != DBMLTokenTypes.KEYWORD && tokenType != TokenType.WHITE_SPACE && tokenType != DBMLTokenTypes.COMMENT) {
                pendingScope = pendingScope?.takeIf {
                    it == ScopeType.TABLE ||
                        it == ScopeType.ENUM ||
                        it == ScopeType.TABLE_INDEXES ||
                        it == ScopeType.PRIMARY_KEY_PENDING ||
                        it == ScopeType.TABLE_PRIMARY_KEY ||
                        it == ScopeType.TABLE_GROUP
                }
            }

            val newContext = currentContext()
            if (newContext != lastContext) {
                segments += ContextSegment(tokenEnd, newContext)
                lastContext = newContext
            }

            lexer.advance()
        }

        return segments
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

    private enum class CompletionContext {
        GLOBAL,
        TABLE_BODY,
        BRACKET_ATTRIBUTE,
        INDEXES_BODY,
        INDEXES_ATTRIBUTE,
        PRIMARY_KEY_BODY,
        NOTE_BODY,
        TABLE_GROUP_BODY
    }

    private data class AttributeSuggestion(val lookup: String, val presentable: String = lookup)

    private data class ContextSegment(val startOffset: Int, val context: CompletionContext)

    private data class ContextCache(val modificationStamp: Long, val segments: List<ContextSegment>) {
        fun contextAt(offset: Int): CompletionContext {
            if (segments.isEmpty()) return CompletionContext.GLOBAL
            var low = 0
            var high = segments.lastIndex
            var resultIndex = 0
            while (low <= high) {
                val mid = (low + high).ushr(1)
                val segment = segments[mid]
                if (segment.startOffset <= offset) {
                    resultIndex = mid
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }
            return segments[resultIndex].context
        }
    }

    private companion object {
        private val CONTEXT_CACHE_KEY = Key.create<ContextCache>("dbml.completion.context.cache")

        private enum class ScopeType {
            TABLE,
            ENUM,
            TABLE_INDEXES,
            PRIMARY_KEY_PENDING,
            TABLE_PRIMARY_KEY,
            NOTE_BLOCK,
            TABLE_GROUP,
            OTHER
        }

        private val BLOCK_TABLE_SECTION_KEYWORDS = setOf(
            "note",
            "indexes",
            "primary key"
        )

        private val COLUMN_TYPE_SUGGESTIONS = listOf(
            "int",
            "bigint",
            "varchar",
            "text",
            "boolean",
            "datetime",
            "timestamp",
            "uuid",
            "float",
            "decimal",
            "json",
            "enum"
        )

        private val TABLE_SECTION_KEYWORDS = listOf(
            "note",
            "indexes",
            "primary key",
            "ref"
        )

        private val ATTRIBUTE_SUGGESTIONS = listOf(
            AttributeSuggestion("pk"),
            AttributeSuggestion("unique"),
            AttributeSuggestion("not null"),
            AttributeSuggestion("null"),
            AttributeSuggestion("increment"),
            AttributeSuggestion("default: ", "default: …"),
            AttributeSuggestion("ref"),
            AttributeSuggestion("note")
        )

        private val INDEX_BODY_SUGGESTIONS = listOf(
            "index",
            "unique",
            "(",
            "on",
            "name: ",
            "type: ",
            "note: "
        )

        private val INDEX_ATTRIBUTE_SUGGESTIONS = listOf(
            "name: ",
            "type: ",
            "unique",
            "note"
        )

        private val PRIMARY_KEY_SUGGESTIONS = listOf(
            "columns: []",
            "name: ",
            "note: ",
            "type: ",
            "clustered"
        )

        private val NOTE_BODY_SUGGESTIONS = listOf(
            "TODO: ",
            "- ",
            "# "
        )

        private val TABLE_GROUP_SUGGESTIONS = listOf(
            "table ",
            "note: ",
            "color: ",
            "headercolor: "
        )

        private val BRACED_BLOCK_INSERT_HANDLER = InsertHandler<LookupElement> { context, _ ->
            val document = context.document
            val tailOffset = context.tailOffset
            val chars = document.charsSequence
            val existingBraceIndex = skipInlineWhitespace(chars, tailOffset)

            if (existingBraceIndex >= chars.length || chars[existingBraceIndex] != '{') {
                document.insertString(tailOffset, " {}")
                context.editor.caretModel.moveToOffset(tailOffset + 2)
            } else {
                context.editor.caretModel.moveToOffset(existingBraceIndex + 1)
            }
        }

        private val TABLE_DECLARATION_INSERT_HANDLER = InsertHandler<LookupElement> { context, _ ->
            val document = context.document
            val tailOffset = context.tailOffset
            val chars = document.charsSequence

            if (tailOffset >= chars.length || chars[tailOffset] != ' ') {
                document.insertString(tailOffset, " ")
            }

            context.editor.caretModel.moveToOffset(tailOffset + 1)
        }

        private fun skipInlineWhitespace(chars: CharSequence, start: Int): Int {
            var index = start
            while (index < chars.length) {
                val ch = chars[index]
                if (ch == '\n' || !ch.isWhitespace()) break
                index++
            }
            return index
        }
    }
}
