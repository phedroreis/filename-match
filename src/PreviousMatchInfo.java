/*******************************************************************************
 * Objetos desta classe representam as informacoes sobre um match catalogado
 * previamente. 
 * 
 * @version 1.0
 * @since 2.0 5 de janeiro de 2024
 * @author Pedro Reis
 ******************************************************************************/
final class PreviousMatchInfo {
    
    private final Pathname pathname;
    
    private final boolean isSameContent;
    
   /*-------------------------------------------------------------------------
                                    
    --------------------------------------------------------------------------*/ 
    /**
     * Construtor.
     * 
     * @param pathname O pathname do arq. que deu match.
     * 
     * @param isSameContent Se ambos tinham o mesmo conteudo.
     */
    PreviousMatchInfo(final Pathname pathname, final boolean isSameContent) {
        
        this.pathname = pathname;
        this.isSameContent = isSameContent;
        
    }//construtor
    
    /*-------------------------------------------------------------------------
                
    --------------------------------------------------------------------------*/  
    /**
     * Retorna o path absoluto do arquivo que deu este match.
     * 
     * @return o path absoluto do arquivo que deu este match.
     */
    Pathname getPathname() {
        
        return pathname;
        
    }//getPathname
    
    /*-------------------------------------------------------------------------
                
    --------------------------------------------------------------------------*/ 
    /**
     * Retorna se os dois arquivos tem o mesmo conteudo.
     * 
     * @return Se os dois arquivos tem o mesmo conteudo.
     */
    boolean isSameContent() {
        
        return isSameContent;
        
    }//isSameContent
    
}//classe PreviousMatchInfo
