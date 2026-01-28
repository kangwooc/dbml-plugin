package com.github.kangwooc.dbmlplugin.lang

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

class DBMLLiveTemplatesProvider : DefaultLiveTemplatesProvider {
    override fun getDefaultLiveTemplateFiles(): Array<String> {
        return arrayOf("liveTemplates/DBML")
    }

    override fun getHiddenLiveTemplateFiles(): Array<String>? {
        return null
    }
}
