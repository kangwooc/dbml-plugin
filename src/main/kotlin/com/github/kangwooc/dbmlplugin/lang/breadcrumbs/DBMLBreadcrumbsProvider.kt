package com.github.kangwooc.dbmlplugin.lang.breadcrumbs

import com.github.kangwooc.dbmlplugin.DBMLIcons
import com.github.kangwooc.dbmlplugin.lang.DBMLLanguage
import com.github.kangwooc.dbmlplugin.lang.psi.*
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import javax.swing.Icon

class DBMLBreadcrumbsProvider : BreadcrumbsProvider {

    override fun getLanguages(): Array<Language> = arrayOf(DBMLLanguage)

    override fun acceptElement(element: PsiElement): Boolean {
        return element is DBMLTableDecl ||
               element is DBMLColumnDef ||
               element is DBMLEnumDecl ||
               element is DBMLProjectDecl ||
               element is DBMLTableGroupDecl ||
               element is DBMLIndexesBlock
    }

    override fun getElementInfo(element: PsiElement): String {
        return when (element) {
            is DBMLTableDecl -> "Table: ${element.name ?: "<unnamed>"}"
            is DBMLColumnDef -> element.name ?: "<unnamed>"
            is DBMLEnumDecl -> "Enum: ${element.name ?: "<unnamed>"}"
            is DBMLProjectDecl -> "Project"
            is DBMLTableGroupDecl -> "TableGroup"
            is DBMLIndexesBlock -> "indexes"
            else -> element.text.take(30)
        }
    }

    override fun getElementTooltip(element: PsiElement): String? {
        return when (element) {
            is DBMLTableDecl -> "Table declaration"
            is DBMLColumnDef -> {
                val columnType = com.intellij.psi.util.PsiTreeUtil.findChildOfType(
                    element,
                    DBMLColumnType::class.java
                )?.text
                "Column: ${element.name}${columnType?.let { ": $it" } ?: ""}"
            }
            is DBMLEnumDecl -> "Enum declaration"
            is DBMLProjectDecl -> "Project metadata"
            is DBMLTableGroupDecl -> "Table group"
            is DBMLIndexesBlock -> "Indexes block"
            else -> null
        }
    }

    override fun getElementIcon(element: PsiElement): Icon? {
        return when (element) {
            is DBMLTableDecl -> DBMLIcons.TABLE
            is DBMLColumnDef -> DBMLIcons.COLUMN
            is DBMLEnumDecl -> DBMLIcons.ENUM
            is DBMLProjectDecl -> DBMLIcons.PROJECT
            is DBMLTableGroupDecl -> DBMLIcons.TABLE_GROUP
            is DBMLIndexesBlock -> DBMLIcons.INDEX
            else -> null
        }
    }
}
