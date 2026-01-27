<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# dbml-plugin Changelog

## [0.0.1]
- Register DBML parser definition to create DBML PSI files.
- Add initial DBML PSI parser for top-level declarations.
- Parse table bodies into column/index/note/primary key PSI blocks.
- Split table columns into name/type/attribute PSI nodes and use PSI in completion/inspection when available.
- DBML file type registration with syntax highlighting and keyword-aware lexer.
- Context-aware DBML code completion for tables, indexes, notes, and table groups.
- Table structure inspection enforcing indexes/primary key/note section ordering.
- DBML cheat sheet documentation.
