package com.github.kangwooc.dbmlplugin.lang.inspection

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLFile
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLIndexDef
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLIndexesBlock
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLNoteBlock
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLPrimaryKeyBlock
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTableBody
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTableDecl
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

class DBMLTableStructureInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is DBMLFile) return
                analyzeFile(file, holder)
            }
        }
    }

    private fun analyzeFile(file: DBMLFile, holder: ProblemsHolder) {
        val tables = PsiTreeUtil.findChildrenOfType(file, DBMLTableDecl::class.java)
        
        for (table in tables) {
            val body = PsiTreeUtil.findChildOfType(table, DBMLTableBody::class.java) ?: continue
            
            checkForBareIndexDeclarations(body, holder)
            checkSectionOrdering(body, holder)
        }
    }

    private fun checkForBareIndexDeclarations(body: DBMLTableBody, holder: ProblemsHolder) {
        val indexDefs = PsiTreeUtil.findChildrenOfType(body, DBMLIndexDef::class.java)
        for (indexDef in indexDefs) {
            val parentIndexesBlock = PsiTreeUtil.getParentOfType(indexDef, DBMLIndexesBlock::class.java, true)
            val isInsideIndexesBlock = parentIndexesBlock != null && PsiTreeUtil.isAncestor(body, parentIndexesBlock, false)
            
            if (!isInsideIndexesBlock) {
                holder.registerProblem(
                    indexDef,
                    "Wrap index declarations inside an indexes { } block"
                )
            }
        }
    }

    private fun checkSectionOrdering(body: DBMLTableBody, holder: ProblemsHolder) {
        var lastSection = SectionType.COLUMNS
        body.children.forEach { child ->
            val section = when (child) {
                is DBMLIndexesBlock -> SectionType.INDEXES
                is DBMLPrimaryKeyBlock -> SectionType.PRIMARY_KEY
                is DBMLNoteBlock -> SectionType.NOTE
                else -> null
            }
            if (section != null) {
                if (section.order < lastSection.order) {
                    holder.registerProblem(
                        child,
                        "'${section.displayName}' section must appear before '${lastSection.displayName}' section."
                    )
                } else {
                    lastSection = section
                }
            }
        }
    }

    private enum class SectionType(val order: Int, val displayName: String) {
        COLUMNS(0, "columns"),
        INDEXES(1, "indexes"),
        PRIMARY_KEY(2, "primary key"),
        NOTE(3, "note")
    }
}
