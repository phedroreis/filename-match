import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class CountChars {
    
    public static void main(String[] args) throws IOException {
        
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
            
            Path path = Path.of(filename);
            
            String content = Files.readString(path);
            
            Scanner scanner = new Scanner(content);
            
            StringBuilder sb = new StringBuilder(1024);
            
            int countLines = 1;
            
            while (scanner.hasNext()) {
                
                String line = scanner.nextLine();
                
                sb.append(
                    String.format(
                        "% 6d] %,d\n", 
                        countLines++,
                        line.length()
                    )
                );
            }
            
            try (PrintWriter pw = new PrintWriter(filename + ".txt")) {
                
                pw.print(sb.toString());
                
            }          
        }
           
    }//main
    
}
