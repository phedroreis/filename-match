import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class CountChars {       
       
    public static void main(String[] args) throws IOException {
        
        final List<int[]> sortedList = new LinkedList<>();   
        
        boolean sortedByLength = (args.length == 0);
        
        File dirCurrent = new File(".");
        
        String[] filenames = dirCurrent.list(
            new FilenameFilter(){
                @Override
                public boolean accept(File f, String s) {
                    return s.toLowerCase().matches(".*\\.html?") ;
                }
            }
        );
        
        StringBuilder sb;        
        
        for (String filename: filenames) {
            
            Path path = Path.of(filename);
            
            String content = Files.readString(path);
            
            Scanner scanner = new Scanner(content);
            
            sb = new StringBuilder(1024);     
            
            int countLines = 0;
            
            while (scanner.hasNext()) {
                
                String line = scanner.nextLine();
                
                int[] data = new int[2];
                
                countLines++;
                        
                if (sortedByLength) { 
                    data[0] = line.length(); data[1] = countLines;                   
                    
                }
                else {                    
                    data[0] = countLines; data[1] = line.length(); 
                }
               
                sortedList.add(data);
            }
 
          
            sb = new StringBuilder(1024);
            
            if (sortedByLength) {
                Collections.sort(sortedList, new Compare()); 
                sb.append("comprimento : linha\n");
            }                
            else 
                sb.append("      linha : comprimento\n"); 

            
            for (int[] data : sortedList) {       

                sb.append(String.format("% 11d : %d \n", data[0], data[1]));
            }            
            
            try (PrintWriter pw = new PrintWriter(filename + ".txt")) {
                
                pw.print(sb.toString());
                
            }   
            
        }//for
           
    }//main
    
    private static final class Compare implements Comparator<int[]> {

        @Override
        public int compare(int[] a, int[] b) {
            
            return ((int[])a)[0] - ((int[])b)[0];
        }        
    }
    
}//classe CountChars