package com.github.kangwooc.dbmlplugin.lang.annotator

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DBMLColorAnnotatorTest : BasePlatformTestCase() {

    fun `test detects hex color in headercolor`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                Table users [headercolor: <caret>#3498DB] {
                  id int
                }
            """.trimIndent()
        )

        val markers = myFixture.findAllGutters()
        assertTrue("Expected gutter icon for color", markers.isNotEmpty())
    }

    fun `test detects short hex color`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                TableGroup e_commerce [color: <caret>#345] {
                  merchants
                }
            """.trimIndent()
        )

        val markers = myFixture.findAllGutters()
        assertTrue("Expected gutter icon for short hex color", markers.isNotEmpty())
    }

    fun `test detects color in relationship`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                Ref: products.id > users.id [color: <caret>#79AD51]
            """.trimIndent()
        )

        val markers = myFixture.findAllGutters()
        assertTrue("Expected gutter icon for relationship color", markers.isNotEmpty())
    }

    fun `test ignores invalid color codes`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                Table users [headercolor: <caret>#ZZZ] {
                  id int
                }
            """.trimIndent()
        )

        val markers = myFixture.findAllGutters()
        assertTrue("Should not show gutter for invalid color", markers.isEmpty())
    }

    fun `test ignores non-color contexts`() {
        myFixture.configureByText(
            "sample.dbml",
            """
                Table users {
                  id int [note: '#3498DB is a color']
                }
            """.trimIndent()
        )

        val markers = myFixture.findAllGutters()
        assertTrue("Should not show gutter for color in note", markers.isEmpty())
    }
}
