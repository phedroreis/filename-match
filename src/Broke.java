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
    
    private static final Map<String, String> MAP = new HashMap<>(256);
    
    private static final Map<String, Integer> WARNINGS = new HashMap<>(256);
    
    private static final String[] TAGS = {"pre", "style", "script"};

    private static int maxLength;
    
    private static int unbrokenLines;
    
    /*-------------------------------------------------------------------------
        O programa nao quebra linhas dentro do escopo de tags pre, style e
        script, pois isso pode afetar a exibicao da pagina.
    --------------------------------------------------------------------------*/ 
    private static void checkTagContent(final String content) {
        
        Scanner scanner = new Scanner(content);
        
        while (scanner.hasNext()) {
            
            String line = scanner.nextLine();
            
            if (line.length() > maxLength) unbrokenLines++;
      
        }//while
        
    }//checkTagContent
    
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
    public static void main(String[] args) {
                       
        try {
            
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
            
           /*
            Tenta inicialmente criar uma barra de status de progresso de 60
            caracteres de largura
            */
            int barLength = 61;
            
            //O num. de arqs. que cada pontinho na barra de status representa
            int filesPerDot; 
            
            /*
            O loop encontra o maior divisor de pathnamesListSize menor que 
            61, cujo resto da divisao seja menor que o quociente da divisao.
            
            Este divisor serah o comprimento da barra de progresso. E cada ponto
            impresso nesta barra irah representar um numero de arquivos
            processados que eh igual ao quociente (filesPerDot) dessa divisao
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

            /*
            Processa cada arquivo HTML no diretorio corrente
            */
            for (String filename: filenames) {
                
                unbrokenLines = 0;
                
                Path path = Path.of(filename);//Objeto p/ acessar o arquivo
                
                //Joga o conteudo do arquivo pra dentro da String content
                String content = Files.readString(path);
                
                int countMatches = 0;  
                
                String group; String replacer;
                
                Pattern pattern;
                
                Matcher matcher;
                /*
                Substitui todas as tags pre, style e script por marcadores. 
                */
                for (String tag : TAGS) {
                    
                    pattern = Pattern.compile(
                        "<" + tag + ".*?>(!\u13a3)+?<\\/" + tag + ">"
                    );
                        
                    matcher = pattern.matcher(content);
                    
                    while (matcher.find()) {
                        group = matcher.group();
                        checkTagContent(group);
                        replacer = "#" + countMatches++ + "A5FE34BC";
                        MAP.put(replacer, group);
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
                        
                        String brokeCharFinder = line.substring(0, maxLength);
                        
                        int p = brokeCharFinder.lastIndexOf('>');
                        if (p == -1) p = brokeCharFinder.lastIndexOf(' ');
                        if (p == -1) {
                            unbrokenLines++;
                            break;
                        }
                        
                        sb.append(line.substring(0, p + 1)).append('\n');
                        
                        line = line.substring(p + 1, line.length());
                        
                    }//while
                    
                    sb.append(line).append('\n');

                }//while
                
                //Recebe o conteudo do arquivo com as linhas quebradas
                content = sb.toString();
                
                //Reinsere as tags pre, style e script no lugar dos marcadores
                for (String key: MAP.keySet()) {
                    
                    content = content.replace(key, MAP.get(key));
                    
                }//for

                //Grava em um arquivo com o nome do arquivo original e a 
                //extensao broke.html
                try (
                    PrintWriter pw = new PrintWriter(filename + ".broke.html")
                ) {

                    pw.print(content);

                }//try  
                
                //A cada countFiles arqs. processados, um ponto eh impresso na 
                //barra de progresso
                if (++countFiles % filesPerDot == 0) System.out.print("."); 

                if (unbrokenLines > 0) WARNINGS.put(filename, unbrokenLines);
                    
            }//for
            
            System.out.println("\n\nFeito!\n");
            
            for (String key : WARNINGS.keySet()) {
                System.out.println(
                    key + " : " + WARNINGS.get(key) + 
                    " linha(s) n\u00e3o quebrada(s)"
                );
            }
         
        }//try
        catch (IOException e) {
            
            System.out.println(e);
            
        }
        catch (NumberFormatException e) {
            
            System.out.println("N\u00famero max. inv\u00e1lido!");
            
        }
           
    }//main
    
}//classe Broke