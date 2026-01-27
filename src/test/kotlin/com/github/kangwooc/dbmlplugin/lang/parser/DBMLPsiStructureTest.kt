package com.github.kangwooc.dbmlplugin.lang.parser

import com.github.kangwooc.dbmlplugin.lang.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DBMLPsiStructureTest : BasePlatformTestCase() {

    fun `test table declaration has correct children`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int [pk]
              name varchar(255)
            }
        """.trimIndent())

        val table = PsiTreeUtil.findChildOfType(myFixture.file, DBMLTableDecl::class.java)
        assertNotNull("Expected table declaration", table)
        assertEquals("users", table?.name)

        val tableBody = PsiTreeUtil.findChildOfType(table, DBMLTableBody::class.java)
        assertNotNull("Expected table body", tableBody)

        val columns = PsiTreeUtil.findChildrenOfType(table, DBMLColumnDef::class.java)
        assertTrue("Expected at least 1 column", columns.isNotEmpty())
    }

    fun `test column with attributes has correct structure`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int [pk, increment]
            }
        """.trimIndent())

        val column = PsiTreeUtil.findChildOfType(myFixture.file, DBMLColumnDef::class.java)
        assertNotNull("Expected column definition", column)
        
        val columnName = PsiTreeUtil.findChildOfType(column, DBMLColumnName::class.java)
        assertNotNull("Expected column name", columnName)
        assertEquals("id", columnName?.text)

        val columnType = PsiTreeUtil.findChildOfType(column, DBMLColumnType::class.java)
        assertNotNull("Expected column type", columnType)
        assertEquals("int", columnType?.text)

        val attrList = PsiTreeUtil.findChildOfType(column, DBMLColumnAttrList::class.java)
        assertNotNull("Expected attribute list", attrList)
    }

    fun `test enum declaration structure`() {
        myFixture.configureByText("test.dbml", """
            enum user_status {
              active
              inactive
              banned
            }
        """.trimIndent())

        val enumDecl = PsiTreeUtil.findChildOfType(myFixture.file, DBMLEnumDecl::class.java)
        assertNotNull("Expected enum declaration", enumDecl)
        assertEquals("user_status", enumDecl?.name)
    }

    fun `test ref declaration structure`() {
        myFixture.configureByText("test.dbml", """
            Ref: posts.user_id > users.id [delete: cascade]
        """.trimIndent())

        val refDecl = PsiTreeUtil.findChildOfType(myFixture.file, DBMLRefDecl::class.java)
        assertNotNull("Expected ref declaration", refDecl)
        assertTrue("Ref should contain reference arrow", refDecl!!.text.contains(">"))
    }

    fun `test project declaration structure`() {
        myFixture.configureByText("test.dbml", """
            Project ecommerce {
              database_type: 'PostgreSQL'
              Note: 'Sample database'
            }
        """.trimIndent())

        val projectDecl = PsiTreeUtil.findChildOfType(myFixture.file, DBMLProjectDecl::class.java)
        assertNotNull("Expected project declaration", projectDecl)
        assertTrue("Project should contain settings", projectDecl!!.text.contains("database_type"))
    }

    fun `test tablegroup declaration structure`() {
        myFixture.configureByText("test.dbml", """
            TableGroup management {
              users
              roles
              permissions
            }
        """.trimIndent())

        val tablegroupDecl = PsiTreeUtil.findChildOfType(myFixture.file, DBMLTableGroupDecl::class.java)
        assertNotNull("Expected tablegroup declaration", tablegroupDecl)
        assertTrue("TableGroup should contain table names", tablegroupDecl!!.text.contains("users"))
    }

    fun `test inline ref in column`() {
        myFixture.configureByText("test.dbml", """
            Table posts {
              user_id int [ref: > users.id]
            }
        """.trimIndent())

        val column = PsiTreeUtil.findChildOfType(myFixture.file, DBMLColumnDef::class.java)
        assertNotNull("Expected column definition", column)

        val attrList = PsiTreeUtil.findChildOfType(column, DBMLColumnAttrList::class.java)
        assertNotNull("Expected attribute list", attrList)
        assertTrue("Expected ref in attributes", attrList!!.text.contains("ref:"))
    }

    fun `test note declaration exists`() {
        myFixture.configureByText("test.dbml", """
            Note: 'Simple note'
        """.trimIndent())

        val noteDecl = PsiTreeUtil.findChildOfType(myFixture.file, DBMLNoteDecl::class.java)
        assertNotNull("Expected note declaration", noteDecl)
    }

    fun `test multiple top-level declarations`() {
        myFixture.configureByText("test.dbml", """
            enum status { active }
            
            Table users { id int }
            
            Table posts { id int }
            
            Ref: posts.user_id > users.id
        """.trimIndent())

        val enums = PsiTreeUtil.findChildrenOfType(myFixture.file, DBMLEnumDecl::class.java)
        assertEquals("Expected 1 enum", 1, enums.size)

        val tables = PsiTreeUtil.findChildrenOfType(myFixture.file, DBMLTableDecl::class.java)
        assertEquals("Expected 2 tables", 2, tables.size)

        val refs = PsiTreeUtil.findChildrenOfType(myFixture.file, DBMLRefDecl::class.java)
        assertEquals("Expected 1 ref", 1, refs.size)
    }

    fun `test table with settings`() {
        myFixture.configureByText("test.dbml", """
            Table users [headercolor: #3498DB] {
              id int
            }
        """.trimIndent())

        val table = PsiTreeUtil.findChildOfType(myFixture.file, DBMLTableDecl::class.java)
        assertNotNull("Expected table declaration", table)
        assertTrue("Table should contain settings", table!!.text.contains("headercolor"))
    }

    fun `test column with default value`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              created_at datetime [default: `now()`]
            }
        """.trimIndent())

        val column = PsiTreeUtil.findChildOfType(myFixture.file, DBMLColumnDef::class.java)
        assertNotNull("Expected column definition", column)

        val attrList = PsiTreeUtil.findChildOfType(column, DBMLColumnAttrList::class.java)
        assertNotNull("Expected attribute list", attrList)
        assertTrue("Expected default attribute", attrList!!.text.contains("default:"))
    }
}
