package com.github.kangwooc.dbmlplugin.lang.psi

import com.github.kangwooc.dbmlplugin.lang.DBMLLanguage
import com.intellij.psi.tree.IElementType

class DBMLTokenType(debugName: String) : IElementType(debugName, DBMLLanguage)

object DBMLTokenTypes {
    val KEYWORD = DBMLTokenType("KEYWORD")
    val IDENTIFIER = DBMLTokenType("IDENTIFIER")
    val STRING = DBMLTokenType("STRING")
    val NUMBER = DBMLTokenType("NUMBER")
    val COMMENT = DBMLTokenType("COMMENT")
    val BRACE = DBMLTokenType("BRACE")
    val BRACKET = DBMLTokenType("BRACKET")
    val PAREN = DBMLTokenType("PAREN")
    val COMMA = DBMLTokenType("COMMA")
    val DOT = DBMLTokenType("DOT")
    val SEMICOLON = DBMLTokenType("SEMICOLON")
    val OPERATOR = DBMLTokenType("OPERATOR")
}
