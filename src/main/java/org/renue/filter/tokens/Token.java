package org.renue.filter.tokens;

import org.renue.filter.tokens.types.TokenType;

public class Token {
    private TokenType type;
    private double doubleValue;
    private String stringValue;
    private int columnNumber;

    public Token(TokenType type) {
        this.type = type;
    }

    public Token(double value) {
        this.doubleValue = value;
        this.type = TokenType.DOUBLE_OPERAND;
    }

    public Token(int value) {
        this.columnNumber = value;
        this.type = TokenType.COLUMN;
    }

    public Token(String stringValue) {
        this.stringValue = stringValue;
        this.type = TokenType.STRING_OPERAND;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public TokenType getType() {
        return type;
    }
}
