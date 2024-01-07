import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Broke {
    
    public static void main(String[] args) {
        
        int maxLength = -1;
        
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

                Scanner scanner = new Scanner(content);

                StringBuilder sb = new StringBuilder(1024);

                int countLines = 0;

                while (scanner.hasNext()) {

                    String line = scanner.nextLine();
                    countLines++;

                    while (line.length() > maxLength) {
                        
                        String brokeCharFinder = line.substring(0, maxLength);
                        
                        int p = brokeCharFinder.lastIndexOf('>');
                        if (p == -1) p = brokeCharFinder.lastIndexOf(' ');
                        if (p == -1) {
                            System.out.println(
                                "Impossivel quebrar linha " + countLines
                            );
                            break;
                        }
                        
                        sb.append(line.substring(0, p + 1)).append('\n');
                        
                        line = line.substring(p + 1, line.length());
                        
                    }//while
                    
                    sb.append(line).append('\n');

                }//while

                try (PrintWriter pw = new PrintWriter(filename + ".broke.html")) {

                    pw.print(sb.toString());

                }//try   
                
            }//for
        
        }//try
        catch (IOException e) {
            
            System.out.println(e);
            
        }
        catch (NumberFormatException e) {
            
            System.out.println("Numero max. invalido!");
            
        }
           
    }//main
    
}
