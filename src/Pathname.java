import java.io.File;
import java.util.HashMap;
import java.util.Map;
/*******************************************************************************
 * Objetos desta classe representam os dados do pahtname de um arquivo. 
 * 
 * @version 1.0
 * @since 1.0 4 de janeiro de 2024
 * @author Pedro Reis
 ******************************************************************************/
final class Pathname {
    
    private static final Map<Integer, String> PATHMAP = new HashMap<>(256);
    
    private final int pathId;
    
    private final String filename;
    
    private final String name;
    
     /*-------------------------------------------------------------------------
                Insere o caminho de um diretorio e sua ID no mapa
    --------------------------------------------------------------------------*/   
    static void map(final int pathId, final String path) {
        
        PATHMAP.put(pathId, path);
        
    }//map   
 
    /*-------------------------------------------------------------------------
         Converte uma String em sua versao minuscula e sem acentos. Eh
         usado para comparar Strings em nomes de arquivos. Assim a String
         Açúcar serah considerada match com acucar, por exemplo.
    --------------------------------------------------------------------------*/  
    private static String normalize(final String str) {
        
        char[] charArray = str.toLowerCase().toCharArray();
        
        for (int i = 0; i < charArray.length; i++) {
            
            switch(charArray[i]) {
                case '\u00e0':
                case '\u00e1':
                case '\u00e2':
                case '\u00e3':
                    charArray[i] = 'a'; break;
                case '\u00e8':
                case '\u00e9':
                case '\u00ea':
                case '\u00eb':
                    charArray[i] = 'e'; break;
                case '\u00ec':
                case '\u00ed':
                case '\u00ee':
                case '\u00ef':
                case '\u0129':
                    charArray[i] = 'i'; break;
                case '\u00f2':
                case '\u00f3':
                case '\u00f4':
                case '\u00f5':
                case '\u00f6':                   
                    charArray[i] = 'o'; break;
                case '\u00f9':
                case '\u00fa':
                case '\u00fb':
                case '\u00fc':
                case '\u0169': 
                    charArray[i] = 'u'; break;
                case '\u00e7':
                    charArray[i] ='c';
            }
        }//for
        
        return new String(charArray);
        
    }//normalize        
  
    /*-------------------------------------------------------------------------
                              Construtor da classe
    --------------------------------------------------------------------------*/      
    Pathname(final int pathId, final File file) {
        
        this.pathId = pathId;
        
        filename = '/' + file.getName();
        
        int p = filename.lastIndexOf('.');
        
        if (p < 2)            
            name = normalize(filename.substring(1));
        else
            name = normalize(filename.substring(1, p));        
        
    }//construtor
    
    /*-------------------------------------------------------------------------
                    Retorna o nome do arquivo sem a sua extensao
    --------------------------------------------------------------------------*/       
    String getName() {
        
        return name;
        
    }//getName
    
    /*-------------------------------------------------------------------------
                         Retorna o pathname absoluto do objeto
    --------------------------------------------------------------------------*/   
    @Override
    public String toString() {
        
        return PATHMAP.get(pathId) + filename;
        
    }//toString
           
    
}//classe Pathname