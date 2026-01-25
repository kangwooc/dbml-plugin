package com.github.kangwooc.dbmlplugin.lang

import com.intellij.lang.Language

object DBMLLanguage : Language("DBML") {
    private fun readResolve(): Any = DBMLLanguage
}
