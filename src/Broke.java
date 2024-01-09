import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Broke {
    
    /*-------------------------------------------------------------------------
          Retorna uma String contendo o caractere c repetido length vezes   
    --------------------------------------------------------------------------*/  
    private static String repeat(final char c, final int length) {
        
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++) s.append(c);
        return s.toString();
        
    }//repeat 
    
    /*-------------------------------------------------------------------------
                                    
    --------------------------------------------------------------------------*/     
    private static int getMaxBreakPoint(final String test) {

        return Math.max(test.lastIndexOf('>'), test.lastIndexOf('<') - 1);
    
    }//getMaxBreakPoint
    
    /*-------------------------------------------------------------------------
                                    
    --------------------------------------------------------------------------*/     
    private static int getMinBreakPoint(final String test) {
        
        int a = test.indexOf('>');
        int b = test.indexOf('<') - 1;
          
        int min = Math.min(a, b);
        
        if (min == -1) return Math.max(a, b); else return min;
      
    }//getMinBreakPoint
    
    /*-------------------------------------------------------------------------
                                    
    --------------------------------------------------------------------------*/ 
    public static void main(String[] args) {        
    
        final Map<String, String> map = new HashMap<>(256);
    
        final Map<String, Integer> overflowsMap = new HashMap<>(256);
    
        final String[] tags = {"pre", "style", "script"};        
         
        int maxLength = -1;
               
        if (args.length > 0) 

            maxLength = Integer.parseInt(args[0]); 
  
        else {

            System.out.println(
                """
                Digite java Broke max

                Se for JAR File digite java -jar NomeArq.jar max

                Onde 'max' deve ser o n\u00famero m\u00e1ximo de 
                caracteres  permitidos  em  uma  linha.
                """
            );
            System.exit(0);

        }//if-else
        
        if (maxLength < 1) {
            
            System.out.println("Comprimento da linha muito pequeno!");
            System.exit(0);
        }

        /*
        Obtem um array com os nomes de todos os arquivos do tipo HTML no
        diretorio corrente
        */
        String[] filenames = new File(".").list(
            new FilenameFilter(){
                @Override
                public boolean accept(File f, String s) {
                    return s.toLowerCase().matches(".*\\.html?") ;
                }
            }
        );
        
        if (filenames.length == 0) {
            
            System.out.println("Nenhum arquivo HTML encontrado!");
            System.exit(0);
            
        }
        
       /*
        Tenta inicialmente criar uma barra de status de progresso de 60
        caracteres de largura
        */
        int barLength = 61;

        //O num. de arqs. que cada pontinho na barra de status representa
        int filesPerDot; 

        /*
        O loop encontra o maior divisor de filenames.length menor que 
        61, cujo resto da divisao seja menor que o quociente da divisao.
        */
        do {

        } while (
            (filenames.length % --barLength) >= 
            (filesPerDot = filenames.length / barLength)
        );

        //Conta o num. de arqs. jah processados
        int countFiles = 0; 

        //Imprime a barra de progresso
        System.out.print("\n0%|" + repeat(' ', barLength) + "|100%\n   "); 
        
        try {    

            /*
            Processa cada arquivo HTML no diretorio corrente
            */
            for (String filename: filenames) {
                
                int brokenLines = 0;
 
                //Objeto p/ acessar o arquivo
                Path path = Path.of(filename);
                
                String outputFilename = 
                    filename.replaceAll("\\.(html?|HTML?)", ".broke.html");
                
                //Joga o conteudo do arquivo pra dentro da String content
                String content = Files.readString(path);
                
                int countMatches = 0;  
                
                String group; String replacer;
                
                Pattern pattern;
                
                Matcher matcher;
                /*
                Substitui todas as tags pre, style e script por marcadores. 
                */
                for (String tag : tags) {
                    
                    pattern = Pattern.compile(
                        "<" + tag + "[\\s\\S]*?>[\\s\\S]*?<\\/" + tag + ">"
                    );
                        
                    matcher = pattern.matcher(content);
                    
                    while (matcher.find()) {
                        group = matcher.group();
                        replacer = "#" + countMatches++ + "A5FE34BC";
                        map.put(replacer, group);
                        content = content.replace(group, replacer);
                    }//while
                    
                }//for
                    
                    
                Scanner scanner = new Scanner(content);

                StringBuilder sb = new StringBuilder(65536);

                //Le linha por linha do arquivo 
                while (scanner.hasNext()) {

                    String line = scanner.nextLine();

                    //Linhas maiores que maxLength sao quebradas sucessivamente
                    while (line.length() > maxLength) {
                          
                        int breakPoint = 
                            getMaxBreakPoint(line.substring(0, maxLength));
                        
                        if (breakPoint < 0) {
                            
                            breakPoint = getMinBreakPoint(line);
                            if (breakPoint < 0) break;
                               
                        }
                        
                        brokenLines++;
                        
                        sb.append(line.substring(0, breakPoint+1)).append('\n');
                        
                        line = line.substring(breakPoint + 1, line.length());
                        
                    }//while
                    
                    sb.append(line).append('\n');

                }//while
                
                //Recebe o conteudo do arquivo com as linhas quebradas
                content = sb.toString();
                
                //Reinsere as tags pre, style e script no lugar dos marcadores
                for (String key: map.keySet()) {
                    
                    content = content.replace(key, map.get(key));
                    
                }//for

                //Grava em um arquivo com o nome do arquivo original e a 
                //extensao broke.html
                if (brokenLines > 0) {
                    
                    try ( PrintWriter pw = new PrintWriter(outputFilename) ) {

                        pw.print(content);

                    }//try  
                    
                    int overflows = 0;
                    
                    scanner = new Scanner(content);
                    
                    while (scanner.hasNext()) 
                        if (scanner.nextLine().length() > maxLength) overflows++;
                    
                    if (overflows > 0) 
                        overflowsMap.put(outputFilename, overflows);
                }
                
                //A cada countFiles arqs. processados, um ponto eh impresso na 
                //barra de progresso
                if (++countFiles % filesPerDot == 0) System.out.print("."); 
      
            }//for
            
            System.out.println("\n\nFeito!\n");
            
            for (String key : overflowsMap.keySet()) {
                
                int numberOfOverflows = overflowsMap.get(key);
                
                System.out.printf(
                    "%s : Transbordamento em %d linha%s\n",
                    key,
                    numberOfOverflows,
                    (numberOfOverflows > 1 ? "s" : "")
                );
                
            }//for
         
        }//try
        catch (IOException e) {
            
            System.out.println(e);
            
        }
        catch (NumberFormatException e) {
            
            System.out.println("N\u00famero max. inv\u00e1lido!");
            
        }
           
    }//main
    
}//classe Broke