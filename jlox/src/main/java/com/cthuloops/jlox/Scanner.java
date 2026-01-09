package com.cthuloops.jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cthuloops.jlox.TokenType.AND;
import static com.cthuloops.jlox.TokenType.BANG;
import static com.cthuloops.jlox.TokenType.BANG_EQUAL;
import static com.cthuloops.jlox.TokenType.CLASS;
import static com.cthuloops.jlox.TokenType.COMMA;
import static com.cthuloops.jlox.TokenType.DOT;
import static com.cthuloops.jlox.TokenType.ELSE;
import static com.cthuloops.jlox.TokenType.EOF;
import static com.cthuloops.jlox.TokenType.EQUAL;
import static com.cthuloops.jlox.TokenType.EQUAL_EQUAL;
import static com.cthuloops.jlox.TokenType.FALSE;
import static com.cthuloops.jlox.TokenType.FOR;
import static com.cthuloops.jlox.TokenType.FUN;
import static com.cthuloops.jlox.TokenType.GREATER;
import static com.cthuloops.jlox.TokenType.GREATER_EQUAL;
import static com.cthuloops.jlox.TokenType.IDENTIFIER;
import static com.cthuloops.jlox.TokenType.IF;
import static com.cthuloops.jlox.TokenType.LEFT_BRACE;
import static com.cthuloops.jlox.TokenType.LEFT_PAREN;
import static com.cthuloops.jlox.TokenType.LESS;
import static com.cthuloops.jlox.TokenType.LESS_EQUAL;
import static com.cthuloops.jlox.TokenType.MINUS;
import static com.cthuloops.jlox.TokenType.NIL;
import static com.cthuloops.jlox.TokenType.NUMBER;
import static com.cthuloops.jlox.TokenType.OR;
import static com.cthuloops.jlox.TokenType.PLUS;
import static com.cthuloops.jlox.TokenType.PRINT;
import static com.cthuloops.jlox.TokenType.RETURN;
import static com.cthuloops.jlox.TokenType.RIGHT_BRACE;
import static com.cthuloops.jlox.TokenType.RIGHT_PAREN;
import static com.cthuloops.jlox.TokenType.SEMICOLON;
import static com.cthuloops.jlox.TokenType.SLASH;
import static com.cthuloops.jlox.TokenType.STAR;
import static com.cthuloops.jlox.TokenType.STRING;
import static com.cthuloops.jlox.TokenType.SUPER;
import static com.cthuloops.jlox.TokenType.THIS;
import static com.cthuloops.jlox.TokenType.TRUE;
import static com.cthuloops.jlox.TokenType.VAR;
import static com.cthuloops.jlox.TokenType.WHILE;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // One character lexemes.
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;

            // Potentially two character lexemes.
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(SLASH);
                }

            // Whitespace.
            case ' ':
            case '\r':
            case '\t':
                break;
            
            // Newline.
            case '\n':
                line++;
                break;

            // Literals.
            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }

                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) {
            type = IDENTIFIER;
        }
        addToken(type);
    }


    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }

        if (source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }

        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }

        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
