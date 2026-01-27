package com.github.kangwooc.dbmlplugin.lang

/**
 * Central place for DBML language keywords so lexer and code insight features stay in sync.
 */
object DBMLKeywords {
    val ALL: Set<String> = linkedSetOf(
        "project",
        "table",
        "tablegroup",
        "tablepartial",
        "indexes",
        "index",
        "ref",
        "references",
        "note",
        "notes",
        "enum",
        "primary",
        "key",
        "pk",
        "unique",
        "increment",
        "not",
        "null",
        "default",
        "name",
        "type",
        "color",
        "headercolor",
        "database_type",
        "now",
        "on",
        "update",
        "delete",
        "cascade",
        "restrict",
        "set",
        "no",
        "action",
        "as",
        "true",
        "false"
    )
}
