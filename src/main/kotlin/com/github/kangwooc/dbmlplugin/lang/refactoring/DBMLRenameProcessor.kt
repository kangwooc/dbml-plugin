package com.github.kangwooc.dbmlplugin.lang.refactoring

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLNamedElement
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenamePsiElementProcessor

class DBMLRenameProcessor : RenamePsiElementProcessor() {
    override fun canProcessElement(element: PsiElement): Boolean {
        return element is DBMLNamedElement
    }
}
