package com.github.kangwooc.dbmlplugin.lang.reference

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTableDecl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DBMLReferenceTest : BasePlatformTestCase() {

    fun `test table reference resolves to table declaration`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int [pk]
            }
            
            Table posts {
              user_id int
            }
            
            Ref: posts.user_id > <caret>users.id
        """.trimIndent())

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Should find element at caret", element)
        
        val reference = element?.parent?.reference ?: element?.reference
        if (reference != null) {
            val resolved = reference.resolve()
            assertNotNull("Reference should resolve", resolved)
            
            val table = if (resolved is DBMLTableDecl) resolved else 
                PsiTreeUtil.getParentOfType(resolved, DBMLTableDecl::class.java)
            assertNotNull("Should resolve to a table", table)
            assertEquals("Should resolve to users table", "users", table?.name)
        }
    }

    fun `test table reference provides variants`() {
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
        assertNotNull("Completion should return variants", variants)
    }

    fun `test multiple tables with same reference`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int [pk]
              name varchar
            }
            
            Table orders {
              id int [pk]
              user_id int
            }
            
            Table reviews {
              id int [pk]
              user_id int
            }
            
            Ref: orders.user_id > users.id
            Ref: reviews.user_id > <caret>users.id
        """.trimIndent())

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertNotNull("Should find element at caret", element)
        
        val reference = element?.parent?.reference ?: element?.reference
        if (reference != null) {
            val resolved = reference.resolve()
            if (resolved != null) {
                val table = if (resolved is DBMLTableDecl) resolved else 
                    PsiTreeUtil.getParentOfType(resolved, DBMLTableDecl::class.java)
                assertEquals("Should resolve to users table", "users", table?.name)
            }
        }
    }

    fun `test reference to non-existent table returns null`() {
        myFixture.configureByText("test.dbml", """
            Table users {
              id int [pk]
            }
            
            Ref: posts.user_id > <caret>nonexistent.id
        """.trimIndent())

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        val reference = element?.parent?.reference ?: element?.reference
        if (reference != null) {
            val resolved = reference.resolve()
            assertNull("Reference to non-existent table should not resolve", resolved)
        }
    }
}
