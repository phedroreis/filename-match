import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/******************************************************************************
 * Classe responsavel por processar entradas de usuario pelo terminal.
 * 
 * @since 1.0 2 de janeiro de 2024
 * @version 1.0
 * @author Pedro Reis
 ******************************************************************************/
final class InputReader {
    
private String label;
private String enterOptionLabel;
private String defaultOption;
private InputParser parser;
private final BufferedReader inputReader;
private final PrintStream console;

/*-----------------------------------------------------------------------------

------------------------------------------------------------------------------*/
/**
 * Construtor.
 * 
 * @param console Um printStream para escrever no terminal
 * 
 * @param charset O encoding que console ira usar para ler dados
 * 
 * @throws UnsupportedEncodingException Se charset nao for reconhecido
 */
InputReader(final PrintStream console, final String charset)
    throws UnsupportedEncodingException {
    
    this.console = console;
    
    inputReader = new BufferedReader(new InputStreamReader(System.in, charset));
    
}//construtor

/*-----------------------------------------------------------------------------

------------------------------------------------------------------------------*/
/**
 * Configura o prompt de entrada de dados.
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
void setPrompt(
    final String label,
    final String enterOptionLabel,
    final String defaultOption,
    final InputParser parser
){
    
    this.label = label;
    this.enterOptionLabel = enterOptionLabel;
    this.defaultOption = defaultOption;
    this.parser = parser;      
  
}//setPrompt

/*-----------------------------------------------------------------------------

------------------------------------------------------------------------------*/
/**
 * O metodo apresenta a mensagem adequada especificando o tipo de entrada, le a
 * entrada e retorna um valor validado.
 * 
 * @return Uma entrada validada
 */
String readInput() throws IOException {
    
    boolean err;
    String input = null;
    
    do {
        err = false;
        
        console.println('\n' + label + ':');
        console.print("[ENTER = " + enterOptionLabel + "] >");
        
        try {           
      
            input = inputReader.readLine(); 
            
            if (input.isBlank()) return defaultOption;
            
            input = parser.parse(input);
        }
        catch (IllegalArgumentException e) {
            
            console.println("\n" + e.getMessage());
            err = true;             
        }
        
    } while (err);
    
    return input;
    
}//readInput   
    
}//classe InputReader
