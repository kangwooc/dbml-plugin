package com.github.kangwooc.dbmlplugin.lang.findUsages

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLNamedElement
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.github.kangwooc.dbmlplugin.lang.highlighting.DBMLLexer
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTokenTypes
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTableDecl
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLColumnDef
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLEnumDecl

class DBMLFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner {
        return DefaultWordsScanner(
            DBMLLexer(),
            TokenSet.create(DBMLTokenTypes.IDENTIFIER),
            TokenSet.create(DBMLTokenTypes.COMMENT),
            TokenSet.create(DBMLTokenTypes.STRING, DBMLTokenTypes.NUMBER)
        )
    }

    override fun canFindUsagesFor(element: PsiElement): Boolean {
        return element is DBMLNamedElement
    }

    override fun getHelpId(element: PsiElement): String? {
        return null
    }

    override fun getType(element: PsiElement): String {
        return when (element) {
            is DBMLTableDecl -> "table"
            is DBMLColumnDef -> "column"
            is DBMLEnumDecl -> "enum"
            else -> "element"
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return when (element) {
            is DBMLNamedElement -> element.name ?: "unnamed"
            else -> "unnamed"
        }
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return when (element) {
            is DBMLNamedElement -> element.name ?: element.text
            else -> element.text
        }
    }
}
