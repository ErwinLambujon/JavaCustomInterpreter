public class Lexer {
    // Token types
    enum TokenType {
        KEYWORD, IDENTIFIER, LITERAL, SYMBOL, EOF
    }

    // Token class
    static class Token {
        TokenType type;
        String value;

        Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    private String code;
    private int currentPosition;

    public Lexer(String code) {
        this.code = code;
        this.currentPosition = 0;
        skipWhitespace(); // Skip initial whitespace characters
    }

    // Method to skip whitespace characters
    private void skipWhitespace() {
        while (currentPosition < code.length() && Character.isWhitespace(code.charAt(currentPosition))) {
            currentPosition++;
        }
    }

    public Token getNextToken() {
        skipWhitespace(); // Skip whitespace characters

        if (currentPosition >= code.length()) {
            System.out.println("DEBUG: Lexer output: EOF");
            return new Token(TokenType.EOF, "END OF FILE");
        }

        String substring = code.substring(currentPosition);

        if (substring.startsWith("BEGIN CODE")) {
            currentPosition += 10; // Skip "BEGIN CODE"
            System.out.println("DEBUG: Lexer output: KEYWORD BEGIN CODE");
            return new Token(TokenType.KEYWORD, "BEGIN CODE");
        } else if (substring.startsWith("END CODE")) {
            currentPosition += 8; // Skip "END CODE"
            System.out.println("DEBUG: Lexer output: KEYWORD END CODE");
            return new Token(TokenType.KEYWORD, "END CODE");
        }

        char currentChar = code.charAt(currentPosition);

        if (Character.isDigit(currentChar)) {
            // Numeric literal
            StringBuilder literalBuilder = new StringBuilder();
            while (currentPosition < code.length() && Character.isDigit(currentChar)) {
                literalBuilder.append(currentChar);
                currentPosition++;
                if (currentPosition < code.length()) {
                    currentChar = code.charAt(currentPosition);
                }
            }
            System.out.println("DEBUG: Lexer output: LITERAL " + literalBuilder.toString());
            return new Token(TokenType.LITERAL, literalBuilder.toString());
        }

        if (Character.isLetter(currentChar) || currentChar == '_') {
            // Identifier or keyword
            StringBuilder identifierBuilder = new StringBuilder();
            while (currentPosition < code.length() &&
                    (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
                identifierBuilder.append(currentChar);
                currentPosition++;
                if (currentPosition < code.length()) {
                    currentChar = code.charAt(currentPosition);
                }
            }
            String identifier = identifierBuilder.toString();
            if (identifier.equals("INT") || identifier.equals("CHAR") || identifier.equals("BOOL") || identifier.equals("FLOAT")
                    || identifier.equals("DISPLAY")) {
                System.out.println("DEBUG: Lexer output: KEYWORD " + identifier);
                return new Token(TokenType.KEYWORD, identifier);
            } else {
                System.out.println("DEBUG: Lexer output: IDENTIFIER " + identifier);
                return new Token(TokenType.IDENTIFIER, identifier);
            }
        } else if (currentChar == '"' || currentChar == '\'') {
            // Literal (string or char)
            char quote = currentChar;
            StringBuilder literalBuilder = new StringBuilder();
            currentPosition++;
            while (currentPosition < code.length() && code.charAt(currentPosition) != quote) {
                literalBuilder.append(code.charAt(currentPosition));
                currentPosition++;
            }
            currentPosition++; // Skip the closing quote
            System.out.println("DEBUG: Lexer output: LITERAL " + literalBuilder.toString());
            return new Token(TokenType.LITERAL, literalBuilder.toString());
        } else if (currentChar == '#' || currentChar == '&' || currentChar == '$' || currentChar == ':') {
            // Symbols
            currentPosition++;
            System.out.println("DEBUG: Lexer output: SYMBOL " + currentChar);
            return new Token(TokenType.SYMBOL, String.valueOf(currentChar));
        } else {
            // Skip unsupported characters
            currentPosition++;
            return getNextToken();
        }
    }
}
