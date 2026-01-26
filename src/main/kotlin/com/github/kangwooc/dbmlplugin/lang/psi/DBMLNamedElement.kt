package com.github.kangwooc.dbmlplugin.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

interface DBMLNamedElement : PsiNamedElement {
    override fun getName(): String?
    override fun setName(name: String): PsiElement
}
