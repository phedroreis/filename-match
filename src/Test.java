import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class Test {
    
    public static void main(String[] args) throws UnsupportedEncodingException {
         
        System.out.println("ç");
        System.out.println("charset do sistema:" + System.getProperty("file.encoding"));
        
                
        var isrDefault = new InputStreamReader(System.in);
        
        System.out.println("charset de leitura padrao:" + isrDefault.getEncoding());

        var isr = new InputStreamReader(System.in, System.getProperty("file.encoding"));
        
        System.out.println("charset de leitura customizado:" + isr.getEncoding());
    }
}
