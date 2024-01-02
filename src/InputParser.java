/******************************************************************************
 * As classes que implementarem esta interface devem fornecer o metodo para
 * validar uma entrada de usuario para a classe FilenameMatch.
 * 
 * @since 1.0 2 de janeiro de 2024
 * @version 1.0
 * @author Pedro Reis
 ******************************************************************************/
interface InputParser {
    
    /**
     * O metodo deve realizar a validacao de uma entrada de usuario para a 
     * classe FilenameMatch.
     * 
     * @param input A entrada digitada pelo usuario
     * 
     * @return A propria entrada, se validada
     * 
     * @throws IllegalArgumentException Uma implementacao deste metodo deve
     * lancar uma IllegalArgumentException para toda entrada invalida, com a
     * mensagem de erro adequada. Se a entrada invalida causar outro tipo de 
     * excecao, esta deve ser capturada e o bloco catch correspondente deve 
     * entao lancar uma IllegalArgumentException
     */
     public String parse(final String input) throws IllegalArgumentException;
    
}//interface InputParser
