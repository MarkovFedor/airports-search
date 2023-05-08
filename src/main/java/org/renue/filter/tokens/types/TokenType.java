package org.renue.filter.tokens.types;

public enum TokenType {
    LESS,
    MORE,
    EQUALS,
    NOT_EQUALS,
    AND,
    OR,
    LEFT_BRACKET,
    RIGHT_BRACKET,
    COLUMN,
    DOUBLE_OPERAND,
    STRING_OPERAND,
    TRUE(true),
    FALSE(false);

    private boolean value;

    TokenType() {

    }
    TokenType(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }
}
