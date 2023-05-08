package org.renue.filter;

import org.renue.exceptions.FilterFormatException;
import org.renue.exceptions.IncorrectFilterQuery;
import org.renue.filter.tokens.*;
import org.renue.filter.tokens.types.TokenType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isDigit;

public class Filtrator {
    private LinkedList<Token> parsedTokens;
    private Stack<Token> stack;
    public Filtrator() {
        parsedTokens = new LinkedList<Token>();
    }

    public void parse(String query) throws FilterFormatException {
        int i = 0;
        char current;
        String numString;
        while(i < query.length()) {
            numString = "";
            current = query.charAt(i);
            if(current == '\'' || current == (char)8217) {
                i++;
                while (i<query.length() && query.charAt(i) != '\'')
                {
                    numString=numString+ query.charAt(i);
                    i++;
                }

                parsedTokens.addLast(new Token(numString));
                i++;
                continue;
            }
            if(isAlphabetic(current)) {
                i++;
                while(query.charAt(i)!='[')
                {
                    i++;
                }
                i++;
                while(i < query.length() && query.charAt(i)!=']')
                {
                    numString = numString + query.charAt(i);
                    i++;
                }
                i++;
                parsedTokens.addLast(new Token(Integer.parseInt(numString)-1));
                continue;
            }
            if(isDigit(current)) {
                numString = numString + current;
                i++;
                while (i < query.length() && isDigit(query.charAt(i))) {
                    numString = numString + Character.toString(query.charAt(i));
                    i++;
                }
                i--;

                parsedTokens.addLast(new Token(Double.parseDouble(numString)));
                i++;
                continue;
            }
            else if(current == '>') {
                parsedTokens.addLast(new Token(TokenType.MORE));
                i++;
                continue;
            }
            else if(current == '=') {
                parsedTokens.addLast(new Token(TokenType.EQUALS));
                i++;
                continue;
            }
            else if(current == '&') {
                parsedTokens.addLast(new Token(TokenType.AND));
                i++;
                continue;
            }
            else if(current == '|') {
                if(query.charAt(i++) != '|') {
                    throw new FilterFormatException("Неправильный формат строки с фильтрами: для указания ИЛИ используйте \"||\"");
                }
                parsedTokens.addLast(new Token(TokenType.OR));
                i++;
                continue;
            }
            else if(current == '<') {
                if(query.charAt(++i) == '>') {
                    parsedTokens.addLast(new Token(TokenType.NOT_EQUALS));
                } else {
                    parsedTokens.addLast(new Token(TokenType.LESS));
                    i--;
                }
                i++;
                continue;
            }
            else if(current == '(') {
                parsedTokens.addLast(new Token(TokenType.LEFT_BRACKET));
                i++;
                continue;
            }
            else if(current == ')') {
                parsedTokens.addLast(new Token(TokenType.RIGHT_BRACKET));
                i++;
                continue;
            }
        }
    }

    public ArrayList<String[]> filter(ArrayList<String[]> lines) throws IncorrectFilterQuery {
        ArrayList<String[]> filtered = new ArrayList<>();
        for(String[] query: lines) {
            stack = new Stack<>();
            int i = 0;
            while(i < parsedTokens.size()) {
                Token current = parsedTokens.get(i);
                if(current.getType() == TokenType.LEFT_BRACKET) pushLeftBracketInStack(current);
                if(current.getType() == TokenType.COLUMN) {
                    Token operator = parsedTokens.get(++i);
                    Token comparator = parsedTokens.get(++i);
                    String queryChart = query[current.getColumnNumber()];
                    int resultOfCompare = 0;
                    if(comparator.getType() == TokenType.DOUBLE_OPERAND) {
                        double value = comparator.getDoubleValue();
                        double parsedValueOfQuery = Double.parseDouble(queryChart);
                        resultOfCompare = Double.compare(parsedValueOfQuery, value);
                    }
                    if(comparator.getType() == TokenType.STRING_OPERAND) {
                        String value = comparator.getStringValue();
                        resultOfCompare = queryChart.compareToIgnoreCase(value);
                    }
                    if(operator.getType() == TokenType.MORE) {
                        if(resultOfCompare == 1) {
                            pushBooleanInStack(new Token(TokenType.TRUE));
                        } else {
                            pushBooleanInStack(new Token(TokenType.FALSE));
                        }
                    }
                    if(operator.getType() == TokenType.LESS) {
                        if(resultOfCompare == -1) {
                            pushBooleanInStack(new Token(TokenType.TRUE));
                        } else {
                            pushBooleanInStack(new Token(TokenType.FALSE));
                        }
                    }
                    if(operator.getType() == TokenType.EQUALS) {
                        if(resultOfCompare == 0) {
                            pushBooleanInStack(new Token(TokenType.TRUE));
                        } else {
                            pushBooleanInStack(new Token(TokenType.FALSE));
                        }
                    }
                    if(operator.getType() == TokenType.NOT_EQUALS) {
                        if(resultOfCompare != 0) {
                            pushBooleanInStack(new Token(TokenType.TRUE));
                        } else {
                            pushBooleanInStack(new Token(TokenType.FALSE));
                        }
                    }
                }
                if(current.getType() == TokenType.RIGHT_BRACKET) pushRightBracketInStack(current);
                if(current.getType() == TokenType.OR) pushOrInStack(current);
                if(current.getType() == TokenType.AND) pushAndInStack(current);
                i++;
            }
            if(stack.size() != 1) throw new IncorrectFilterQuery("error: некорректная строка фильтров");
            if(stack.pop().getType().getValue()) {
                filtered.add(query);
            }
        }

        return filtered;
    }

    private void pushBooleanInStack(Token token) {
        TokenType lastInStack = null;
        if(stack.isEmpty()) {
            stack.push(token);
        } else {
            lastInStack = stack.peek().getType();
        }
        if(lastInStack == TokenType.AND) processAndInStack(token);
        if(lastInStack == TokenType.OR) processOrInStack(token);
        if(lastInStack == TokenType.LEFT_BRACKET) stack.push(token);
    }

    private void processAndInStack(Token token) {
        stack.pop();
        Token secondOperand = stack.pop();
        if(secondOperand.getType().getValue() && token.getType().getValue()) {
            pushBooleanInStack(new Token(TokenType.TRUE));
        } else {
            pushBooleanInStack(new Token(TokenType.FALSE));
        }
    }

    private void processOrInStack(Token token) {
        stack.pop();
        Token secondOperand = stack.pop();
        if(secondOperand.getType().getValue() || token.getType().getValue()) {
            pushBooleanInStack(new Token(TokenType.TRUE));
        } else {
            pushBooleanInStack(new Token(TokenType.FALSE));
        }
    }

    private void pushRightBracketInStack(Token token) {
        Token booleanToken = stack.pop();
        stack.pop();
        pushBooleanInStack(booleanToken);
    }

    private void pushLeftBracketInStack(Token token) {
        stack.push(token);
    }

    private void pushOrInStack(Token token) {
        stack.push(token);
    }

    private void pushAndInStack(Token token) {
        stack.push(token);
    }
}
