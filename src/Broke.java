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
    
    private static final Map<String, String> MAP = new HashMap<>();
    
    private static final Pattern PRE =
        Pattern.compile("<pre>(.|\\n)+?<\\/pre>");
        
    private static final Pattern STYLE =
        Pattern.compile("<style>(.|\\n)+?<\\/style>"); 
        
    private static final Pattern SCRIPT =
        Pattern.compile("<script>(.|\\n)+?<\\/script>"); 
    
    private static int maxLength;
    
    private static int unbrokenLines = 0;
    
    /*-------------------------------------------------------------------------
        O programa nao quebra linhas dentro do escopo de tags pre, style e
        script, pois isso pode afetar a exibicao da pagina. Quando eh encontrada
        alguma linha, no escopo destas tags, que deveria ser quebrada mas nao
        foi, o programa emite um alerta no terminal.
    --------------------------------------------------------------------------*/ 
    private static void checkTagContent(
        final String content, 
        final String tagName
    ) {
        
        Scanner scanner = new Scanner(content);
        
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            
            if (line.length() > maxLength) {
                System.out.printf("\nAVISO: encontrada linha com mais de %,d" + 
                    " caracteres no escopo de uma tag %s. Linha nao quebrada!", 
                    maxLength, tagName
                );
                
                unbrokenLines++;
            }
        }//while
        
    }//checkTagContent
    
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

                    Onde 'max' deve ser o numero maximo de 
                    caracteres  permitidos  em  uma  linha.
                    """
                );
                System.exit(0);
            }

            File dirCurrent = new File(".");

            String[] filenames = dirCurrent.list(
                new FilenameFilter(){
                    @Override
                    public boolean accept(File f, String s) {
                        return s.toLowerCase().matches(".*\\.html?") ;
                    }
                }
            );

            for (String filename: filenames) {
                
                System.out.println("\nQuebrando " + filename + " ...\n");

                Path path = Path.of(filename);

                String content = Files.readString(path);
                
                int countMatches = 0;  
                
                String group; String replacer;
                
                Matcher matcher = PRE.matcher(content);
                
                while (matcher.find()) {
                    group = matcher.group();
                    checkTagContent(group, "<pre>");
                    replacer = "#" + countMatches++ + "A5FE34BC";
                    MAP.put(replacer, group);
                    content = content.replace(group, replacer);
                }
                
                matcher = STYLE.matcher(content);
                
                while (matcher.find()) {
                    group = matcher.group();
                    checkTagContent(group, "<style>");                
                    replacer = "#" + countMatches++ + "A5FE34BC";
                    MAP.put(replacer, group);
                    content = content.replace(group, replacer);
                }   

                matcher = SCRIPT.matcher(content);
                
                while (matcher.find()) {
                    group = matcher.group();
                    checkTagContent(group, "<script>");
                    replacer = "#" + countMatches++ + "A5FE34BC";
                    MAP.put(replacer, group);
                    content = content.replace(group, replacer);
                }                  
                 
                Scanner scanner = new Scanner(content);

                StringBuilder sb = new StringBuilder(1024);


                while (scanner.hasNext()) {

                    String line = scanner.nextLine();

                    while (line.length() > maxLength) {
                        
                        String brokeCharFinder = line.substring(0, maxLength);
                        
                        int p = brokeCharFinder.lastIndexOf('>');
                        if (p == -1) p = brokeCharFinder.lastIndexOf(' ');
                        if (p == -1) {
                            System.out.println("AVISO: linha nao quebrada!");
                            unbrokenLines++;
                            break;
                        }
                        
                        sb.append(line.substring(0, p + 1)).append('\n');
                        
                        line = line.substring(p + 1, line.length());
                        
                    }//while
                    
                    sb.append(line).append('\n');

                }//while
                
                content = sb.toString();
                
                for (String key: MAP.keySet()) {
                    
                    content = content.replace(key, MAP.get(key));
                    
                }

                try (PrintWriter pw = new PrintWriter(filename + ".broke.html")) {

                    pw.print(content);

                }//try   
                
            }//for
            
            System.out.printf("\n%,d linhas nao quebradas.\n", unbrokenLines);
        
        }//try
        catch (IOException e) {
            
            System.out.println(e);
            
        }
        catch (NumberFormatException e) {
            
            System.out.println("Numero max. invalido!");
            
        }
           
    }//main
    
}//classe Broke
