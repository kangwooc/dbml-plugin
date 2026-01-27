package com.github.kangwooc.dbmlplugin.lang.structure

import com.github.kangwooc.dbmlplugin.DBMLIcons
import com.github.kangwooc.dbmlplugin.lang.psi.*
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.util.PsiTreeUtil

class DBMLFileStructureViewElement(private val file: DBMLFile) :
    StructureViewTreeElement, SortableTreeElement {

    override fun getValue() = file

    override fun navigate(requestFocus: Boolean) {
        (file as? NavigatablePsiElement)?.navigate(requestFocus)
    }

    override fun canNavigate() = (file as? NavigatablePsiElement)?.canNavigate() ?: false

    override fun canNavigateToSource() = (file as? NavigatablePsiElement)?.canNavigateToSource() ?: false

    override fun getAlphaSortKey() = file.name ?: ""

    override fun getPresentation(): ItemPresentation {
        return PresentationData(file.name, null, DBMLIcons.FILE, null)
    }

    override fun getChildren(): Array<TreeElement> {
        val children = mutableListOf<TreeElement>()

        PsiTreeUtil.findChildrenOfType(file, DBMLTableDecl::class.java).forEach {
            children.add(DBMLTableStructureViewElement(it))
        }

        PsiTreeUtil.findChildrenOfType(file, DBMLEnumDecl::class.java).forEach {
            children.add(DBMLEnumStructureViewElement(it))
        }

        PsiTreeUtil.findChildrenOfType(file, DBMLProjectDecl::class.java).forEach {
            children.add(DBMLProjectStructureViewElement(it))
        }

        PsiTreeUtil.findChildrenOfType(file, DBMLTableGroupDecl::class.java).forEach {
            children.add(DBMLTableGroupStructureViewElement(it))
        }

        PsiTreeUtil.findChildrenOfType(file, DBMLRefDecl::class.java).forEach {
            children.add(DBMLRefStructureViewElement(it))
        }

        return children.toTypedArray()
    }
}

class DBMLTableStructureViewElement(private val table: DBMLTableDecl) :
    StructureViewTreeElement, SortableTreeElement {

    override fun getValue() = table

    override fun navigate(requestFocus: Boolean) {
        (table as? NavigatablePsiElement)?.navigate(requestFocus)
    }

    override fun canNavigate() = (table as? NavigatablePsiElement)?.canNavigate() ?: false

    override fun canNavigateToSource() = (table as? NavigatablePsiElement)?.canNavigateToSource() ?: false

    override fun getAlphaSortKey() = table.name ?: ""

    override fun getPresentation(): ItemPresentation {
        return PresentationData(table.name ?: "<unnamed>", "Table", DBMLIcons.TABLE, null)
    }

    override fun getChildren(): Array<TreeElement> {
        val children = mutableListOf<TreeElement>()

        PsiTreeUtil.findChildrenOfType(table, DBMLColumnDef::class.java).forEach {
            children.add(DBMLColumnStructureViewElement(it))
        }

        PsiTreeUtil.findChildOfType(table, DBMLIndexesBlock::class.java)?.let {
            children.add(DBMLIndexesStructureViewElement(it))
        }

        return children.toTypedArray()
    }
}

class DBMLColumnStructureViewElement(private val column: DBMLColumnDef) :
    StructureViewTreeElement, SortableTreeElement {

    override fun getValue() = column

    override fun navigate(requestFocus: Boolean) {
        (column as? NavigatablePsiElement)?.navigate(requestFocus)
    }

    override fun canNavigate() = (column as? NavigatablePsiElement)?.canNavigate() ?: false

    override fun canNavigateToSource() = (column as? NavigatablePsiElement)?.canNavigateToSource() ?: false

    override fun getAlphaSortKey() = column.name ?: ""

    override fun getPresentation(): ItemPresentation {
        val columnName = column.name ?: "<unnamed>"
        val columnType = PsiTreeUtil.findChildOfType(column, DBMLColumnType::class.java)?.text ?: ""
        return PresentationData("$columnName: $columnType", null, DBMLIcons.COLUMN, null)
    }

    override fun getChildren() = emptyArray<TreeElement>()
}

class DBMLEnumStructureViewElement(private val enum: DBMLEnumDecl) :
    StructureViewTreeElement, SortableTreeElement {

    override fun getValue() = enum

    override fun navigate(requestFocus: Boolean) {
        (enum as? NavigatablePsiElement)?.navigate(requestFocus)
    }

    override fun canNavigate() = (enum as? NavigatablePsiElement)?.canNavigate() ?: false

    override fun canNavigateToSource() = (enum as? NavigatablePsiElement)?.canNavigateToSource() ?: false

    override fun getAlphaSortKey() = enum.name ?: ""

    override fun getPresentation(): ItemPresentation {
        return PresentationData(enum.name ?: "<unnamed>", "Enum", DBMLIcons.ENUM, null)
    }

    override fun getChildren() = emptyArray<TreeElement>()
}

class DBMLProjectStructureViewElement(private val project: DBMLProjectDecl) :
    StructureViewTreeElement, SortableTreeElement {

    override fun getValue() = project

    override fun navigate(requestFocus: Boolean) {
        (project as? NavigatablePsiElement)?.navigate(requestFocus)
    }

    override fun canNavigate() = (project as? NavigatablePsiElement)?.canNavigate() ?: false

    override fun canNavigateToSource() = (project as? NavigatablePsiElement)?.canNavigateToSource() ?: false

    override fun getAlphaSortKey() = "Project"

    override fun getPresentation(): ItemPresentation {
        return PresentationData("Project", null, DBMLIcons.PROJECT, null)
    }

    override fun getChildren() = emptyArray<TreeElement>()
}

class DBMLTableGroupStructureViewElement(private val group: DBMLTableGroupDecl) :
    StructureViewTreeElement, SortableTreeElement {

    override fun getValue() = group

    override fun navigate(requestFocus: Boolean) {
        (group as? NavigatablePsiElement)?.navigate(requestFocus)
    }

    override fun canNavigate() = (group as? NavigatablePsiElement)?.canNavigate() ?: false

    override fun canNavigateToSource() = (group as? NavigatablePsiElement)?.canNavigateToSource() ?: false

    override fun getAlphaSortKey() = "TableGroup"

    override fun getPresentation(): ItemPresentation {
        return PresentationData("TableGroup", null, DBMLIcons.TABLE_GROUP, null)
    }

    override fun getChildren() = emptyArray<TreeElement>()
}

class DBMLRefStructureViewElement(private val ref: DBMLRefDecl) :
    StructureViewTreeElement, SortableTreeElement {

    override fun getValue() = ref

    override fun navigate(requestFocus: Boolean) {
        (ref as? NavigatablePsiElement)?.navigate(requestFocus)
    }

    override fun canNavigate() = (ref as? NavigatablePsiElement)?.canNavigate() ?: false

    override fun canNavigateToSource() = (ref as? NavigatablePsiElement)?.canNavigateToSource() ?: false

    override fun getAlphaSortKey() = ref.text.take(30)

    override fun getPresentation(): ItemPresentation {
        val text = ref.text.replace("\n", " ").take(50)
        return PresentationData(text, "Reference", DBMLIcons.REF, null)
    }

    override fun getChildren() = emptyArray<TreeElement>()
}

class DBMLIndexesStructureViewElement(private val indexes: DBMLIndexesBlock) :
    StructureViewTreeElement, SortableTreeElement {

    override fun getValue() = indexes

    override fun navigate(requestFocus: Boolean) {
        (indexes as? NavigatablePsiElement)?.navigate(requestFocus)
    }

    override fun canNavigate() = (indexes as? NavigatablePsiElement)?.canNavigate() ?: false

    override fun canNavigateToSource() = (indexes as? NavigatablePsiElement)?.canNavigateToSource() ?: false

    override fun getAlphaSortKey() = "indexes"

    override fun getPresentation(): ItemPresentation {
        return PresentationData("indexes", null, DBMLIcons.INDEX, null)
    }

    override fun getChildren() = emptyArray<TreeElement>()
}
