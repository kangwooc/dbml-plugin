package com.github.kangwooc.dbmlplugin.lang.refactoring

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLColumnDef
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLEnumDecl
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTableDecl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DBMLRenameTest : BasePlatformTestCase() {

    fun `test table setName works correctly`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int [pk]
              name varchar
            }
        """.trimIndent())

        val table = PsiTreeUtil.findChildOfType(myFixture.file, DBMLTableDecl::class.java)
        assertNotNull("Should find table declaration", table)
        assertEquals("Original table name should be 'users'", "users", table?.name)
        assertNotNull("Table should have name identifier", table?.nameIdentifier)
    }

    fun `test column setName works correctly`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int [pk]
              email varchar
            }
        """.trimIndent())

        val columns = PsiTreeUtil.findChildrenOfType(myFixture.file, DBMLColumnDef::class.java)
        assertTrue("Should find columns", columns.isNotEmpty())
        
        val idColumn = columns.find { it.name == "id" }
        assertNotNull("Should find 'id' column", idColumn)
        assertNotNull("Column should have name identifier", idColumn?.nameIdentifier)
    }

    fun `test enum setName works correctly`() {
        myFixture.configureByText("test.dbml", """
            enum user_status {
              active
              inactive
              pending
            }
        """.trimIndent())

        val enumDecl = PsiTreeUtil.findChildOfType(myFixture.file, DBMLEnumDecl::class.java)
        assertNotNull("Should find enum declaration", enumDecl)
        assertEquals("Original enum name should be 'user_status'", "user_status", enumDecl?.name)
        assertNotNull("Enum should have name identifier", enumDecl?.nameIdentifier)
    }

    fun `test table name identifier exists`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int [pk]
            }
        """.trimIndent())

        val table = PsiTreeUtil.findChildOfType(myFixture.file, DBMLTableDecl::class.java)
        assertNotNull("Should find table declaration", table)
        assertNotNull("Table should have name identifier", table?.nameIdentifier)
        assertEquals("Table name should be 'users'", "users", table?.name)
    }

    fun `test column name identifier exists`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int [pk]
              email varchar
            }
        """.trimIndent())

        val columns = PsiTreeUtil.findChildrenOfType(myFixture.file, DBMLColumnDef::class.java)
        assertTrue("Should find columns", columns.isNotEmpty())
        
        columns.forEach { column ->
            assertNotNull("Column '${column.name}' should have name identifier", column.nameIdentifier)
        }
    }

    fun `test enum name identifier exists`() {
        myFixture.configureByText("test.dbml", """
            enum status {
              active
              inactive
            }
        """.trimIndent())

        val enumDecl = PsiTreeUtil.findChildOfType(myFixture.file, DBMLEnumDecl::class.java)
        assertNotNull("Should find enum declaration", enumDecl)
        assertNotNull("Enum should have name identifier", enumDecl?.nameIdentifier)
        assertEquals("Enum name should be 'status'", "status", enumDecl?.name)
    }

    fun `test rename column definition`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              <caret>id int [pk]
              name varchar
            }
        """.trimIndent())

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        val columnDef = PsiTreeUtil.getParentOfType(element, DBMLColumnDef::class.java)
        
        assertNotNull("Should find column definition", columnDef)
        assertEquals("Original column name should be 'id'", "id", columnDef?.name)
        
        myFixture.renameElementAtCaret("user_id")
        
        val columns = PsiTreeUtil.findChildrenOfType(myFixture.file, DBMLColumnDef::class.java)
        val renamedColumn = columns.find { it.name == "user_id" }
        assertNotNull("Column should be renamed to 'user_id'", renamedColumn)
    }
}
