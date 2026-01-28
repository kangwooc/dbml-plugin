package com.github.kangwooc.dbmlplugin.lang.reference

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTableDecl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class DBMLTableReference(element: PsiElement, textRange: TextRange) : 
    PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {

    private val tableName: String = element.text.substring(textRange.startOffset, textRange.endOffset)

    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val file = element.containingFile
        val tables = PsiTreeUtil.findChildrenOfType(file, DBMLTableDecl::class.java)
        
        return tables
            .filter { it.name == tableName }
            .map { PsiElementResolveResult(it) }
            .toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        val file = element.containingFile
        val tables = PsiTreeUtil.findChildrenOfType(file, DBMLTableDecl::class.java)
        
        return tables
            .mapNotNull { it.name }
            .distinct()
            .toTypedArray()
    }
}
