// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.parser;

import static com.intellij.dts.lang.psi.DtsTypes.*;
import static com.intellij.dts.lang.parser.DtsParserUtil.*;
import static com.intellij.lang.WhitespacesBinders.*;

@java.lang.SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class DtsParser implements com.intellij.lang.PsiParser, com.intellij.lang.LightPsiParser {

  public com.intellij.lang.ASTNode parse(com.intellij.psi.tree.IElementType root_, com.intellij.lang.PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(com.intellij.psi.tree.IElementType root_, com.intellij.lang.PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, EXTENDS_SETS_);
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(com.intellij.psi.tree.IElementType root_, com.intellij.lang.PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(com.intellij.psi.tree.IElementType root_, com.intellij.lang.PsiBuilder builder_, int level_) {
    return file(builder_, level_ + 1);
  }

  public static final com.intellij.psi.tree.TokenSet[] EXTENDS_SETS_ = new com.intellij.psi.tree.TokenSet[] {
    create_token_set_(ADD_EXPR, B_AND_EXPR, B_NOT_EXPR, B_OR_EXPR,
      CHAR_EXPR, DIV_EXPR, EQ_EXPR, EXPR,
      GEQ_EXPR, GRT_EXPR, INT_EXPR, LEQ_EXPR,
      LES_EXPR, LSH_EXPR, L_AND_EXPR, L_NOT_EXPR,
      L_OR_EXPR, MOD_EXPR, MUL_EXPR, NEG_EXPR,
      NEQ_EXPR, PAREN_EXPR, PP_MACRO_EXPR, RSH_EXPR,
      SUB_EXPR, TERNARY_EXPR, XOR_EXPR),
  };

  /* ********************************************************** */
  // BYTE_LITERAL
  public static boolean byte_$(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byte_$")) return false;
    if (!nextTokenIs(builder_, BYTE_LITERAL)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, BYTE_LITERAL);
    exit_section_(builder_, marker_, BYTE, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACKET byteArrayContent RBRACKET
  public static boolean byteArray(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArray")) return false;
    if (!nextTokenIs(builder_, LBRACKET)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, BYTE_ARRAY, null);
    result_ = consumeToken(builder_, LBRACKET);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, byteArrayContent(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACKET) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // byteArrayValue*
  static boolean byteArrayContent(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArrayContent")) return false;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_);
    while (true) {
      int pos_ = current_position_(builder_);
      if (!byteArrayValue(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "byteArrayContent", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, true, false, DtsParser::byteArrayRecover);
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | COMMA | RBRACKET | RPAREN)
  static boolean byteArrayRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArrayRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !byteArrayRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RBRACE | COMMA | RBRACKET | RPAREN
  private static boolean byteArrayRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArrayRecover_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, SEMICOLON);
    if (!result_) result_ = consumeTokenFast(builder_, RBRACE);
    if (!result_) result_ = consumeTokenFast(builder_, COMMA);
    if (!result_) result_ = consumeTokenFast(builder_, RBRACKET);
    if (!result_) result_ = consumeTokenFast(builder_, RPAREN);
    return result_;
  }

  /* ********************************************************** */
  // <<value (byte | ppMacro)>>
  static boolean byteArrayValue(com.intellij.lang.PsiBuilder builder_, int level_) {
    return value(builder_, level_ + 1, DtsParser::byteArrayValue_0_0);
  }

  // byte | ppMacro
  private static boolean byteArrayValue_0_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArrayValue_0_0")) return false;
    boolean result_;
    result_ = byte_$(builder_, level_ + 1);
    if (!result_) result_ = ppMacro(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // int | char | exprValue | ppMacro
  static boolean cell(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cell")) return false;
    boolean result_;
    result_ = int_$(builder_, level_ + 1);
    if (!result_) result_ = char_$(builder_, level_ + 1);
    if (!result_) result_ = exprValue(builder_, level_ + 1);
    if (!result_) result_ = ppMacro(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // (&BITS cellArrayBits)? LANGL cellArrayContent RANGL
  public static boolean cellArray(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArray")) return false;
    if (!nextTokenIs(builder_, "<cell array>", BITS, LANGL)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, CELL_ARRAY, "<cell array>");
    result_ = cellArray_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LANGL);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, cellArrayContent(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RANGL) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (&BITS cellArrayBits)?
  private static boolean cellArray_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArray_0")) return false;
    cellArray_0_0(builder_, level_ + 1);
    return true;
  }

  // &BITS cellArrayBits
  private static boolean cellArray_0_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArray_0_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = cellArray_0_0_0(builder_, level_ + 1);
    result_ = result_ && cellArrayBits(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // &BITS
  private static boolean cellArray_0_0_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArray_0_0_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _AND_);
    result_ = consumeToken(builder_, BITS);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // BITS (int | ppMacro)
  public static boolean cellArrayBits(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayBits")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, CELL_ARRAY_BITS, "<cell array bits>");
    result_ = consumeToken(builder_, BITS);
    pinned_ = result_; // pin = 1
    result_ = result_ && cellArrayBits_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, DtsParser::cellArrayBitsRecover);
    return result_ || pinned_;
  }

  // int | ppMacro
  private static boolean cellArrayBits_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayBits_1")) return false;
    boolean result_;
    result_ = int_$(builder_, level_ + 1);
    if (!result_) result_ = ppMacro(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | LANGL)
  static boolean cellArrayBitsRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayBitsRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !cellArrayBitsRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RBRACE | LANGL
  private static boolean cellArrayBitsRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayBitsRecover_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, SEMICOLON);
    if (!result_) result_ = consumeTokenFast(builder_, RBRACE);
    if (!result_) result_ = consumeTokenFast(builder_, LANGL);
    return result_;
  }

  /* ********************************************************** */
  // cellArrayValue*
  static boolean cellArrayContent(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayContent")) return false;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_);
    while (true) {
      int pos_ = current_position_(builder_);
      if (!cellArrayValue(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "cellArrayContent", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, true, false, DtsParser::cellArrayRecover);
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | COMMA | RANGL | RPAREN)
  static boolean cellArrayRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !cellArrayRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RBRACE | COMMA | RANGL | RPAREN
  private static boolean cellArrayRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayRecover_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, SEMICOLON);
    if (!result_) result_ = consumeTokenFast(builder_, RBRACE);
    if (!result_) result_ = consumeTokenFast(builder_, COMMA);
    if (!result_) result_ = consumeTokenFast(builder_, RANGL);
    if (!result_) result_ = consumeTokenFast(builder_, RPAREN);
    return result_;
  }

  /* ********************************************************** */
  // <<value (pHandle | cell)>>
  static boolean cellArrayValue(com.intellij.lang.PsiBuilder builder_, int level_) {
    return value(builder_, level_ + 1, DtsParser::cellArrayValue_0_0);
  }

  // pHandle | cell
  private static boolean cellArrayValue_0_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayValue_0_0")) return false;
    boolean result_;
    result_ = pHandle(builder_, level_ + 1);
    if (!result_) result_ = cell(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // CHAR_LITERAL
  public static boolean char_$(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "char_$")) return false;
    if (!nextTokenIs(builder_, CHAR_LITERAL)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CHAR_LITERAL);
    exit_section_(builder_, marker_, CHAR, result_);
    return result_;
  }

  /* ********************************************************** */
  static Parser compilerDirective_$(Parser directive, Parser arg) {
    return (builder_, level_) -> compilerDirective(builder_, level_ + 1, directive, arg);
  }

  // <<directive>> <<arg>>
  public static boolean compilerDirective(com.intellij.lang.PsiBuilder builder_, int level_, Parser directive, Parser arg) {
    if (!recursion_guard_(builder_, level_, "compilerDirective")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, COMPILER_DIRECTIVE, null);
    result_ = directive.parse(builder_, level_);
    pinned_ = result_; // pin = 1
    result_ = result_ && arg.parse(builder_, level_);
    exit_section_(builder_, level_, marker_, result_, pinned_, DtsParser::compilerDirectiveRecover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // <<entry <<compilerDirective <<directive>> <<arg>>>>>>
  static boolean compilerDirectiveEntry(com.intellij.lang.PsiBuilder builder_, int level_, Parser directive, Parser arg) {
    return entry(builder_, level_ + 1, compilerDirective_$(directive, arg));
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | afterLineBreak)
  static boolean compilerDirectiveRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "compilerDirectiveRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !compilerDirectiveRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RBRACE | afterLineBreak
  private static boolean compilerDirectiveRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "compilerDirectiveRecover_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenFast(builder_, SEMICOLON);
    if (!result_) result_ = consumeTokenFast(builder_, RBRACE);
    if (!result_) result_ = parseAfterLineBreak(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // <<entry property>> |
  //         <<entry subNode>> |
  //         <<entry refNode>> |
  //         <<entry rootNode>> |
  //         <<compilerDirectiveEntry V1 ()>> |
  //         <<compilerDirectiveEntry PLUGIN ()>> |
  //         <<compilerDirectiveEntry (LABEL* MEMRESERVE) (cell cell)>> |
  //         <<compilerDirectiveEntry DELETE_NODE (pHandle | NAME)>> |
  //         <<compilerDirectiveEntry DELETE_PROP NAME>> |
  //         <<compilerDirectiveEntry OMIT_NODE pHandle>> |
  //         invalidEntry
  static boolean entries(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entries")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = entry(builder_, level_ + 1, DtsParser::property);
    if (!result_) result_ = entry(builder_, level_ + 1, DtsParser::subNode);
    if (!result_) result_ = entry(builder_, level_ + 1, DtsParser::refNode);
    if (!result_) result_ = entry(builder_, level_ + 1, DtsParser::rootNode);
    if (!result_) result_ = compilerDirectiveEntry(builder_, level_ + 1, V1_parser_, DtsParser::entries_4_1);
    if (!result_) result_ = compilerDirectiveEntry(builder_, level_ + 1, PLUGIN_parser_, DtsParser::entries_5_1);
    if (!result_) result_ = compilerDirectiveEntry(builder_, level_ + 1, DtsParser::entries_6_0, DtsParser::entries_6_1);
    if (!result_) result_ = compilerDirectiveEntry(builder_, level_ + 1, DELETE_NODE_parser_, DtsParser::entries_7_1);
    if (!result_) result_ = compilerDirectiveEntry(builder_, level_ + 1, DELETE_PROP_parser_, NAME_parser_);
    if (!result_) result_ = compilerDirectiveEntry(builder_, level_ + 1, OMIT_NODE_parser_, DtsParser::pHandle);
    if (!result_) result_ = parseInvalidEntry(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ()
  private static boolean entries_4_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    return true;
  }

  // ()
  private static boolean entries_5_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    return true;
  }

  // LABEL* MEMRESERVE
  private static boolean entries_6_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entries_6_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = entries_6_0_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, MEMRESERVE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LABEL*
  private static boolean entries_6_0_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entries_6_0_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "entries_6_0_0", pos_)) break;
    }
    return true;
  }

  // cell cell
  private static boolean entries_6_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entries_6_1")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = cell(builder_, level_ + 1);
    result_ = result_ && cell(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // pHandle | NAME
  private static boolean entries_7_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "entries_7_1")) return false;
    boolean result_;
    result_ = pHandle(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, NAME);
    return result_;
  }

  /* ********************************************************** */
  // <<arg>> SEMICOLON
  public static boolean entry(com.intellij.lang.PsiBuilder builder_, int level_, Parser arg) {
    if (!recursion_guard_(builder_, level_, "entry")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, ENTRY, null);
    result_ = arg.parse(builder_, level_);
    pinned_ = result_; // pin = 1
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    register_hook_(builder_, RIGHT_BINDER, trailingCommentsBinder);
    register_hook_(builder_, LEFT_BINDER, leadingCommentsBinder);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // LPAREN expr RPAREN
  public static boolean exprValue(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "exprValue")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPR_VALUE, null);
    result_ = consumeToken(builder_, LPAREN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, expr(builder_, level_ + 1, -1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // entries*
  static boolean file(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "file")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!entries(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "file", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // () INCLUDE INCLUDE_PATH
  public static boolean includeStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "includeStatement")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, INCLUDE_STATEMENT, "<include statement>");
    result_ = includeStatement_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeTokens(builder_, -1, INCLUDE, INCLUDE_PATH));
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ()
  private static boolean includeStatement_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    return true;
  }

  /* ********************************************************** */
  // INT_LITERAL
  public static boolean int_$(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "int_$")) return false;
    if (!nextTokenIs(builder_, INT_LITERAL)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, INT_LITERAL);
    exit_section_(builder_, marker_, INT, result_);
    return result_;
  }

  /* ********************************************************** */
  // (!RBRACE entries)+
  public static boolean nodeContent(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nodeContent")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, NODE_CONTENT, "<node content>");
    result_ = nodeContent_0(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!nodeContent_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "nodeContent", pos_)) break;
    }
    register_hook_(builder_, RIGHT_BINDER, GREEDY_RIGHT_BINDER);
    register_hook_(builder_, LEFT_BINDER, GREEDY_LEFT_BINDER);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // !RBRACE entries
  private static boolean nodeContent_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nodeContent_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = nodeContent_0_0(builder_, level_ + 1);
    result_ = result_ && entries(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !RBRACE
  private static boolean nodeContent_0_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nodeContent_0_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !consumeToken(builder_, RBRACE);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // HANDLE (NAME | pHandlePath)
  public static boolean pHandle(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pHandle")) return false;
    if (!nextTokenIs(builder_, HANDLE)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, P_HANDLE, null);
    result_ = consumeToken(builder_, HANDLE);
    pinned_ = result_; // pin = 1
    result_ = result_ && pHandle_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // NAME | pHandlePath
  private static boolean pHandle_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pHandle_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, NAME);
    if (!result_) result_ = pHandlePath(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // LBRACE path RBRACE
  static boolean pHandlePath(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pHandlePath")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, path(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // PATH
  static boolean path(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "path")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, PATH);
    exit_section_(builder_, level_, marker_, result_, false, DtsParser::pathRecover);
    return result_;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RANGL | RBRACE | afterLineBreak)
  static boolean pathRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pathRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !pathRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RANGL | RBRACE | afterLineBreak
  private static boolean pathRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pathRecover_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenFast(builder_, SEMICOLON);
    if (!result_) result_ = consumeTokenFast(builder_, RANGL);
    if (!result_) result_ = consumeTokenFast(builder_, RBRACE);
    if (!result_) result_ = parseAfterLineBreak(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // PP_DEFINE PP_SYMBOL PP_DEFINE_VALUE?
  public static boolean ppDefineStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppDefineStatement")) return false;
    if (!nextTokenIs(builder_, PP_DEFINE)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_DEFINE_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, PP_DEFINE, PP_SYMBOL);
    pinned_ = result_; // pin = 1
    result_ = result_ && ppDefineStatement_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // PP_DEFINE_VALUE?
  private static boolean ppDefineStatement_2(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppDefineStatement_2")) return false;
    consumeToken(builder_, PP_DEFINE_VALUE);
    return true;
  }

  /* ********************************************************** */
  // PP_ELIF PP_EXPRESSION
  public static boolean ppElifStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppElifStatement")) return false;
    if (!nextTokenIs(builder_, PP_ELIF)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_ELIF_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, PP_ELIF, PP_EXPRESSION);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // PP_ELIFDEF PP_SYMBOL
  public static boolean ppElifdefStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppElifdefStatement")) return false;
    if (!nextTokenIs(builder_, PP_ELIFDEF)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_ELIFDEF_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, PP_ELIFDEF, PP_SYMBOL);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // PP_ELIFNDEF PP_SYMBOL
  public static boolean ppElifndefStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppElifndefStatement")) return false;
    if (!nextTokenIs(builder_, PP_ELIFNDEF)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_ELIFNDEF_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, PP_ELIFNDEF, PP_SYMBOL);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // PP_ELSE
  public static boolean ppElseStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppElseStatement")) return false;
    if (!nextTokenIs(builder_, PP_ELSE)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PP_ELSE);
    exit_section_(builder_, marker_, PP_ELSE_STATEMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // PP_ENDIF
  public static boolean ppEndifStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppEndifStatement")) return false;
    if (!nextTokenIs(builder_, PP_ENDIF)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PP_ENDIF);
    exit_section_(builder_, marker_, PP_ENDIF_STATEMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // PP_IF PP_EXPRESSION
  public static boolean ppIfStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppIfStatement")) return false;
    if (!nextTokenIs(builder_, PP_IF)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_IF_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, PP_IF, PP_EXPRESSION);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // PP_IFDEF PP_SYMBOL
  public static boolean ppIfdefStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppIfdefStatement")) return false;
    if (!nextTokenIs(builder_, PP_IFDEF)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_IFDEF_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, PP_IFDEF, PP_SYMBOL);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // PP_IFNDEF PP_SYMBOL
  public static boolean ppIfndefStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppIfndefStatement")) return false;
    if (!nextTokenIs(builder_, PP_IFNDEF)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_IFNDEF_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, PP_IFNDEF, PP_SYMBOL);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // PP_INCLUDE PP_INCLUDE_PATH
  public static boolean ppIncludeStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppIncludeStatement")) return false;
    if (!nextTokenIs(builder_, PP_INCLUDE)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_INCLUDE_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, PP_INCLUDE, PP_INCLUDE_PATH);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // parserPpMacro
  public static boolean ppMacro(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppMacro")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_MACRO, "<pp macro>");
    result_ = parsePpMacro(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // () (ppStatements) PP_STATEMENT_END
  static boolean ppStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppStatement")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = ppStatement_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, ppStatement_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, PP_STATEMENT_END) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ()
  private static boolean ppStatement_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    return true;
  }

  // (ppStatements)
  private static boolean ppStatement_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppStatement_1")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = ppStatements(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ppIncludeStatement |
  //     ppIfdefStatement |
  //     ppIfndefStatement |
  //     ppEndifStatement |
  //     ppDefineStatement |
  //     ppUndefStatement |
  //     ppIfStatement |
  //     ppElifStatement |
  //     ppElseStatement
  static boolean ppStatements(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppStatements")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = ppIncludeStatement(builder_, level_ + 1);
    if (!result_) result_ = ppIfdefStatement(builder_, level_ + 1);
    if (!result_) result_ = ppIfndefStatement(builder_, level_ + 1);
    if (!result_) result_ = ppEndifStatement(builder_, level_ + 1);
    if (!result_) result_ = ppDefineStatement(builder_, level_ + 1);
    if (!result_) result_ = ppUndefStatement(builder_, level_ + 1);
    if (!result_) result_ = ppIfStatement(builder_, level_ + 1);
    if (!result_) result_ = ppElifStatement(builder_, level_ + 1);
    if (!result_) result_ = ppElseStatement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, DtsParser::pp_statementRecover);
    return result_;
  }

  /* ********************************************************** */
  // PP_UNDEF PP_SYMBOL
  public static boolean ppUndefStatement(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppUndefStatement")) return false;
    if (!nextTokenIs(builder_, PP_UNDEF)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_UNDEF_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, PP_UNDEF, PP_SYMBOL);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // !(PP_STATEMENT_END)
  static boolean pp_statementRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pp_statementRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !pp_statementRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (PP_STATEMENT_END)
  private static boolean pp_statementRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pp_statementRecover_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenFast(builder_, PP_STATEMENT_END);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LABEL* propertyName propertyAssignment?
  public static boolean property(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PROPERTY, "<property>");
    result_ = property_0(builder_, level_ + 1);
    result_ = result_ && parsePropertyName(builder_, level_ + 1);
    result_ = result_ && property_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // LABEL*
  private static boolean property_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "property_0", pos_)) break;
    }
    return true;
  }

  // propertyAssignment?
  private static boolean property_2(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property_2")) return false;
    propertyAssignment(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // ASSIGN propertyContent
  static boolean propertyAssignment(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyAssignment")) return false;
    if (!nextTokenIs(builder_, ASSIGN)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, ASSIGN);
    pinned_ = result_; // pin = 1
    result_ = result_ && propertyContent(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // propertyValue (COMMA propertyValue)*
  public static boolean propertyContent(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyContent")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PROPERTY_CONTENT, "<property content>");
    result_ = propertyValue(builder_, level_ + 1);
    result_ = result_ && propertyContent_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, DtsParser::propertyRecover);
    return result_;
  }

  // (COMMA propertyValue)*
  private static boolean propertyContent_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyContent_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!propertyContent_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "propertyContent_1", pos_)) break;
    }
    return true;
  }

  // COMMA propertyValue
  private static boolean propertyContent_1_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyContent_1_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && propertyValue(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | NAME | afterLineBreak)
  static boolean propertyRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !propertyRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RBRACE | NAME | afterLineBreak
  private static boolean propertyRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyRecover_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenFast(builder_, SEMICOLON);
    if (!result_) result_ = consumeTokenFast(builder_, RBRACE);
    if (!result_) result_ = consumeTokenFast(builder_, NAME);
    if (!result_) result_ = parseAfterLineBreak(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // <<value (string | pHandle | cellArray | byteArray | ppMacro)>>
  static boolean propertyValue(com.intellij.lang.PsiBuilder builder_, int level_) {
    return value(builder_, level_ + 1, DtsParser::propertyValue_0_0);
  }

  // string | pHandle | cellArray | byteArray | ppMacro
  private static boolean propertyValue_0_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyValue_0_0")) return false;
    boolean result_;
    result_ = string(builder_, level_ + 1);
    if (!result_) result_ = pHandle(builder_, level_ + 1);
    if (!result_) result_ = cellArray(builder_, level_ + 1);
    if (!result_) result_ = byteArray(builder_, level_ + 1);
    if (!result_) result_ = ppMacro(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // LABEL* pHandle LBRACE nodeContent? RBRACE
  public static boolean refNode(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "refNode")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, REF_NODE, "<ref node>");
    result_ = refNode_0(builder_, level_ + 1);
    result_ = result_ && pHandle(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, refNode_3(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, DtsParser::rootNodeRecover);
    return result_ || pinned_;
  }

  // LABEL*
  private static boolean refNode_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "refNode_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "refNode_0", pos_)) break;
    }
    return true;
  }

  // nodeContent?
  private static boolean refNode_3(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "refNode_3")) return false;
    nodeContent(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // SLASH LBRACE nodeContent? RBRACE
  public static boolean rootNode(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rootNode")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, ROOT_NODE, "<root node>");
    result_ = consumeTokens(builder_, 2, SLASH, LBRACE);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, rootNode_2(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, DtsParser::rootNodeRecover);
    return result_ || pinned_;
  }

  // nodeContent?
  private static boolean rootNode_2(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rootNode_2")) return false;
    nodeContent(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | HANDLE | afterLineBreak)
  static boolean rootNodeRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rootNodeRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !rootNodeRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RBRACE | HANDLE | afterLineBreak
  private static boolean rootNodeRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rootNodeRecover_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenFast(builder_, SEMICOLON);
    if (!result_) result_ = consumeTokenFast(builder_, RBRACE);
    if (!result_) result_ = consumeTokenFast(builder_, HANDLE);
    if (!result_) result_ = parseAfterLineBreak(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // STRING_LITERAL
  public static boolean string(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string")) return false;
    if (!nextTokenIs(builder_, STRING_LITERAL)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, STRING_LITERAL);
    exit_section_(builder_, marker_, STRING, result_);
    return result_;
  }

  /* ********************************************************** */
  // LABEL* OMIT_NODE? LABEL* NAME LBRACE nodeContent? RBRACE
  public static boolean subNode(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "subNode")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, SUB_NODE, "<sub node>");
    result_ = subNode_0(builder_, level_ + 1);
    result_ = result_ && subNode_1(builder_, level_ + 1);
    result_ = result_ && subNode_2(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 2, NAME, LBRACE);
    pinned_ = result_; // pin = 5
    result_ = result_ && report_error_(builder_, subNode_5(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, DtsParser::subNodeRecover);
    return result_ || pinned_;
  }

  // LABEL*
  private static boolean subNode_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "subNode_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "subNode_0", pos_)) break;
    }
    return true;
  }

  // OMIT_NODE?
  private static boolean subNode_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "subNode_1")) return false;
    consumeToken(builder_, OMIT_NODE);
    return true;
  }

  // LABEL*
  private static boolean subNode_2(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "subNode_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "subNode_2", pos_)) break;
    }
    return true;
  }

  // nodeContent?
  private static boolean subNode_5(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "subNode_5")) return false;
    nodeContent(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | NAME | afterLineBreak)
  static boolean subNodeRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "subNodeRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !subNodeRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RBRACE | NAME | afterLineBreak
  private static boolean subNodeRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "subNodeRecover_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenFast(builder_, SEMICOLON);
    if (!result_) result_ = consumeTokenFast(builder_, RBRACE);
    if (!result_) result_ = consumeTokenFast(builder_, NAME);
    if (!result_) result_ = parseAfterLineBreak(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LABEL* <<arg>> trailingLabels?
  static boolean value(com.intellij.lang.PsiBuilder builder_, int level_, Parser arg) {
    if (!recursion_guard_(builder_, level_, "value")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = value_0(builder_, level_ + 1);
    result_ = result_ && arg.parse(builder_, level_);
    result_ = result_ && value_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LABEL*
  private static boolean value_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "value_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "value_0", pos_)) break;
    }
    return true;
  }

  // trailingLabels?
  private static boolean value_2(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "value_2")) return false;
    parseTrailingLabels(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // Expression root: expr
  // Operator priority table:
  // 0: ATOM(ppMacroExpr)
  // 1: BINARY(ternaryExpr)
  // 2: BINARY(lOrExpr)
  // 3: BINARY(lAndExpr)
  // 4: BINARY(bOrExpr)
  // 5: BINARY(xorExpr)
  // 6: BINARY(bAndExpr)
  // 7: BINARY(eqExpr) BINARY(neqExpr)
  // 8: BINARY(lesExpr) BINARY(grtExpr) BINARY(leqExpr) BINARY(geqExpr)
  // 9: BINARY(lshExpr) BINARY(rshExpr)
  // 10: BINARY(addExpr) BINARY(subExpr)
  // 11: BINARY(mulExpr) BINARY(divExpr) BINARY(modExpr)
  // 12: PREFIX(lNotExpr) PREFIX(bNotExpr) PREFIX(negExpr)
  // 13: ATOM(intExpr) ATOM(charExpr) PREFIX(parenExpr)
  public static boolean expr(com.intellij.lang.PsiBuilder builder_, int level_, int priority_) {
    if (!recursion_guard_(builder_, level_, "expr")) return false;
    addVariant(builder_, "<expr>");
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, "<expr>");
    result_ = ppMacroExpr(builder_, level_ + 1);
    if (!result_) result_ = negExpr(builder_, level_ + 1);
    if (!result_) result_ = lNotExpr(builder_, level_ + 1);
    if (!result_) result_ = bNotExpr(builder_, level_ + 1);
    if (!result_) result_ = intExpr(builder_, level_ + 1);
    if (!result_) result_ = charExpr(builder_, level_ + 1);
    if (!result_) result_ = parenExpr(builder_, level_ + 1);
    pinned_ = result_;
    result_ = result_ && expr_0(builder_, level_ + 1, priority_);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  public static boolean expr_0(com.intellij.lang.PsiBuilder builder_, int level_, int priority_) {
    if (!recursion_guard_(builder_, level_, "expr_0")) return false;
    boolean result_ = true;
    while (true) {
      com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
      if (priority_ < 1 && consumeTokenSmart(builder_, TERNARY)) {
        result_ = report_error_(builder_, expr(builder_, level_, 1));
        result_ = ternaryExpr_1(builder_, level_ + 1) && result_;
        exit_section_(builder_, level_, marker_, TERNARY_EXPR, result_, true, null);
      }
      else if (priority_ < 2 && consumeTokenSmart(builder_, L_OR)) {
        result_ = expr(builder_, level_, 2);
        exit_section_(builder_, level_, marker_, L_OR_EXPR, result_, true, null);
      }
      else if (priority_ < 3 && consumeTokenSmart(builder_, L_AND)) {
        result_ = expr(builder_, level_, 3);
        exit_section_(builder_, level_, marker_, L_AND_EXPR, result_, true, null);
      }
      else if (priority_ < 4 && consumeTokenSmart(builder_, OR)) {
        result_ = expr(builder_, level_, 4);
        exit_section_(builder_, level_, marker_, B_OR_EXPR, result_, true, null);
      }
      else if (priority_ < 5 && consumeTokenSmart(builder_, XOR)) {
        result_ = expr(builder_, level_, 5);
        exit_section_(builder_, level_, marker_, XOR_EXPR, result_, true, null);
      }
      else if (priority_ < 6 && consumeTokenSmart(builder_, AND)) {
        result_ = expr(builder_, level_, 6);
        exit_section_(builder_, level_, marker_, B_AND_EXPR, result_, true, null);
      }
      else if (priority_ < 7 && consumeTokenSmart(builder_, EQ)) {
        result_ = expr(builder_, level_, 7);
        exit_section_(builder_, level_, marker_, EQ_EXPR, result_, true, null);
      }
      else if (priority_ < 7 && consumeTokenSmart(builder_, NEQ)) {
        result_ = expr(builder_, level_, 7);
        exit_section_(builder_, level_, marker_, NEQ_EXPR, result_, true, null);
      }
      else if (priority_ < 8 && consumeTokenSmart(builder_, LES)) {
        result_ = expr(builder_, level_, 8);
        exit_section_(builder_, level_, marker_, LES_EXPR, result_, true, null);
      }
      else if (priority_ < 8 && consumeTokenSmart(builder_, GRT)) {
        result_ = expr(builder_, level_, 8);
        exit_section_(builder_, level_, marker_, GRT_EXPR, result_, true, null);
      }
      else if (priority_ < 8 && consumeTokenSmart(builder_, LEQ)) {
        result_ = expr(builder_, level_, 8);
        exit_section_(builder_, level_, marker_, LEQ_EXPR, result_, true, null);
      }
      else if (priority_ < 8 && consumeTokenSmart(builder_, GEQ)) {
        result_ = expr(builder_, level_, 8);
        exit_section_(builder_, level_, marker_, GEQ_EXPR, result_, true, null);
      }
      else if (priority_ < 9 && consumeTokenSmart(builder_, LSH)) {
        result_ = expr(builder_, level_, 9);
        exit_section_(builder_, level_, marker_, LSH_EXPR, result_, true, null);
      }
      else if (priority_ < 9 && consumeTokenSmart(builder_, RSH)) {
        result_ = expr(builder_, level_, 9);
        exit_section_(builder_, level_, marker_, RSH_EXPR, result_, true, null);
      }
      else if (priority_ < 10 && consumeTokenSmart(builder_, ADD)) {
        result_ = expr(builder_, level_, 10);
        exit_section_(builder_, level_, marker_, ADD_EXPR, result_, true, null);
      }
      else if (priority_ < 10 && consumeTokenSmart(builder_, SUB)) {
        result_ = expr(builder_, level_, 10);
        exit_section_(builder_, level_, marker_, SUB_EXPR, result_, true, null);
      }
      else if (priority_ < 11 && consumeTokenSmart(builder_, MUL)) {
        result_ = expr(builder_, level_, 11);
        exit_section_(builder_, level_, marker_, MUL_EXPR, result_, true, null);
      }
      else if (priority_ < 11 && consumeTokenSmart(builder_, DIV)) {
        result_ = expr(builder_, level_, 11);
        exit_section_(builder_, level_, marker_, DIV_EXPR, result_, true, null);
      }
      else if (priority_ < 11 && consumeTokenSmart(builder_, MOD)) {
        result_ = expr(builder_, level_, 11);
        exit_section_(builder_, level_, marker_, MOD_EXPR, result_, true, null);
      }
      else {
        exit_section_(builder_, level_, marker_, null, false, false, null);
        break;
      }
    }
    return result_;
  }

  // ppMacro
  public static boolean ppMacroExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppMacroExpr")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_MACRO_EXPR, "<pp macro expr>");
    result_ = ppMacro(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // COLON expr
  private static boolean ternaryExpr_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ternaryExpr_1")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COLON);
    result_ = result_ && expr(builder_, level_ + 1, -1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  public static boolean negExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "negExpr")) return false;
    if (!nextTokenIsSmart(builder_, SUB)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokenSmart(builder_, SUB);
    pinned_ = result_;
    result_ = pinned_ && expr(builder_, level_, 12);
    exit_section_(builder_, level_, marker_, NEG_EXPR, result_, pinned_, null);
    return result_ || pinned_;
  }

  public static boolean lNotExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "lNotExpr")) return false;
    if (!nextTokenIsSmart(builder_, L_NOT)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokenSmart(builder_, L_NOT);
    pinned_ = result_;
    result_ = pinned_ && expr(builder_, level_, 12);
    exit_section_(builder_, level_, marker_, L_NOT_EXPR, result_, pinned_, null);
    return result_ || pinned_;
  }

  public static boolean bNotExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bNotExpr")) return false;
    if (!nextTokenIsSmart(builder_, NOT)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokenSmart(builder_, NOT);
    pinned_ = result_;
    result_ = pinned_ && expr(builder_, level_, 12);
    exit_section_(builder_, level_, marker_, B_NOT_EXPR, result_, pinned_, null);
    return result_ || pinned_;
  }

  // int
  public static boolean intExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "intExpr")) return false;
    if (!nextTokenIsSmart(builder_, INT_LITERAL)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = int_$(builder_, level_ + 1);
    exit_section_(builder_, marker_, INT_EXPR, result_);
    return result_;
  }

  // char
  public static boolean charExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "charExpr")) return false;
    if (!nextTokenIsSmart(builder_, CHAR_LITERAL)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = char_$(builder_, level_ + 1);
    exit_section_(builder_, marker_, CHAR_EXPR, result_);
    return result_;
  }

  public static boolean parenExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenExpr")) return false;
    if (!nextTokenIsSmart(builder_, LPAREN)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokenSmart(builder_, LPAREN);
    pinned_ = result_;
    result_ = pinned_ && expr(builder_, level_, -1);
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RPAREN)) && result_;
    exit_section_(builder_, level_, marker_, PAREN_EXPR, result_, pinned_, null);
    return result_ || pinned_;
  }

  static final Parser DELETE_NODE_parser_ = (builder_, level_) -> consumeToken(builder_, DELETE_NODE);
  static final Parser DELETE_PROP_parser_ = (builder_, level_) -> consumeToken(builder_, DELETE_PROP);
  static final Parser NAME_parser_ = (builder_, level_) -> consumeToken(builder_, NAME);
  static final Parser OMIT_NODE_parser_ = (builder_, level_) -> consumeToken(builder_, OMIT_NODE);
  static final Parser PLUGIN_parser_ = (builder_, level_) -> consumeToken(builder_, PLUGIN);
  static final Parser V1_parser_ = (builder_, level_) -> consumeToken(builder_, V1);
}
