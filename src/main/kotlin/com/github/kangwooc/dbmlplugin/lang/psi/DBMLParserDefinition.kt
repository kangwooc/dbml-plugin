package com.github.kangwooc.dbmlplugin.lang.psi

import com.github.kangwooc.dbmlplugin.lang.DBMLLanguage
import com.github.kangwooc.dbmlplugin.lang.highlighting.DBMLLexer
import com.github.kangwooc.dbmlplugin.lang.parser.DBMLParser
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class DBMLParserDefinition : ParserDefinition {

    override fun createLexer(project: Project) = DBMLLexer()

    override fun createParser(project: Project): PsiParser = DBMLParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getWhitespaceTokens(): TokenSet = TokenSet.create(TokenType.WHITE_SPACE)

    override fun getCommentTokens(): TokenSet = TokenSet.create(DBMLTokenTypes.COMMENT)

    override fun getStringLiteralElements(): TokenSet = TokenSet.create(DBMLTokenTypes.STRING)

    override fun createElement(node: ASTNode): PsiElement = when (node.elementType) {
        DBMLElementTypes.TABLE_DECL -> DBMLTableDecl(node)
        DBMLElementTypes.TABLE_BODY -> DBMLTableBody(node)
        DBMLElementTypes.COLUMN_DEF -> DBMLColumnDef(node)
        DBMLElementTypes.COLUMN_NAME -> DBMLColumnName(node)
        DBMLElementTypes.COLUMN_TYPE -> DBMLColumnType(node)
        DBMLElementTypes.COLUMN_ATTR_LIST -> DBMLColumnAttrList(node)
        DBMLElementTypes.ENUM_DECL -> DBMLEnumDecl(node)
        DBMLElementTypes.REF_DECL -> DBMLRefDecl(node)
        DBMLElementTypes.PROJECT_DECL -> DBMLProjectDecl(node)
        DBMLElementTypes.TABLEGROUP_DECL -> DBMLTableGroupDecl(node)
        DBMLElementTypes.NOTE_DECL -> DBMLNoteDecl(node)
        DBMLElementTypes.NOTE_BLOCK -> DBMLNoteBlock(node)
        DBMLElementTypes.INDEXES_BLOCK -> DBMLIndexesBlock(node)
        DBMLElementTypes.INDEX_DEF -> DBMLIndexDef(node)
        DBMLElementTypes.PRIMARY_KEY_BLOCK -> DBMLPrimaryKeyBlock(node)
        DBMLElementTypes.UNKNOWN_STMT -> DBMLUnknownStatement(node)
        else -> DBMLStatement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile = DBMLFile(viewProvider)

    companion object {
        private val FILE = IFileElementType(DBMLLanguage)
    }
}
