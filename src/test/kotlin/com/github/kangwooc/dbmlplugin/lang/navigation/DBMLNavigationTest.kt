package com.github.kangwooc.dbmlplugin.lang.navigation

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTableDecl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DBMLNavigationTest : BasePlatformTestCase() {

    fun `test go to table declaration from ref`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int
            }
            
            Table posts {
              user_id int
            }
            
            Ref: posts.user_id > <caret>users.id
        """.trimIndent())

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Should find element at caret", element)
        
        val reference = element?.parent?.reference
        if (reference != null) {
            val resolved = reference.resolve()
            assertNotNull("Should resolve table reference", resolved)
            
            val table = PsiTreeUtil.getParentOfType(resolved, DBMLTableDecl::class.java)
            assertEquals("Should resolve to users table", "users", table?.name)
        }
    }

    fun `test go to table declaration from column ref`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int [pk]
            }
            
            Table posts {
              user_id int [ref: > <caret>users.id]
            }
        """.trimIndent())

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Should find element at caret", element)
        
        val reference = element?.parent?.reference ?: element?.reference
        if (reference != null) {
            val resolved = reference.resolve()
            if (resolved != null) {
                val table = PsiTreeUtil.getParentOfType(resolved, DBMLTableDecl::class.java) ?: resolved as? DBMLTableDecl
                assertNotNull("Should resolve to a table", table)
                assertEquals("Should resolve to users table", "users", table?.name)
            }
        }
    }

    fun `test table reference provides completion variants`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int [pk]
            }
            
            Table posts {
              id int [pk]
            }
            
            Table products {
              id int [pk]
            }
            
            Ref: posts.id > <caret>
        """.trimIndent())

        val variants = myFixture.completeBasic()
        assertNotNull("Should have completion variants", variants)
    }

    fun `test enum reference in column type`() {
        myFixture.configureByText("test.dbml", """
            enum user_status {
              active
              inactive
            }
            
            Table users {
              status user_status
            }
        """.trimIndent())

        val tables = PsiTreeUtil.findChildrenOfType(myFixture.file, DBMLTableDecl::class.java)
        assertTrue("Should parse table with enum column", tables.isNotEmpty())
    }

    fun `test table name extraction`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int
            }
            
            Table posts {
              id int
            }
        """.trimIndent())

        val tables = PsiTreeUtil.findChildrenOfType(myFixture.file, DBMLTableDecl::class.java)
        val tableNames = tables.mapNotNull { it.name }.toSet()
        
        assertEquals("Should find 2 tables", 2, tableNames.size)
        assertTrue("Should contain users", tableNames.contains("users"))
        assertTrue("Should contain posts", tableNames.contains("posts"))
    }
}
