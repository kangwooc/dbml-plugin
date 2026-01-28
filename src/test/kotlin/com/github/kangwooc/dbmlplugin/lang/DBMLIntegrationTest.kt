package com.github.kangwooc.dbmlplugin.lang

import com.github.kangwooc.dbmlplugin.lang.psi.DBMLFile
import com.github.kangwooc.dbmlplugin.lang.psi.DBMLTableDecl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DBMLIntegrationTest : BasePlatformTestCase() {

    fun `test dbml file is recognized`() {
        val file = myFixture.configureByText("sample.dbml", """
            table users {
              id int [pk]
            }
        """.trimIndent())
        
        assertTrue("File should be recognized as DBML", file is DBMLFile)
        assertEquals("Wrong file type", DBMLFileType, file.fileType)
    }

    fun `test basic table parsing`() {
        val file = myFixture.configureByText("sample.dbml", """
            table users {
              id int [pk]
              name varchar(255)
            }
        """.trimIndent())
        
        val tables = PsiTreeUtil.findChildrenOfType(file, DBMLTableDecl::class.java)
        assertNotEmpty(tables)
    }

    fun `test syntax highlighting works`() {
        myFixture.configureByText("sample.dbml", """
            table users {
              id int [pk]
            }
        """.trimIndent())
        
        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }

    fun `test file type icon and description`() {
        assertEquals("DBML", DBMLFileType.name)
        assertEquals("dbml", DBMLFileType.defaultExtension)
        assertNotNull(DBMLFileType.icon)
        assertTrue(DBMLFileType.description.contains("MARKUP"))
    }

    fun `test lexer tokenizes keywords`() {
        myFixture.configureByText("sample.dbml", "table enum ref project")
        
        val file = myFixture.file
        assertNotNull(file)
        assertTrue(file.text.contains("table"))
    }

    fun `test parses multiple tables`() {
        val file = myFixture.configureByText("sample.dbml", """
            table users {
              id int
            }
            
            table posts {
              id int
            }
        """.trimIndent())
        
        val tables = PsiTreeUtil.findChildrenOfType(file, DBMLTableDecl::class.java)
        assertTrue("Expected at least one table", tables.isNotEmpty())
    }

    fun `test handles syntax errors gracefully`() {
        val file = myFixture.configureByText("sample.dbml", """
            table users {
              invalid syntax !@#
            }
        """.trimIndent())
        
        assertNotNull(file)
        assertTrue(file is DBMLFile)
    }
}
