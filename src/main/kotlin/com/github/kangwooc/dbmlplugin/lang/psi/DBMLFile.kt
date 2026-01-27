package com.github.kangwooc.dbmlplugin.lang.psi

import com.github.kangwooc.dbmlplugin.lang.DBMLFileType
import com.github.kangwooc.dbmlplugin.lang.DBMLLanguage
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider

class DBMLFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, DBMLLanguage) {

    override fun getFileType() = DBMLFileType

    override fun toString(): String = "DBML File"
}
