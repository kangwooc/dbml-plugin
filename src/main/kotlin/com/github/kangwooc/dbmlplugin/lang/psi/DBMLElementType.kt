package com.github.kangwooc.dbmlplugin.lang.psi

import com.github.kangwooc.dbmlplugin.lang.DBMLLanguage
import com.intellij.psi.tree.IElementType

class DBMLElementType(debugName: String) : IElementType(debugName, DBMLLanguage)

object DBMLElementTypes {
    val TABLE_DECL = DBMLElementType("TABLE_DECL")
    val TABLE_BODY = DBMLElementType("TABLE_BODY")
    val COLUMN_DEF = DBMLElementType("COLUMN_DEF")
    val COLUMN_NAME = DBMLElementType("COLUMN_NAME")
    val COLUMN_TYPE = DBMLElementType("COLUMN_TYPE")
    val COLUMN_ATTR_LIST = DBMLElementType("COLUMN_ATTR_LIST")
    val ENUM_DECL = DBMLElementType("ENUM_DECL")
    val REF_DECL = DBMLElementType("REF_DECL")
    val PROJECT_DECL = DBMLElementType("PROJECT_DECL")
    val TABLEGROUP_DECL = DBMLElementType("TABLEGROUP_DECL")
    val NOTE_DECL = DBMLElementType("NOTE_DECL")
    val NOTE_BLOCK = DBMLElementType("NOTE_BLOCK")
    val INDEXES_BLOCK = DBMLElementType("INDEXES_BLOCK")
    val INDEX_DEF = DBMLElementType("INDEX_DEF")
    val PRIMARY_KEY_BLOCK = DBMLElementType("PRIMARY_KEY_BLOCK")
    val UNKNOWN_STMT = DBMLElementType("UNKNOWN_STMT")
}
