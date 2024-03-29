import java.util.*;
public class CodeInterpreter {
    private static Map<String, String> symbolTable = new HashMap<>(); // To store variable types
    private static Map<String, String> valueTable = new HashMap<>(); // To store variable values

    public static void interpret(String code) {
        Lexer lexer = new Lexer(code);
        Parser parser = new Parser(lexer, symbolTable, valueTable);
        parser.parseCodeBlock();
    }

    public static void main(String[] args) {
        interpret("BEGIN CODE\n" +
                "INT z = 5\n" +
                "DISPLAY: z\n" +
                "END CODE");
    }
}
