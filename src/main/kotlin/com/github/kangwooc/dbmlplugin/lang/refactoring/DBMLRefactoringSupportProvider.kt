package com.github.kangwooc.dbmlplugin.lang.refactoring

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTableDecl
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLColumnDef
import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement

class DBMLRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        return element is DBMLTableDecl || element is DBMLColumnDef
    }

    override fun isInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        return element is DBMLTableDecl || element is DBMLColumnDef
    }
}
