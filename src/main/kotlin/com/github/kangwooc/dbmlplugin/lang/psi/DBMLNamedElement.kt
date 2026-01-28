package com.github.kangwooc.dbmlplugin.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

interface DBMLNamedElement : PsiNameIdentifierOwner {
    override fun getName(): String?
    override fun setName(name: String): PsiElement
    override fun getNameIdentifier(): PsiElement?
}
