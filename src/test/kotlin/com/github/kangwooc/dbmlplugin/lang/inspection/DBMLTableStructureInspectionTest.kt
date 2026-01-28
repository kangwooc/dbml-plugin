package com.github.kangwooc.dbmlplugin.lang.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DBMLTableStructureInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(DBMLTableStructureInspection())
    }

    fun `test reports bare index entry`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                table users {
                  index email
                }
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        val summary = highlights.joinToString(prefix = "found: ", separator = ", ") { info ->
            "${'$'}{info.description}:${'$'}{info.text}"
        }
        assertTrue(summary, highlights.any { it.description?.contains("Wrap index") == true })
    }

    fun `test lexer classifies index as keyword`() {
        val sample = """
            table users {
              index email
            }
        """.trimIndent()

        val lexer = com.github.kangwooc.dbmlplugin.lang.highlighting.DBMLLexer()
        lexer.start(sample, 0, sample.length, 0)

        val tokens = mutableListOf<Pair<String?, String>>()
        while (true) {
            val type = lexer.tokenType ?: break
            val text = sample.substring(lexer.tokenStart, lexer.tokenEnd)
            tokens += type.toString() to text
            lexer.advance()
        }

        val hasKeywordIndex = tokens.any { it.first == "KEYWORD" && it.second.equals("index", ignoreCase = true) }
        assertTrue(tokens.joinToString { "${'$'}{it.first}:${'$'}{it.second}" }, hasKeywordIndex)
    }

    fun `test reports out of order sections`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                table users {
                  primary key {
                    columns: [id]
                  }
                  indexes {
                    index email
                  }
                }
            """.trimIndent()
        )

        val highlights = myFixture.doHighlighting()
        assertTrue(
            "Expected warning about section ordering",
            highlights.any { it.description?.contains("must appear before") == true }
        )
    }

    fun `test allows valid ordering`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                table users {
                  indexes {
                    index email
                  }
                  primary key {
                    columns: [id]
                  }
                  note {
                    text
                  }
                }
            """.trimIndent()
        )

        myFixture.checkHighlighting()
    }
}
