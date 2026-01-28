package com.github.kangwooc.dbmlplugin.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil

open class DBMLStatement(node: ASTNode) : ASTWrapperPsiElement(node)

class DBMLTableDecl(node: ASTNode) : DBMLStatement(node), DBMLNamedElement {
    override fun getName(): String? {
        val keywords = node.getChildren(TokenSet.create(DBMLTokenTypes.KEYWORD))
        if (keywords.isEmpty()) return null
        
        var nextSibling: ASTNode? = keywords[0].treeNext
        while (nextSibling != null) {
            if (nextSibling.elementType == DBMLTokenTypes.IDENTIFIER) {
                return nextSibling.text
            }
            nextSibling = nextSibling.treeNext
        }
        return null
    }

    override fun setName(name: String): PsiElement {
        val identifierNode = findIdentifierNode() ?: return this
        return DBMLElementFactory.replaceIdentifier(this, identifierNode, name)
    }

    override fun getNameIdentifier(): PsiElement? {
        return findIdentifierNode()?.psi
    }

    private fun findIdentifierNode(): ASTNode? {
        val keywords = node.getChildren(TokenSet.create(DBMLTokenTypes.KEYWORD))
        if (keywords.isEmpty()) return null

        var nextSibling: ASTNode? = keywords[0].treeNext
        while (nextSibling != null) {
            if (nextSibling.elementType == DBMLTokenTypes.IDENTIFIER) {
                return nextSibling
            }
            nextSibling = nextSibling.treeNext
        }
        return null
    }
}

class DBMLTableBody(node: ASTNode) : DBMLStatement(node)

class DBMLColumnDef(node: ASTNode) : DBMLStatement(node), DBMLNamedElement {
    override fun getName(): String? {
        return PsiTreeUtil.findChildOfType(this, DBMLColumnName::class.java)?.text
    }

    override fun setName(name: String): PsiElement {
        val columnName = PsiTreeUtil.findChildOfType(this, DBMLColumnName::class.java) ?: return this
        val identifierNode = columnName.node.findChildByType(DBMLTokenTypes.IDENTIFIER) ?: return this
        return DBMLElementFactory.replaceIdentifier(this, identifierNode, name)
    }

    override fun getNameIdentifier(): PsiElement? {
        val columnName = PsiTreeUtil.findChildOfType(this, DBMLColumnName::class.java) ?: return null
        return columnName.node.findChildByType(DBMLTokenTypes.IDENTIFIER)?.psi
    }
}

class DBMLColumnName(node: ASTNode) : DBMLStatement(node)

class DBMLColumnType(node: ASTNode) : DBMLStatement(node)

class DBMLColumnAttrList(node: ASTNode) : DBMLStatement(node)

class DBMLEnumDecl(node: ASTNode) : DBMLStatement(node), DBMLNamedElement {
    override fun getName(): String? {
        val keywords = node.getChildren(TokenSet.create(DBMLTokenTypes.KEYWORD))
        if (keywords.isEmpty()) return null
        
        var nextSibling: ASTNode? = keywords[0].treeNext
        while (nextSibling != null) {
            if (nextSibling.elementType == DBMLTokenTypes.IDENTIFIER) {
                return nextSibling.text
            }
            nextSibling = nextSibling.treeNext
        }
        return null
    }

    override fun setName(name: String): PsiElement {
        val identifierNode = findIdentifierNode() ?: return this
        return DBMLElementFactory.replaceIdentifier(this, identifierNode, name)
    }

    override fun getNameIdentifier(): PsiElement? {
        return findIdentifierNode()?.psi
    }

    private fun findIdentifierNode(): ASTNode? {
        val keywords = node.getChildren(TokenSet.create(DBMLTokenTypes.KEYWORD))
        if (keywords.isEmpty()) return null

        var nextSibling: ASTNode? = keywords[0].treeNext
        while (nextSibling != null) {
            if (nextSibling.elementType == DBMLTokenTypes.IDENTIFIER) {
                return nextSibling
            }
            nextSibling = nextSibling.treeNext
        }
        return null
    }
}

class DBMLRefDecl(node: ASTNode) : DBMLStatement(node)

class DBMLProjectDecl(node: ASTNode) : DBMLStatement(node)

class DBMLTableGroupDecl(node: ASTNode) : DBMLStatement(node)

class DBMLNoteDecl(node: ASTNode) : DBMLStatement(node)

class DBMLNoteBlock(node: ASTNode) : DBMLStatement(node)

class DBMLIndexesBlock(node: ASTNode) : DBMLStatement(node)

class DBMLIndexDef(node: ASTNode) : DBMLStatement(node)

class DBMLPrimaryKeyBlock(node: ASTNode) : DBMLStatement(node)

class DBMLUnknownStatement(node: ASTNode) : DBMLStatement(node)

object DBMLElementFactory {
    fun replaceIdentifier(element: PsiElement, identifierNode: ASTNode, newName: String): PsiElement {
        val project = element.project
        val dummyText = "Table $newName {}"
        val dummyFile = com.intellij.psi.PsiFileFactory.getInstance(project)
            .createFileFromText("dummy.dbml", com.github.kangwooc.dbmlplugin.lang.DBMLFileType, dummyText)

        val newIdentifier = PsiTreeUtil.findChildOfType(dummyFile, DBMLTableDecl::class.java)
            ?.node?.findChildByType(DBMLTokenTypes.IDENTIFIER)?.psi
            ?: return element

        identifierNode.psi.replace(newIdentifier)
        return element
    }
}


