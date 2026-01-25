package com.github.kangwooc.dbmlplugin.lang

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object DBMLFileType : LanguageFileType(DBMLLanguage) {

    override fun getName(): String = "DBML"

    override fun getDescription(): String = "DATABASE MARKUP LANGUAGE FILE"

    override fun getDefaultExtension(): String = "dbml"

    override fun getIcon(): Icon = AllIcons.FileTypes.Json
}
