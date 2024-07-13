package com.intellij.dts.lang.lexer;

import java.util.Stack;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;

import com.intellij.dts.lang.psi.DtsTypes;

%%

%{
    private final Stack<Integer> stack = new Stack<>();

    private void pushState(int state) {
        stack.push(yystate());
        yybegin(state);
    }

    private void popState() {
        assert !stack.empty();
        yybegin(stack.pop());
    }

    private void resetState() {
        stack.clear();
        yybegin(YYINITIAL);
    }
%}

%{
    private int parenCount = 0;

    private void beginExpr() {
        parenCount = 0;
        pushState(WAITING_EXPR);
    }

    private void openParen() {
        parenCount++;
    }

    private void closeParen() {
        if (parenCount == 0) {
            popState();
        } else {
            parenCount--;
        }
    }
%}

%class DtsLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

EOL               = "\n" | "\r" | "\r\n"
EOL_ESC           = "\\"{LINE_WS}*{EOL}
LINE_WS           = [\ \t]

PP_DIRECTIVES    = "include" | "if" | "ifdef" | "ifndef" | "else" | "elif" | "endif" | "define" | "undef"

// node or property names
NAME              = [a-zA-Z0-9,._+*#?@-]+
PATH              = "/" | (("/"{NAME})+"/"?)

INCLUDE_PATH      = \"[^\"\n]*\"?

// c like identifier for macro names
IDENTIFIER        = [a-zA-Z_][0-9a-zA-Z_]*

BYTE              = [a-fA-F0-9]{2}
INT               = ([0-9]+|0[xX][0-9a-fA-F]+)(U|L|UL|LL|ULL)?
STRING            = \"[^\"]*\"?
CHAR              = \'([^']|(\\((x[0-9a-fA-F]{1,2})|([0-7][0-8]{0,2})|[^x0-7])))?\'?

COMMENT_EOL       = "//"[^\r\n]*
COMMENT_C         = "/*"([^*]|"*"[^/])*"*/"

%state WAITING_CELL WAITING_BYTE WAITING_VALUE WAITING_BITS WAITING_EXPR WAITING_HANDLE WAITING_INCLUDE

%%

{COMMENT_C}                                         { return DtsTypes.COMMENT_C; }
{COMMENT_EOL}                                       { return DtsTypes.COMMENT_EOL; }

// c prerpocssor statments
"#"{LINE_WS}*{PP_DIRECTIVES} (.+ | {EOL_ESC})*     { return DtsTypes.PP_STATEMENT_MARKER; }

"/dts-v1/"                                          { resetState(); return DtsTypes.V1; }
"/plugin/"                                          { resetState(); return DtsTypes.PLUGIN; }
"/include/"                                         { pushState(WAITING_INCLUDE); return DtsTypes.INCLUDE; }
"/memreserve/"                                      { resetState(); pushState(WAITING_CELL); return DtsTypes.MEMRESERVE; }
"/delete-node/"                                     { resetState(); return DtsTypes.DELETE_NODE; }
"/delete-property/"                                 { resetState(); return DtsTypes.DELETE_PROP; }
"/omit-if-no-ref/"                                  { resetState(); return DtsTypes.OMIT_NODE; }

<YYINITIAL> {
    ";"                                             { return DtsTypes.SEMICOLON; }

    "="                                             { pushState(WAITING_VALUE); return DtsTypes.ASSIGN; }

    {NAME}                                          { return DtsTypes.NAME; }
}

<YYINITIAL, WAITING_VALUE> {
    "/bits/"                                        { pushState(WAITING_BITS); return DtsTypes.BITS; }

    ","                                             { return DtsTypes.COMMA; }
    "/"                                             { return DtsTypes.SLASH; }
    "&"                                             { pushState(WAITING_HANDLE); return DtsTypes.HANDLE; }
    "{"                                             { return DtsTypes.LBRACE; }
    "}"                                             { return DtsTypes.RBRACE; }
    "("                                             { return DtsTypes.LPAREN; }
    ")"                                             { return DtsTypes.RPAREN; }
    "["                                             { pushState(WAITING_BYTE); return DtsTypes.LBRACKET; }
    "]"                                             { return DtsTypes.RBRACKET; }
    "<"                                             { pushState(WAITING_CELL); return DtsTypes.LANGL; }
    ">"                                             { return DtsTypes.RANGL; }

    {STRING}                                        { return DtsTypes.STRING_LITERAL; }

    {NAME}":"                                       { return DtsTypes.LABEL; }
    {IDENTIFIER}                                    { return DtsTypes.NAME; }
}

<WAITING_INCLUDE> {
    {INCLUDE_PATH}                                  { return DtsTypes.INCLUDE_PATH; }
}

<WAITING_BITS> {
    {INT}                                           { popState(); return DtsTypes.INT_LITERAL; }
}

<WAITING_HANDLE> {
    "{"                                             { return DtsTypes.LBRACE; }
    "}"                                             { popState(); return DtsTypes.RBRACE; }

    {IDENTIFIER}                                    { popState(); return DtsTypes.NAME; }
    {PATH}                                          { return DtsTypes.PATH; }

    {LINE_WS}                                       { return TokenType.WHITE_SPACE; }

    [^]                                             { popState(); yypushback(1); }
}

<WAITING_CELL> {
    "("                                             { beginExpr(); return DtsTypes.LPAREN; }
    ">"                                             { popState(); return DtsTypes.RANGL; }

    "&"                                             { pushState(WAITING_HANDLE); return DtsTypes.HANDLE; }

    {CHAR}                                          { return DtsTypes.CHAR_LITERAL; }
    {INT}                                           { return DtsTypes.INT_LITERAL; }
    {NAME}":"                                       { return DtsTypes.LABEL; }
    {IDENTIFIER}                                    { return DtsTypes.NAME; }
}

<WAITING_BYTE> {
    "]"                                             { popState(); return DtsTypes.RBRACKET; }

    {BYTE}                                          { return DtsTypes.BYTE_LITERAL; }
    {NAME}":"                                       { return DtsTypes.LABEL; }
    {IDENTIFIER}                                    { return DtsTypes.NAME; }
}

<WAITING_EXPR> {
    "+"                                             { return DtsTypes.ADD; }
    "-"                                             { return DtsTypes.SUB; }
    "*"                                             { return DtsTypes.MUL; }
    "/"                                             { return DtsTypes.DIV; }
    "%"                                             { return DtsTypes.MOD; }

    "&"                                             { return DtsTypes.AND; }
    "|"                                             { return DtsTypes.OR; }
    "^"                                             { return DtsTypes.XOR; }
    "~"                                             { return DtsTypes.NOT; }
    "<<"                                            { return DtsTypes.LSH; }
    ">>"                                            { return DtsTypes.RSH; }

    "&&"                                            { return DtsTypes.L_AND; }
    "||"                                            { return DtsTypes.L_OR; }
    "!"                                             { return DtsTypes.L_NOT; }

    "<"                                             { return DtsTypes.LES; }
    ">"                                             { return DtsTypes.GRT; }
    "<="                                            { return DtsTypes.LEQ; }
    ">="                                            { return DtsTypes.GEQ; }
    "=="                                            { return DtsTypes.EQ; }
    "!="                                            { return DtsTypes.NEQ; }

    ":"                                             { return DtsTypes.COLON; }
    "?"                                             { return DtsTypes.TERNARY; }

    ","                                             { return DtsTypes.COMMA; }

    {INT}                                           { return DtsTypes.INT_LITERAL; }
    {IDENTIFIER}                                    { return DtsTypes.NAME; }
    {CHAR}                                          { return DtsTypes.CHAR_LITERAL; }

    "("                                             { openParen(); return DtsTypes.LPAREN; }
    ")"                                             { closeParen(); return DtsTypes.RPAREN; }
}

({LINE_WS} | {EOL})+                                { return TokenType.WHITE_SPACE; }

[^] {
    if (yystate() != YYINITIAL) {
      resetState();
      yypushback(1);
    } else {
      return TokenType.BAD_CHARACTER;
    }
}