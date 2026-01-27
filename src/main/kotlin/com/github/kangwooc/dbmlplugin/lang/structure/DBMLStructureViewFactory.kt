package com.github.kangwooc.dbmlplugin.lang.structure

import com.github.kangwooc.dbmlplugin.lang.psi.*
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class DBMLStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile) =
        object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?) =
                DBMLStructureViewModel(psiFile, editor)
        }
}

class DBMLStructureViewModel(psiFile: PsiFile, editor: Editor?) :
    TextEditorBasedStructureViewModel(editor, psiFile) {

    override fun getRoot() = DBMLFileStructureViewElement(psiFile as DBMLFile)

    override fun getSorters() = arrayOf(Sorter.ALPHA_SORTER)

    override fun getSuitableClasses() = arrayOf(
        DBMLTableDecl::class.java,
        DBMLEnumDecl::class.java,
        DBMLProjectDecl::class.java,
        DBMLTableGroupDecl::class.java,
        DBMLRefDecl::class.java,
        DBMLNoteDecl::class.java,
        DBMLColumnDef::class.java
    )
}
