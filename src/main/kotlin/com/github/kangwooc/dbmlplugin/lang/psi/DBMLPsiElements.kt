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
        return this
    }
}

class DBMLTableBody(node: ASTNode) : DBMLStatement(node)

class DBMLColumnDef(node: ASTNode) : DBMLStatement(node), DBMLNamedElement {
    override fun getName(): String? {
        return PsiTreeUtil.findChildOfType(this, DBMLColumnName::class.java)?.text
    }

    override fun setName(name: String): PsiElement {
        return this
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
        return this
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


