import java.util.*;


public class Parser {
    private Lexer lexer;
    private Lexer.Token currentToken;

    private Map<String, String> symbolTable = new HashMap<>(); // To store variable types
    private Map<String, String> valueTable = new HashMap<>(); // To store variable values

    public Parser(Lexer lexer, Map<String, String> symbolTable, Map<String, String> valueTable) {
        this.lexer = lexer;
        this.currentToken = lexer.getNextToken();
        this.symbolTable = symbolTable;
        this.valueTable = valueTable;
    }

    private void eat(Lexer.TokenType expectedType, String expectedValue) {
        if (currentToken.type == expectedType && currentToken.value.equals(expectedValue)) {
            currentToken = lexer.getNextToken();
        } else {
            throw new RuntimeException("Syntax error: Expected token " + expectedType + " with value '" + expectedValue +
                    "' but found " + currentToken.type + " with value '" + currentToken.value + "'");
        }
    }

    private void parseVariableDeclaration() {
        String varType = currentToken.value;
        System.out.println("DEBUG: varType = " + varType); // Debugging statement
        eat(Lexer.TokenType.KEYWORD, varType);

        if (currentToken.type == Lexer.TokenType.EOF) {
            return;
        }

        while (currentToken.type == Lexer.TokenType.IDENTIFIER || currentToken.value.equals("=")) {
            if (currentToken.type == Lexer.TokenType.IDENTIFIER) {
                String varName = currentToken.value;
                symbolTable.put(varName, varType);
                eat(Lexer.TokenType.IDENTIFIER, varName);
                if (currentToken.value.equals("=")) {
                    eat(Lexer.TokenType.SYMBOL, "=");
                    System.out.println("DEBUG: currentToken after eating '=' = " + currentToken.value); // Debugging statement
                    if (currentToken.type == Lexer.TokenType.LITERAL) {
                        String value = currentToken.value;
                        System.out.println("DEBUG: value before eating literal = " + value); // Debugging statement
                        eat(Lexer.TokenType.LITERAL, value);
                        valueTable.put(varName, value);
                        System.out.println("DEBUG: value after eating literal = " + valueTable.get(varName)); // Debugging statement
                    } else if (currentToken.type == Lexer.TokenType.IDENTIFIER) {
                        String valueVarName = currentToken.value;
                        eat(Lexer.TokenType.IDENTIFIER, valueVarName);
                        if (valueTable.containsKey(valueVarName)) {
                            valueTable.put(varName, valueTable.get(valueVarName));
                        } else {
                            System.out.println("Semantic error: Variable " + valueVarName + " is not declared");
                        }
                    }
                } else {
                    // Initialize with default value if not already initialized
                    if (!valueTable.containsKey(varName)) {
                        valueTable.put(varName, getDefaultInitialValue(varType));
                    }
                }
                if (currentToken.value.equals(",")) {
                    eat(Lexer.TokenType.SYMBOL, ",");
                }
            } else if (currentToken.value.equals("=")) {
                throw new RuntimeException("Syntax error: Unexpected token '='");
            }

            // Check for end of input or next keyword
            if (currentToken.type == Lexer.TokenType.EOF || (currentToken.type == Lexer.TokenType.KEYWORD && currentToken.value.equals("DISPLAY"))) {
                break;
            }
        }
        System.out.println("symbolTable: " + symbolTable);
        System.out.println("valueTable: " + valueTable);
    }

    // Method to get default initial value based on variable type
    private String getDefaultInitialValue(String varType) {
        switch (varType) {
            case "INT":
            case "BOOL":
                return "0"; // Default value for INT and BOOL
            case "CHAR":
                return ""; // Default value for CHAR
            case "FLOAT":
                return "0.0"; // Default value for FLOAT
            default:
                return ""; // Default value for other types (should be handled based on your language's specification)
        }
    }

    private void parseDisplayStatement() {
        eat(Lexer.TokenType.KEYWORD, "DISPLAY");
        eat(Lexer.TokenType.SYMBOL, ":");

        while (currentToken.type != Lexer.TokenType.KEYWORD || !currentToken.value.equals("END CODE")) {
            if (currentToken.type == Lexer.TokenType.EOF) {
                throw new RuntimeException("Syntax error: Unexpected end of input while parsing display statement");
            }

            // Display token value
            if (currentToken.type == Lexer.TokenType.IDENTIFIER) {
                String varName = currentToken.value;
                if (!valueTable.containsKey(varName)) {
                    throw new RuntimeException("Semantic error: Variable " + varName + " is not declared");
                }
                System.out.print(valueTable.get(varName)); // Display the value of the variable
                currentToken = lexer.getNextToken();
            } else if (currentToken.type == Lexer.TokenType.LITERAL) {
                System.out.print(currentToken.value);
                currentToken = lexer.getNextToken();
            } else if (currentToken.type == Lexer.TokenType.SYMBOL && currentToken.value.equals("&")) {
                System.out.print(" ");
                currentToken = lexer.getNextToken(); // Move to the next token
            } else {
                throw new RuntimeException("Syntax error: Unexpected token " + currentToken.value);
            }
        }
    }


    public void parseCodeBlock() {
        System.out.println("DEBUG: Entering parseCodeBlock()");
        eat(Lexer.TokenType.KEYWORD, "BEGIN CODE"); // Consume BEGIN CODE

        // Parse variable declarations
        while (currentToken.type == Lexer.TokenType.KEYWORD &&
                (currentToken.value.equals("INT") ||
                        currentToken.value.equals("CHAR") ||
                        currentToken.value.equals("BOOL") ||
                        currentToken.value.equals("FLOAT"))) {
            System.out.println("DEBUG: Parsing variable declaration");
            System.out.println("DEBUG: Current token: " + currentToken.type + " " + currentToken.value);
            parseVariableDeclaration();
        }

        if (currentToken.type == Lexer.TokenType.EOF) {
            throw new RuntimeException("Syntax error: Unexpected end of file");
        }

        // Check for display statement
        boolean displayFound = false;
        while (currentToken.type != Lexer.TokenType.EOF) {
            System.out.println("DEBUG: Current token: " + currentToken.type + " " + currentToken.value);
            if (currentToken.type == Lexer.TokenType.KEYWORD && currentToken.value.equals("DISPLAY")) {
                parseDisplayStatement();
                displayFound = true;
                break;
            }
            currentToken = lexer.getNextToken();
        }

        // Throw error if 'DISPLAY' not found
        if (!displayFound) {
            throw new RuntimeException("Syntax error: Expected token KEYWORD with value 'DISPLAY' but found EOF with value 'END OF FILE'");
        }

        // Check for END CODE or EOF
        if (currentToken.type == Lexer.TokenType.KEYWORD && currentToken.value.equals("END CODE")) {
            eat(Lexer.TokenType.KEYWORD, "END CODE"); // Consume END CODE
            System.out.println("DEBUG: Exiting parseCodeBlock()");
        } else if (currentToken.type == Lexer.TokenType.EOF) {
            throw new RuntimeException("Syntax error: Expected token KEYWORD with value 'END CODE' but found EOF with value 'END OF FILE'");
        }
    }
}