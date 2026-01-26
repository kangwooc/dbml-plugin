package com.github.kangwooc.dbmlplugin.lang.reference

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLRefDecl
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

class DBMLReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(DBMLRefDecl::class.java),
            DBMLReferenceProvider()
        )
    }
}

class DBMLReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        val text = element.text
        val references = mutableListOf<PsiReference>()
        
        val tablePattern = Regex("""(\w+)\.(\w+)""")
        tablePattern.findAll(text).forEach { match ->
            val tableName = match.groupValues[1]
            val startOffset = match.range.first
            val endOffset = startOffset + tableName.length
            
            references.add(
                DBMLTableReference(
                    element,
                    com.intellij.openapi.util.TextRange(startOffset, endOffset)
                )
            )
        }
        
        return references.toTypedArray()
    }
}
