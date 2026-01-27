package com.github.kangwooc.dbmlplugin.lang.completion

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DBMLCompletionContributorTest : BasePlatformTestCase() {

    fun `test completes table keyword`() {
        myFixture.configureByText("sample.dbml", "ta<caret>")

        val variants = completeLookupStrings()

        assertContainsElements(variants, "table")
    }

    fun `test offers reference related keywords`() {
        myFixture.configureByText("sample.dbml", "re<caret>")

        val variants = completeLookupStrings()

        assertTrue(variants.containsAll(listOf("ref", "references")))
    }

    fun `test suggests column types inside table body`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                table users {
                  <caret>
                }
            """.trimIndent()
        )

        val variants = completeLookupStrings()

        assertContainsElements(variants, listOf("int", "varchar", "uuid"))
    }

    fun `test suggests attribute keywords inside brackets`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                table users {
                  id int [<caret>]
                }
            """.trimIndent()
        )

        val variants = completeLookupStrings()

        assertTrue(variants.containsAll(listOf("pk", "unique", "default: ")))
    }

    fun `test suggests index keywords inside indexes block`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                table users {
                  indexes {
                    <caret>
                  }
                }
            """.trimIndent()
        )

        val variants = completeLookupStrings()

        assertContainsElements(variants, listOf("name: ", "type: ", "index"))
        assertFalse("Column types should not appear in indexes", variants.contains("int"))
    }

    fun `test suggests index attribute keywords inside brackets`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                table users {
                  indexes {
                    (email) [<caret>]
                  }
                }
            """.trimIndent()
        )

        val variants = completeLookupStrings()

        assertTrue(variants.containsAll(listOf("name: ", "type: ", "unique")))
        assertFalse("Table-only attributes should not leak into index brackets", variants.contains("default: "))
    }

    fun `test suggests primary key keywords`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                table users {
                  primary key {
                    <caret>
                  }
                }
            """.trimIndent()
        )

        val variants = completeLookupStrings()

        assertTrue(variants.containsAll(listOf("columns: []", "name: ", "clustered")))
        assertFalse("Column types should not appear in primary key block", variants.contains("int"))
        assertFalse("Index-only tokens should stay out of primary key block", variants.contains("("))
    }

    fun `test does not suggest column types inside enum`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                enum status {
                  <caret>
                }
            """.trimIndent()
        )

        val variants = completeLookupStrings()

        assertFalse("Expected no column type suggestions in enum", variants.contains("int"))
        assertFalse("Expected no column type suggestions in enum", variants.contains("varchar"))
    }

    fun `test does not suggest table attributes in enum brackets`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                enum status {
                  value [<caret>]
                }
            """.trimIndent()
        )

        val variants = completeLookupStrings()

        assertFalse("Attribute completions should be limited to table columns", variants.contains("default: "))
    }

    fun `test suggests note helpers inside note block`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                table users {
                  note {
                    <caret>
                  }
                }
            """.trimIndent()
        )

        val variants = completeLookupStrings()

        assertTrue(variants.contains("TODO: "))
        assertFalse("Column completions should not appear in note block", variants.contains("int"))
    }

    fun `test suggests table group keywords`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                TableGroup People {
                  <caret>
                }
            """.trimIndent()
        )

        val variants = completeLookupStrings()

        assertTrue(variants.containsAll(listOf("table ", "color: ")))
        assertFalse("Column completions should not appear in table group context", variants.contains("int"))
    }

    fun `test completes note section with braces`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                table users {
                  no<caret>
                }
            """.trimIndent()
        )

        val variants = myFixture.completeBasic()
        val noteItem = variants?.firstOrNull { element ->
            if (element.lookupString != "note") return@firstOrNull false
            val presentation = LookupElementPresentation()
            element.renderElement(presentation)
            presentation.typeText == "table keyword"
        }
            ?: error("Expected note completion to be available")

        myFixture.lookup.currentItem = noteItem
        myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)

        val actual = insertCaretMarker(myFixture.editor.document.charsSequence, myFixture.caretOffset)
        assertEquals(
            """
                table users {
                  note {<caret>}
                }
            """.trimIndent(),
            actual
        )
    }

    fun `test table completion leaves caret after space`() {
        myFixture.configureByText("sample.dbml", "ta<caret>")

        val variants = myFixture.completeBasic()
        val tableItem = variants?.firstOrNull { it.lookupString == "table" }
            ?: error("Expected table completion to be available")

        myFixture.lookup.currentItem = tableItem
        myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)

        val actual = insertCaretMarker(myFixture.editor.document.charsSequence, myFixture.caretOffset)
        assertEquals("table <caret>", actual)
    }

    private fun insertCaretMarker(text: CharSequence, caretOffset: Int): String {
        return buildString(text.length + 7) {
            append(text.subSequence(0, caretOffset))
            append("<caret>")
            append(text.subSequence(caretOffset, text.length))
        }
    }

    private fun completeLookupStrings(): List<String> =
        myFixture.completeBasic()
            ?.map(LookupElement::getLookupString)
            ?: emptyList()
}
