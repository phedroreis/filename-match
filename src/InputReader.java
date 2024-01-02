import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/******************************************************************************
 * Classe responsavel por processar entradas de usuario pelo terminal.
 * 
 * @since 1.0 2 de janeiro de 2024
 * @version 1.0
 * @author Pedro Reis
 ******************************************************************************/
final class InputReader {
    
private final String label;
private final String enterOptionLabel;
private final String defaultOption;
private final InputParser parser;
private final BufferedReader inputReader;

/*-----------------------------------------------------------------------------

------------------------------------------------------------------------------*/
/**
 * Construtor da classe.
 * 
 * @param label Especifica a entrada
 * 
 * @param enterOptionLabel Especifica qual o valor default para esta entrada
 * 
 * @param defaultOption O valor default que deve ser retornado se o usuario 
 * teclar ENTER ou uma entrada em branco
 * 
 * @param parser Um objeto de uma classe que implemente a interface InputParser
 * e que sera responsavel por validar a entrada
 */
protected InputReader(
    final String label,
    final String enterOptionLabel,
    final String defaultOption,
    final InputParser parser
) {
    
    this.label = label;
    this.enterOptionLabel = enterOptionLabel;
    this.defaultOption = defaultOption;
    this.parser = parser;
    
    inputReader = new BufferedReader(new InputStreamReader(System.in));
    
}//construtor

/*-----------------------------------------------------------------------------

------------------------------------------------------------------------------*/
/**
 * O metodo apresenta a mensagem adequada especificando o tipo de entrada, le a
 * entrada e retorna um valor validado.
 * 
 * @return Uma entrada validada
 */
protected String readInput() throws IOException {
    
    boolean err;
    String input = null;
    
    do {
        err = false;
        
        System.out.println('\n' + label + ':');
        System.out.print("[ENTER = " + enterOptionLabel + "] >");
        
        try {           
      
            input = inputReader.readLine(); 
            
            if (input.isBlank()) return defaultOption;
            
            input = parser.parse(input);
        }
        catch (IllegalArgumentException e) {
            
            System.out.println("\n" + e.getMessage());
            err = true;             
        }
        
    } while (err);
    
    return input;
    
}//readInput         
    
}//classe InputReader
