package com.github.kangwooc.dbmlplugin.lang.parser

import com.github.kangwooc.dbmlplugin.lang.psi.*
import com.intellij.testFramework.ParsingTestCase

class DBMLParserTest : ParsingTestCase("parser", "dbml", DBMLParserDefinition()) {

    fun `test parses simple table declaration`() {
        val file = parseFile("simple_table", loadFile("simple_table.dbml"))
        ensureNoErrorElements()
        
        val tableDecl = findFirstElement<DBMLTableDecl>(file)
        assertNotNull("Expected TABLE_DECL element", tableDecl)

        val tableBody = findFirstElement<DBMLTableBody>(file)
        assertNotNull("Expected TABLE_BODY element", tableBody)

        val columns = findAllElements<DBMLColumnDef>(file)
        assertTrue("Expected at least one column", columns.isNotEmpty())
    }

    fun `test parses table with indexes block`() {
        val file = parseFile("table_with_indexes", loadFile("table_with_indexes.dbml"))
        ensureNoErrorElements()

        val tableDecl = findFirstElement<DBMLTableDecl>(file)
        assertNotNull("Expected TABLE_DECL element", tableDecl)
    }

    fun `test parses enum declaration`() {
        val file = parseFile("enum_and_ref", loadFile("enum_and_ref.dbml"))
        ensureNoErrorElements()

        val enumDecl = findFirstElement<DBMLEnumDecl>(file)
        assertNotNull("Expected ENUM_DECL element", enumDecl)
    }

    fun `test parses ref declaration`() {
        val file = parseFile("enum_and_ref", loadFile("enum_and_ref.dbml"))
        ensureNoErrorElements()

        val refDecl = findFirstElement<DBMLRefDecl>(file)
        assertNotNull("Expected REF_DECL element", refDecl)
    }

    fun `test parses project declaration`() {
        val file = parseFile("project_and_notes", loadFile("project_and_notes.dbml"))
        ensureNoErrorElements()

        val projectDecl = findFirstElement<DBMLProjectDecl>(file)
        assertNotNull("Expected PROJECT_DECL element", projectDecl)
    }

    fun `test parses tablegroup declaration`() {
        val file = parseFile("project_and_notes", loadFile("project_and_notes.dbml"))
        ensureNoErrorElements()

        val tablegroupDecl = findFirstElement<DBMLTableGroupDecl>(file)
        assertNotNull("Expected TABLEGROUP_DECL element", tablegroupDecl)
    }

    fun `test parses note declaration`() {
        val file = parseFile("project_and_notes", loadFile("project_and_notes.dbml"))
        ensureNoErrorElements()

        val noteDecl = findFirstElement<DBMLNoteDecl>(file)
        val noteBlock = findFirstElement<DBMLNoteBlock>(file)
        assertTrue("Expected NOTE_DECL or NOTE_BLOCK element", noteDecl != null || noteBlock != null)
    }

    fun `test parses column with attributes`() {
        val file = parseFile("simple_table", loadFile("simple_table.dbml"))
        ensureNoErrorElements()

        val attrLists = findAllElements<DBMLColumnAttrList>(file)
        assertTrue("Expected at least one column with attributes", attrLists.isNotEmpty())
    }

    fun `test parses multiple statements`() {
        val file = parseFile("enum_and_ref", loadFile("enum_and_ref.dbml"))
        ensureNoErrorElements()

        val tables = findAllElements<DBMLTableDecl>(file)
        assertTrue("Expected at least one table declaration", tables.isNotEmpty())

        val enums = findAllElements<DBMLEnumDecl>(file)
        assertTrue("Expected at least one enum declaration", enums.isNotEmpty())

        val refs = findAllElements<DBMLRefDecl>(file)
        assertTrue("Expected at least one ref declaration", refs.isNotEmpty())
    }

    override fun getTestDataPath(): String = "src/test/testData"

    private inline fun <reified T : com.intellij.psi.PsiElement> findFirstElement(file: com.intellij.psi.PsiFile): T? {
        return com.intellij.psi.util.PsiTreeUtil.findChildOfType(file, T::class.java, true)
    }

    private inline fun <reified T : com.intellij.psi.PsiElement> findAllElements(file: com.intellij.psi.PsiFile): List<T> {
        return com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(file, T::class.java).toList()
    }
}
