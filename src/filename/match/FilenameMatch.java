package filename.match;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public final class  FilenameMatch {
    
    /*
    Essa estrutura de dados irah armazenar em uma lista todos os pathnames dos 
    arquivos a serem testados no diretorio de varredura e de seus subdiretorios 
    */
    private static final List<String> PATHNAMES_LIST = new LinkedList<>();
    
    /*
    Armazena o conjunto de todas as strings que produziram matches entre 
    arquivos
    */
    private static final Set<String> MATCHES_SET = new LinkedHashSet<>();
    
    /*
    Essa string define os caracteres que serao considerados delimitadores de
    palavras nos nomes de arquivos
    */
    private static final String TOKENS_DELIMITERS = " .-_"; 
   
    /*
    Uma String com as extensoes dos tipos de arquivos que serao pesquisados
    */
    private static String fileExtensions = null;
    
    /*
    O diretorio a partir do qual serah feita a varredura
    */
    private static File searchDir = null;
     
    /*
    Arquivo para onde redirecionar a saida padrao de texto
    */
    private static PrintStream outputStream = null;
    
    /*
    Esta constante serve para testar se o usuario entrou com um dado que pode
    ser convertido para numero. Se a entrada for uma String que nao pode ser
    convertida para inteiro, uma excecao serah lancada e o bloco catch sabera
    disso porque o valor da variavel continuarah igual ao dessa constante. E 
    assim poderah dar a msg de erro correta.
    */
    private static final int RANDOM_NEGATIVE = -175121;
    
    /*
    Define o numero minimo de tokens para um match entre dois nomes de arquivos
    */
    private static int matchLength = RANDOM_NEGATIVE;
    
    /*
    Define o comprimento minimo para tokens (em caracteres)
    */
    private static int minimumTokenLength = RANDOM_NEGATIVE;
    
    /*
    Um cash que armazena hashs sha256 jah calculados como um mapa, associando 
    cada hash obtido com o pathname do seu arquivo. Sua funcao eh evitar de 
    calcular o hash de um arquivo mais de uma vez. Se jah estiver neste mapa, o
    hash sha eh retornado
    */
    private static Map<String,String> shaMap;
    
    /*
    Determina se o conteudo dos arquivos que dao match serao ou nao comparados
    para ver se sao iguais. O flag -sha, que deve ser passado como argumento ao
    executar o programa, habilita esta checagem
    */
    private static boolean checkSha = false;
    
    /*-------------------------------------------------------------------------
                    Processa os prompts de entrada do usuario
    --------------------------------------------------------------------------*/      
    @SuppressWarnings("null")
    private static void readInputs() throws IOException {
 
        BufferedReader inputReader = 
            new BufferedReader(new InputStreamReader(System.in));
        
        /*
        Cada solicitacao de entrada para o usuario estah "presa" dentro de um
        loop do-while. Enquanto uma entrada valida nao for digitada, a variavel
        err estarah com valor true e serah solicatado que o usuario entre com
        o dado novamente
        */
        boolean err;
        
        do {  //Solicita o diretorio de varredura 
            
            err = false;
            try { 
              
                System.out.println("\nDiret\u00f3rio de varredura:");
                System.out.print("[ENTER = Diret\u00f3rio corrente] > ");
                String input = inputReader.readLine();
                
                //Entrada em branco seleciona diretorio corrente
                if (input.isBlank()) input = ".";

                searchDir = new File(input);           

                //Lanca excecao se nao selecionou diretorio existente
                if (!searchDir.isDirectory()) 
                    throw new IllegalArgumentException(
                        "N\u00e3o \u00e9 um diret\u00f3rio"
                    );
                
                //Lanca excecao se usuario nao tem direito de leitura
                if (!searchDir.canRead()) 
                    throw new IOException(
                        "Imposs\u00edvel ler neste diret\u00f3rio"
                    );
       
            }
            catch(IllegalArgumentException | IOException e) {
                
                System.out.println("\n" + e.getMessage());
                err = true;               
            }
          
        } while(err);
        
        do {  //Seleciona o n. minimo de tokens coincidentes para dar match  
            
            err = false; 
            try { 
                
                System.out.println("\nM\u00ednimo de palavras para dar match:");
                System.out.print("[ENTER = 3] > ");
                String input = inputReader.readLine();
                
                if (input.isBlank()) //Entrada nula seleciona valor default
                    
                    matchLength = 3;
                
                else //Escolha do usuario
                    
                    matchLength = Integer.parseInt(input); 
                
                //Menor que 2 lanca excecao. Valor invalido
                if (matchLength < 2) throw new IllegalArgumentException(
                   "Valor deve ser maior ou igual a 2"
                );
        
            }
            catch(IllegalArgumentException e) {
                
                /*
                matchLength foi inicializada com valor = RANDOM_NEGATIVE, se
                este valor nao foi alterado eh porque o usuario digitou uma 
                entrada que nao pode ser convertida para inteiro
                */
                if (matchLength == RANDOM_NEGATIVE) 
                    
                    System.out.println("\nEntre valor v\u00e1lido");
                
                else //digitou valor menor que 2
                    
                    System.out.println("\n" + e.getMessage());
                
                err = true;
            }
          
        } while(err);   
        
        
        do {  //Numero minimo de caracteres para ser considerado token 
            
            err = false; 
            
            try { 
                
                System.out.println(
                    "\nM\u00ednimo de caracteres que deve ter um token:"
                );
                System.out.print("[ENTER = 2] > ");
                String input = inputReader.readLine();
                
                if (input.isBlank()) //Entrada nula seleciona valor default
                    
                    minimumTokenLength = 2;
                
                else //Valor selecionado pelo usuario
                    
                    minimumTokenLength = Integer.parseInt(input); 
                
                //Menor que 1 lanca excecao. Valor invalido
                if (minimumTokenLength < 1) throw new IllegalArgumentException(
                   "Valor deve ser maior ou igual a 1"
                );
        
            }
            catch(IllegalArgumentException e) {
                
                /*
                minimumTokenLength foi inicializada com valor = RANDOM_NEGATIVE,
                se este valor nao foi alterado eh porque o usuario digitou uma 
                entrada que nao pode ser convertida para inteiro
                */                
                if (minimumTokenLength == RANDOM_NEGATIVE) 
                    
                    System.out.println("\nEntre valor v\u00e1lido");
                
                else //Valor menor que 1
                    
                    System.out.println("\n" + e.getMessage());
                
                err = true;
            }
          
        } while(err);            
     
        File outputFile = null;
        
        do { //Define arquivo de saida
            
            err = false; 
            try { 
              
                System.out.println("\nArquivo de sa\u00edda (com o caminho):");
                System.out.print("[ENTER = sa\u00edda na tela] > ");
                String outputFilename = inputReader.readLine(); 
                
                /*
                Entrada nula define opaco defaul de saidas na tela. O objeto
                outputFile permanece com valor null e isso faz com que as saidas
                nao sejam redirecionadas para algum arquivo
                */
                if (outputFilename.isBlank()) break;
                
                outputFile = new File(outputFilename);
                
                //Se o arquivo selecionado jah existir, este serah apagado
                if (outputFile.exists()) outputFile.delete();
                               
                if (!outputFile.createNewFile()) throw new IllegalArgumentException(
                   "Imposs\u00edvel criar arquivo"
                );
        
            }
            catch(IllegalArgumentException | IOException e) {
                
                System.out.println("\n" + e.getMessage());                
                err = true;
            }
          
        } while(err); 
        
        //Define os tipos de arquivos que serao pesquisados
        System.out.println(
            "\nExtens\u00f5es de arquivos a serem pesquisados " +
            "(sem ponto e separadas por espa\u00e7o):"
        );
        System.out.print("[ENTER = qualquer arquivo] > ");
   
        fileExtensions = inputReader.readLine().toLowerCase() + " "; 
        
        //Se foi aberto um outputFile, entao as saidas sao redirecionadas a
        //este arquivo
        if (outputFile != null) {
            
            outputStream = new PrintStream(new FileOutputStream(outputFile));
            System.setOut(outputStream);            
        }
         
    }//readInputs
    
    /*-------------------------------------------------------------------------
          Retorna uma lista filtrada de files que contem apenas diretorios e
          arquivos com as extensoes selecionadas pelo usuario
    --------------------------------------------------------------------------*/    
    private static File[] getFileList(final File dir) {
        
        File[] fileList = dir.listFiles(
                
            new FileFilter() {
              
                @Override
                public boolean accept(File file) {
                    
                  if (file.isDirectory() || fileExtensions.isBlank()) return true;
                  
                  String filename = file.getName();
                  String ext = 
                     filename.substring(filename.lastIndexOf('.') + 1) + " ";
                  
                  return fileExtensions.contains(ext.toLowerCase());
                  
                }
            }
        );
        
        return fileList;
        
    }//getFileList
    
    /*-------------------------------------------------------------------------
           Extrai o nome do arquivo de seu path absoluto, descartando tambem
           a extensao (se houver)
    --------------------------------------------------------------------------*/     
    private static String getFilenameFromPath(final String absolutePath) {
        
        String filename = new File(absolutePath).getName(); 
        
        int p = filename.lastIndexOf(".");
        
        if (p < 1)            
            return filename;
        else
            return filename.substring(0, p);
        
    }//getFilenameFromPath
    
    /*--------------------------------------------------------------------------
       Extrai para um array todos os tokens (palavras) de um nome de arquivo
    --------------------------------------------------------------------------*/
    private static String[] getTokens(final String absolutePath) {
        
        StringTokenizer tokenizer = 
            new StringTokenizer(
                getFilenameFromPath(absolutePath), 
                TOKENS_DELIMITERS
            );
        
        String[] tokensArray = new String[tokenizer.countTokens() + 1];
        
        /*
        Guarda o proprio pathname do arquivo na posicao 0 do array
        */
        tokensArray[0] = absolutePath;
        
        int countTokens = 1;
        
        while (tokenizer.hasMoreTokens()) {
              
            tokensArray[countTokens++] = tokenizer.nextToken(); 
        }

        return tokensArray;
        
    }//getTokens    
    
    /*-------------------------------------------------------------------------
            Lista todos os arquivos com um determinado match no nome
    --------------------------------------------------------------------------*/  
    private static void listMatches() {
       
       for (String match : MATCHES_SET) {
           
           System.out.println("\nNomes de arquivos com \"" + match + "\" :\n");
           
           for (String absolutePath : PATHNAMES_LIST) {
               
               String[] tokens = getTokens(absolutePath);
               
               StringBuilder normalized = new StringBuilder();
               
               for (int i = 1; i < tokens.length; i++) 
                   normalized.append(tokens[i]).append('-');
               
               if (normalized.toString().toLowerCase().contains(match))
                   System.out.println(absolutePath);
               
            }//for
           
        }//for  
       
    }//listMatches
    
    /*-------------------------------------------------------------------------
                 Retorna o HASH sha256 do conteudo de um arquivo
    --------------------------------------------------------------------------*/      
    private static String getSHA256(final String absolutePath) 
        throws IOException, NoSuchAlgorithmException {
        
        if (!checkSha) return "#";
        
        //Procura o sha256 no cash shaMap
        if (shaMap.containsKey(absolutePath)) return shaMap.get(absolutePath);
        
        File file = new File(absolutePath);
        
        byte[] sha256;

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        
        /*Arquivo menor que 100MB le direto para array de byte*/
        if (file.length() < 100000000) {
            
            sha256 =
                messageDigest.digest(Files.readAllBytes(Paths.get(absolutePath)));
            
        }
        else {/*Maior que 100MB le blocos de 40MB de cada vez*/
            
            try 
               (FileInputStream inputFile = new FileInputStream(absolutePath)) {
                
                byte[] dataBytes = new byte[2097152];
                int bytesFromFile;

                while ( (bytesFromFile=inputFile.read(dataBytes)) != -1 ) {

                    messageDigest.update(dataBytes,0,bytesFromFile); 

                }//while
            
            }//try
    
            sha256 = messageDigest.digest();
            
        }//fim do if-else       
        
        /*
        Converte o hash para o formato de uma String com o valor em hexadecimal
        */
        StringBuilder hexString = new StringBuilder();
        
        for (byte b : sha256) {
           
            hexString.append(String.format("%02X", 0xFF & b));
        }
        
        /*
        Insere o pathname do arquivo e seu hash no cash shaMap para nao ter
        que calcula-lo novamente
        */
        shaMap.put(absolutePath, hexString.toString());
        
        return hexString.toString();
        
    }//getSHA256
    
    /*-------------------------------------------------------------------------
          Um metodo recursivo que insere na estrutura PATHNAMES_LIST os
          pathnames de todos os arquivos validos no diretorio dir e seus 
          subdiretorios          
    --------------------------------------------------------------------------*/    
    private static void getPathnames(final File dir)
        throws IOException, NoSuchAlgorithmException {
        
        /*
        O metodo retorna apenas arquivos cujas extensoes sao elegiveis para
        pesquisa
        */
        File[] fileList = getFileList(dir);
        
        for (File file : fileList) {
            
            if (file.isFile()) {
                
                /*Se file eh arquivo, insere seu nome na lista*/                
                PATHNAMES_LIST.add(file.getAbsolutePath());
                
            } else {
                
                /*Se file eh subdiretorio, o metodo se chama recursivamente
                para continuar a pesquisa neste subdiretorio*/                
                getPathnames(file);                
            }            
        }
        
        /*
        Ao final do loop sabemos quantos arquivos existem para ser pesquisados.
        Podemos criar um cash (na forma de um mapa) para armazenar todos os 
        hashs que forem obtidos destes arquivos. A capacidade inicial deste 
        mapa eh estimada em 10% do numero total de arquivos, jah que nem todos 
        terao matches e portanto nem todos terao que ter seus hashs obtidos
        */
        shaMap = new HashMap<>(fileList.length / 10);
        
    }//getPathnames
     
    /*-------------------------------------------------------------------------
          Verifica se ha match entre a string source (jah convertida para
          array de tokens) e a string target        
    --------------------------------------------------------------------------*/    
    private static boolean isMatch(
        final String[] sourceArray,
        final String targetStr
    ) {
        
        /*
        Obtem o array de tokens da String alvo (que eh o nome do arquivo sem
        sua extensao)
        */
        String[] targetArray = getTokens(targetStr);
        
        int matchCounter = 0;
        int sourceIndex = 1;
        int targetIndex = 1;
        
        while (sourceIndex < sourceArray.length) {
            
            if (
                sourceArray[sourceIndex].length() >= minimumTokenLength
                    &&
                sourceArray[sourceIndex].
                    equalsIgnoreCase(targetArray[targetIndex])
            ) {
                
                sourceIndex++;
                matchCounter++;
                
            } else {
                
                sourceIndex = sourceIndex - matchCounter;
                matchCounter = 0;
                
            }//fim do if-else
            
            if (matchCounter == matchLength) {
                
                sourceIndex = sourceIndex - matchCounter;
                
                String match = "";
                
                int j = sourceIndex + matchCounter - 1;
                
                for (int i = sourceIndex; i <= j; i++)
                    match = match + sourceArray[i] + (i < j ? "-" : "");
                
                MATCHES_SET.add(match.toLowerCase());
                
                return true;
            }
            
            targetIndex++;
            
            if (targetIndex == targetArray.length) {
                
                sourceIndex++;
                targetIndex = 1;
                matchCounter = 0;
                
            }//fim do if
            
        }//fim do while        
       
        return false;
        
    }//isMatch
    
    /*-------------------------------------------------------------------------
        Recebe o array com os tokens de um nome de arquivo e compara com os
        nomes de todos os arquivos no diretorio corrente e nos seus
        subdiretorios
    
        O parametro sourceSHA deve ser passado inicialmente como vazio. Se 
        deixar de ser vazio, indicarah que pelo menos um match jah foi 
        encontrado para sourceArray
    
        O parametro dir indica o diretorio de pesquisa
    --------------------------------------------------------------------------*/    
    private static void lookForMatches(
        String sourceSHA,    
        final String[] sourceArray, 
        final File dir
    ) throws IOException, NoSuchAlgorithmException {
        
        /*
        Retorna uma lista com diretorios e apenas arquivos com extensoes 
        elegiveis pelo usuario para serem pesquisados
        */    
        File[] fileList = getFileList(dir);
        
        for (File targetFile : fileList) {
            
            if (targetFile.isFile()) {
                
                String absolutePath = targetFile.getAbsolutePath();
                
                /*Um arquivo nao pode dar match com ele proprio*/
                if (sourceArray[0].equals(absolutePath)) continue;
                
                /*
                Se deu match, escreve o pathname do arquivo (armazenado na
                posicao 0 do array sourceArray. 
                */
                if (isMatch(sourceArray, absolutePath)) {
                    
                    /*
                    O pathname do arquivo associado ao sourceArray soh eh
                    escrito na 1a vez que der match. Nesse bloco a variavel
                    sourceSHA (passada inicialmente como vazia) deixarah de ser
                    vazia e isso impedirah que seja escrito novamente
                    */
                    if (sourceSHA.isEmpty()) {
                        
                        System.out.println(
                            "\nMatches para \"" + sourceArray[0] + "\" : \n"
                        );
                        
                        sourceSHA = getSHA256(sourceArray[0]);
                        
                    }//if (sourceSHA.isEmpty())
                    
                    /*
                    Verifica ser os dois arquivos que deram match possuem tb
                    conteudos identicos. Se checkSha foi selecionado FALSE pelo 
                    usuario, este teste eh pulado
                    */
                    if (checkSha) {                  

                        String targetSHA = getSHA256(absolutePath);

                        if (sourceSHA.equals(targetSHA)) 

                            System.out.print(" [=] ");

                        else 

                            System.out.print("[<>] ");   
                        
                    }//if (checkSha)
                    
                    //Imprime o nome do arquivo que deu match
                    System.out.println(absolutePath);  
                    
                }// if (isMatch(sourceArray, absolutePath))
                
            } else {
                
               /*Se file eh subdiretorio, o metodo se chama recursivamente
                para continuar a pesquisa neste subdiretorio*/               
                lookForMatches(sourceSHA, sourceArray, targetFile);
                
            }//fim do if-else            
        }
        
    }//lookForMatches

    /*-------------------------------------------------------------------------
                    Metodo que inicia a execucao do programa       
    --------------------------------------------------------------------------*/
    public static void main(String[] args) throws NoSuchAlgorithmException {
        
       try {
           
            //Verifica se o programa foi chamado com o flag -sha
            if ((args.length > 0) && (args[0].equals("-sha"))) checkSha = true;
      
            //Le as entradas do usuario
            readInputs();
        
            /*Obtem uma lista (PATHNAMES_LIST) com os nomes de todos os arquivos 
            validos no diretorio corrente e nos seus subdiretorios. Arquivos 
            validos serao aqueles que tiverem extensoes pertencentes ao conjunto
            de extensoes validas definidas pelo usuario
            */        
            getPathnames(searchDir);
              
            /*
            Percorre todos os arquivos em PATHNAMES_LIST e compara o nome de 
            cada arquivo com os nomes de todos os outros arquivos validos 
            no diretorio corrente e seus subdiretorios
            */
            for (String absolutePath: PATHNAMES_LIST) {
                
                String[] sourceArray = getTokens(absolutePath);

                lookForMatches("", sourceArray, searchDir);
            }
            
            /*
            Apresenta uma listagem de todas as strings que produziram matches,
            cada uma delas com os respectivos arquivos que possuem esta string
            no nome
            */
            listMatches();
        
            /*
            Se as saidas foram redirecionadas para um arquivo, este arquivo 
            agora serah fechado
            */
            if (outputStream != null) outputStream.close();  
        }
        catch(IOException e) {
            
            System.err.println("\n" + e.getMessage());
            System.exit(0);
        }  
      
    }//main
    
}//classe FilenameMatch