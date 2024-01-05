import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.PatternSyntaxException;

/*******************************************************************************
 * Uma aplicacao que encontra e lista nomes de arquivos com substrings 
 * coincidentes.
 * 
 * @since 2.0 4 de janeiro de 2024
 * @version 2.0
 * @author Pedro Reis
 ******************************************************************************/
public final class  FilenameMatch {
    
    /*
    Essa estrutura de dados irah armazenar em uma lista todos os pathnames dos 
    arquivos a serem testados no diretorio de varredura e de seus subdiretorios
    (caso a opcao de varredura de subdiretorios seja selecionada pelo usuario)
    */
    private static final List<Pathname> PATHNAMES_LIST = new LinkedList<>();
    
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
    private static String fileExtensions;
    
    /*
    O diretorio a partir do qual serah feita a varredura
    */
    private static File rootSearchDir;
    
    /*
    Nome do arquivo de saida default
    */
    private static final String OUTPUT_FILENAME = "lista.txt";
     
    /*
    Fluxo para onde redirecionar a saida padrao de texto
    */
    private static PrintStream outputStream;
    
    /*
    Escreve no terminal mesmo quando o objeto de saida padrao tiver sido 
    redirecionado para um arquivo
    */
    private static final PrintStream CONSOLE = System.out;
   
    /*
    Define o numero minimo de tokens para match entre dois nomes de arquivos
    */
    private static int matchLength;
    
    /*
    Define o comprimento minimo para tokens (em caracteres)
    */
    private static int minimumTokenLength;
     
    /*
    Determina se o conteudo dos arquivos que dao match serao ou nao comparados
    para ver se sao iguais. O flag -sha, que deve ser passado como argumento ao
    executar o programa, habilita esta checagem
    */
    private static boolean checkSha = false;
    
    /*
    Determina se o progorama fara ou nao uma pesquisa exaustiva por substrings
    coincidentes entre nomes de arquivos. O flag -full, que deve ser passado 
    como argumento ao executar o programa, habilita a pesquisa exaustiva
    */
    private static boolean shortSearch = true;
    
    /*
    Deternina se os subdiretorios do diretorio de varredura tambem serao 
    pesquisados
    */
    private static boolean includeSubdirs;
    
    /*
    O num. de diretorios pesquisados
    */
    private static int numberOfDirsSearched = 1;
    
    /*
    Uma regex para filtrar nomes de arquivos
    */
    private static String filenameFilterRegex;
        
    /*-------------------------------------------------------------------------
          Retorna uma String contendo o caractere c repetido length vezes   
    --------------------------------------------------------------------------*/  
    private static String repeatChar(final char c, final int length) {
        
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++) s.append(c);
        return s.toString();
        
    }//repeatChar
    
    /*-------------------------------------------------------------------------
                    Processa os prompts de entrada do usuario
    --------------------------------------------------------------------------*/      
    @SuppressWarnings("null")
    private static void readInputs() throws IOException {
        
        InputReader inputReader;//Objeto para ler as entradas        
        
        //O diretorio a partir do qual iniciar a varredura----------------------
        
        inputReader = new InputReader(
            "Diret\u00f3rio de varredura",
            "Diret\u00f3rio corrente",
            ".",
             new RootSearchDirParser()
        );
        
        rootSearchDir = new File(inputReader.readInput());
        
        //----------------------------------------------------------------------
        
        
        //Define se a pesquisa deve se extender aos subdiretorios---------------
        
        inputReader = new InputReader(
            "Pesquisar subdiret\u00f3rios? (S/n)",
            "N\u00e3o",
            "n",
             new IncludeSubdirParser()
        );
        
        includeSubdirs = inputReader.readInput().equals("s"); 
        
        //----------------------------------------------------------------------
        
        
        //O numero minimo de palavras para ser considerado match----------------
        
        inputReader = new InputReader(
            "M\u00ednimo de palavras para dar match",
            "3",
            "3",
             new MatchLengthParser()
        );
        
        matchLength = Integer.parseInt(inputReader.readInput()); 
        
        //----------------------------------------------------------------------
        
        
        //O numero minimo de caracteres para uma palavra ser token--------------
        
        inputReader = new InputReader(
            "M\u00ednimo de caracteres que deve ter um token",
            "2",
            "2",
             new MinimumTokenLengthParser()
        );
        
        minimumTokenLength = Integer.parseInt(inputReader.readInput()); 
        
        //----------------------------------------------------------------------
        
        
        //Define o arquivo de saida---------------------------------------------
        
        inputReader = new InputReader(
            "Arquivo de sa\u00edda (pode incluir caminho relativo ou absoluto)",
            OUTPUT_FILENAME + " , //console = Sa\u00edda no terminal",
            OUTPUT_FILENAME,
            new OutputFileParser()
        );
        
        File outputFile = null;
        
        String outputFilename = inputReader.readInput();
        
        if (outputFilename != null) outputFile = new File(outputFilename);
        
        //----------------------------------------------------------------------
        
        
        //Define uma expressao regular para filtrar nomes de arquivos-----------
        
        inputReader = new InputReader(
            "Defina uma regex para filtrar nomes de arquivos",
            "Qualquer arquivo",
             null,
             new FilenameFilterRegexParser()
        );
        
        filenameFilterRegex = inputReader.readInput();
        
        //----------------------------------------------------------------------
        
        
        //Define que tipos de arquivos podem ser pesquisados--------------------
        
        inputReader = new InputReader(
            "Extens\u00f5es de arquivos a serem pesquisados " +
             "(sem ponto e separadas por espa\u00e7o)",
            "Qualquer arquivo",
             null,
             new FileExtensionsParser()
        );
               
        fileExtensions = inputReader.readInput();

        if (fileExtensions != null) 
            fileExtensions = fileExtensions.toLowerCase() + " ";
        
        //----------------------------------------------------------------------
        
           
        //Se foi selecionado um outputFile, entao as saidas sao redirecionadas a
        //este arquivo
        if (outputFile != null) {
            
            //Eh criado um objeto de fluxo que envia a saida padaro para 
            //o outputFile
            outputStream = new PrintStream(new FileOutputStream(outputFile));
            System.setOut(outputStream); 
      
        }
        else
       //Senao outputStream = null para sinalizar para o programa que a saida
       //padrao esta sendo enviada para o terminal
            outputStream = null;
         
    }//readInputs 
    
   /*--------------------------------------------------------------------------
       Extrai para um array todos os tokens (palavras) de um nome de arquivo
    --------------------------------------------------------------------------*/
    private static String[] getTokens(final Pathname pathname) {
        
        StringTokenizer tokenizer = 
            new StringTokenizer(pathname.getName(), TOKENS_DELIMITERS);
        
        String[] tokensArray = new String[tokenizer.countTokens()];
        
        int countTokens = 0;
        
        while (tokenizer.hasMoreTokens()) {
              
            tokensArray[countTokens++] = tokenizer.nextToken(); 
        }

        return tokensArray;
        
    }//getTokens   
    
    /*-------------------------------------------------------------------------
          Retorna a sequencia de tokens (em um array de tokens) a partir do
          indice startIndex e de comprimento length, ou seja, com length
          tokens. Os tokens na sequencia retornada terao o tracinho (-) como
          separadores
    --------------------------------------------------------------------------*/  
    private static String getTokensSequence(
        final String[] tokens,
        final int startIndex,
        final int length
    ) {
        
        StringBuilder tokensSequence = new StringBuilder();
        
        int lastIndex = startIndex + length - 1;
        
        for (int i = startIndex; i <= lastIndex ; i++) 
            tokensSequence.append(tokens[i]).append(i == lastIndex ? "" : "-");
        
        return tokensSequence.toString();
        
    }//getTokensSequence
    
    /*-------------------------------------------------------------------------
            Lista todos os arquivos com um determinado match no nome
    --------------------------------------------------------------------------*/  
    private static void listMatches() {
       
       for (String match : MATCHES_SET) {
           
           System.out.println("\nNomes de arquivos com \"" + match + "\" :\n");
           
           for (Pathname pathname : PATHNAMES_LIST) {
               
               String[] tokens = getTokens(pathname);
               
               String tokensSequence = 
                    getTokensSequence(tokens, 0, tokens.length);
               
               if (tokensSequence.contains(match))
                   System.out.println(pathname);
               
            }//for
           
        }//for  
       
    }//listMatches
    
    /*-------------------------------------------------------------------------
                 Retorna o HASH sha256 do conteudo de um arquivo
    --------------------------------------------------------------------------*/      
    private static String getSHA256(final Pathname pathname) 
        throws IOException, NoSuchAlgorithmException {
        
        String sourceSha256 = pathname.getSha();
        
        if (sourceSha256 != null) return sourceSha256;
        
        String absolutePath = pathname.toString();        
        
        File file = new File(absolutePath);
        
        byte[] sha256;

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        
        /*Arquivo menor que 100MB le direto para array de byte*/
        if (file.length() < 100000000) {
            
            sha256 =
                messageDigest.digest(
                    Files.readAllBytes(
                        Paths.get(absolutePath)
                    )
                );
            
        }
        else {/*Maior que 100MB le blocos de 80MB de cada vez*/
            
            try 
               (FileInputStream inputFile = new FileInputStream(absolutePath)) {
                
                byte[] dataBytes = new byte[4194304];
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
        
        for (byte b : sha256) hexString.append(String.format("%02X", b));
        
        sourceSha256 = hexString.toString();
        
        /*
        Salva o hash deste pathname pra nao ter que recalcula-lo
        */
        pathname.setSha(sourceSha256);
        
        return sourceSha256;
        
    }//getSHA256    
    
    /*-------------------------------------------------------------------------
     Escreve no terminal apenas quando as saidas foram direcionadas ao arquivo        
    --------------------------------------------------------------------------*/      
    private static void writeToConsole(final String s) {
        
        if (outputStream != null) CONSOLE.print(s); 
        
    }//writeToConsole
    
    /*-------------------------------------------------------------------------
          Um metodo recursivo que insere na estrutura PATHNAMES_LIST os
          pathnames de todos os arquivos validos no diretorio dir e seus 
          subdiretorios          
    --------------------------------------------------------------------------*/    
    private static void getPathnames(final File dir)
        throws IOException, NoSuchAlgorithmException {
        
        int pathId = numberOfDirsSearched;
        Pathname.map(pathId, dir.getAbsolutePath());
        
        /*
        O metodo retorna apenas arquivos cujas extensoes sao elegiveis para
        pesquisa
        */
        File[] fileList = dir.listFiles(new FilesFilter());
        
        writeToConsole("Pesquisando em " + dir + " ...\n");
        
        for (File file : fileList) {
            
            if (file.isFile()) {
                        
                /*Se file eh arquivo, insere seu nome na lista*/                
                PATHNAMES_LIST.add(new Pathname(pathId, file));
                
            } else {
                
                /*Se file eh subdiretorio, o metodo se chama recursivamente
                para continuar a pesquisa neste subdiretorio*/ 
                numberOfDirsSearched++;
                getPathnames(file);                
            }            
        }        
       
    }//getPathnames
    
    /*-------------------------------------------------------------------------
          Verifica se ha match entre a string source (jah convertida para
          array de tokens) e a string target        
    --------------------------------------------------------------------------*/        
    private static boolean isMatch(
        final Pathname sourcePathname,
        final Pathname targetPathname
    ) {
        
        boolean isMatch = false;

        String[] sourceArray = getTokens(sourcePathname);
        String[] targetArray = getTokens(targetPathname); 
        
        int lastSrcIndex = sourceArray.length - matchLength;
        
        int lastTrgtIndex = targetArray.length - matchLength;
        
        for (int srcIndex = 0; srcIndex <= lastSrcIndex; srcIndex++) {
                    
            for (int trgtIndex = 0; trgtIndex <= lastTrgtIndex; trgtIndex++) {
                
                int matchCounter = 0;
                
                for (int i = 0; i < matchLength; i++) {
                    
                    if (
                        sourceArray[srcIndex+i].length() >= minimumTokenLength                    
                                          &&
                        sourceArray[srcIndex+i].equals(targetArray[trgtIndex+i])
                    )
                        matchCounter++;
                    else
                        break;
                }//for i
                
                if (matchCounter == matchLength) {
                                  
                    MATCHES_SET.add(
                        getTokensSequence(
                            sourceArray,
                            srcIndex, 
                            matchCounter
                        )
                    );  
                    
                    if (shortSearch) return true;
                    
                    isMatch = true;

                }//if
                   
            }//for sourceIndex    
            
        }//for targetIndex
        
        return isMatch;
        
    }//isMatch
        
    /*-------------------------------------------------------------------------
        Recebe o array com os tokens de um nome de arquivo e compara com os
        nomes de todos os arquivos localizados
    --------------------------------------------------------------------------*/  
    private static void lookForMatches(
        final Pathname sourcePathname,
        final Iterator<Pathname> iterator
    ) 
        throws IOException, NoSuchAlgorithmException {
        
        String sourceSHA = null;
        
        List<PreviousMatchInfo> matchesList = sourcePathname.getList();
        
        for (PreviousMatchInfo matchInfo: matchesList) {
            
            if (sourceSHA == null) {

                System.out.println(
                    "\nMatches para \"" + sourcePathname + "\" : \n"
                );
                
                if (checkSha) 
                    sourceSHA = getSHA256(sourcePathname);
                else
                    sourceSHA = "#";

            }//if (printlnMatchesPara)
            
            if (checkSha) { 
                
                if (matchInfo.isSameContent()) 
                
                    System.out.print(" [=] ");

                else

                    System.out.print("[<>] ");
            } 
            else
                System.out.print(" [?] ");
            
            System.out.println(matchInfo.getPathname());            
            
        }
        
        while (iterator.hasNext()) {
            
            Pathname targetPathname = iterator.next();
              
            /*
            Se deu match, escreve o pathname do arquivo (armazenado na
            posicao 0 do array sourceArray. 
            */
            if (isMatch(sourcePathname, targetPathname)) {
                
                /*
                O pathname do arquivo associado ao sourceArray soh eh
                escrito na 1a vez que der match. 
                */
                if (sourceSHA == null) {

                    System.out.println(
                        "\nMatches para \"" + sourcePathname + "\" : \n"
                    );
                    
                if (checkSha) 
                    sourceSHA = getSHA256(sourcePathname);
                else
                    sourceSHA = "#";
                    
   
                }//if (printlnMatchesPara)

                /*
                Verifica se os dois arquivos que deram match possuem tb
                conteudos identicos. Se checkSha foi selecionado false pelo 
                usuario, este teste eh pulado
                */
                boolean isSameContent = false;
                if (checkSha) {  
                    
                    String targetSHA = getSHA256(targetPathname);
                    
                    isSameContent = (sourceSHA.equals(targetSHA)); 
                    
                    if (isSameContent)

                        System.out.print(" [=] ");

                    else 

                        System.out.print("[<>] ");   

                }//if (checkSha)
                else
                    System.out.print(" [?] ");               
                
                targetPathname.addMatch(sourcePathname, isSameContent);               

                //Imprime o nome do arquivo que deu match
                System.out.println(targetPathname);  

            }// if (isMatch(sourceArray, absolutePath)) 
            
        }//for
        
    }//lookForMatches

    /*-------------------------------------------------------------------------
                    Metodo que inicia a execucao do programa       
    --------------------------------------------------------------------------*/
    public static void main(String[] args) throws NoSuchAlgorithmException {
          
       try {
            
            for (String arg : args) {
                
                if (arg.equals("-sha")) checkSha = true;
                if (arg.equals("-full")) shortSearch = false;
            }
            
            //Le as entradas do usuario
            readInputs();
            
            //Marca a hora (em milisegundos) do inicio da execucao do processo
            long start = System.currentTimeMillis();
            
            /*Obtem uma lista (PATHNAMES_LIST) com os nomes de todos os arquivos 
            validos no diretorio corrente e nos seus subdiretorios (caso esta
            opcao esteja habilitada). Arquivos validos serao aqueles que tiverem 
            extensoes pertencentes ao conjunto de extensoes validas definidas 
            pelo usuario
            */   
            writeToConsole("\n"); getPathnames(rootSearchDir);
            
            //Num. de arqs. que serao processados
            int pathnamesListSize = PATHNAMES_LIST.size();
            
            if (pathnamesListSize < 2) {
                writeToConsole("\nSem arquivos para comparar\n");
                System.exit(0);
            }
            
            writeToConsole(
                String.format(
                    "\n%,d arquivos localizados em %,d diret\u00f3rio" +
                    (numberOfDirsSearched > 1 ? "s pesquisados" : " pesquisado")
                    + "\n", 
                    pathnamesListSize, 
                    numberOfDirsSearched
                )
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
                (pathnamesListSize % --barLength) >= 
                (filesPerDot = pathnamesListSize / barLength)
            );
                   
            //Conta o num. de arqs. jah processados
            int countFiles = 0; 
            
            //Imprime a barra de progresso
            writeToConsole("\n0%|" + repeatChar(' ', barLength) + "|100%\n   "); 
            
            Iterator<Pathname> i; 
              
            /*
            Percorre todos os arquivos em PATHNAMES_LIST e compara o nome de 
            cada arquivo com os nomes de todos os outros arquivos validos 
            no diretorio corrente e seus subdiretorios (caso includeSubdirs
            esteja true)
            */
            for (Pathname pathname: PATHNAMES_LIST) {
                 
                //A cada countFiles arqs. processados, um ponto eh impresso na 
                //barra de progresso
                if (++countFiles % filesPerDot == 0) writeToConsole(".");  
                
                i = PATHNAMES_LIST.listIterator(countFiles);
                
                lookForMatches(pathname, i);
                
            }//for
            
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
            if (outputStream != null) {
                
                outputStream.close();
                System.setOut(CONSOLE);
            } 

            int seconds = (int)(System.currentTimeMillis() - start) / 1000;
            int hours = seconds / 3600;
            seconds = seconds % 3600;  
            int minutes = seconds / 60;
            seconds = seconds % 60;
             
            System.out.printf(
                "\n\nFeito! [%dh:%dm:%ds]\n", 
                hours, minutes, seconds
            );
        }
        catch(IOException e) {
            
            System.err.println("\n" + e.getMessage());
            System.exit(0);
        }
      
    }//main
    
/*=============================================================================
                 Classes privadas da classe FilenameMatch
==============================================================================*/    
private final static class RootSearchDirParser implements InputParser {
    
    @Override
    public String parse(final String input) throws IllegalArgumentException {
        
        String parsedInput = input;
        File testInput;        
        
        testInput = new File(parsedInput);           

        //Lanca excecao se nao selecionou diretorio existente
        if (!testInput.isDirectory()) 
           throw new IllegalArgumentException(
               "N\u00e3o \u00e9 um diret\u00f3rio"
        );

        //Lanca excecao se usuario nao tem direito de leitura
        if (!testInput.canRead()) 
           throw new IllegalArgumentException(
               "Imposs\u00edvel ler neste diret\u00f3rio"
        );

        return parsedInput;
    }
    
}//classe privada RootSearchDirParser ------------------------------------------



private final static class IncludeSubdirParser implements InputParser {
    
    @Override
    public String parse(final String input) throws IllegalArgumentException {
        
        String parsedInput = input;
        
        switch (parsedInput.toLowerCase()) {
            case "s":
            case "sim":
                return "s";
            case "n":
            case "n\u00e3o":
                return "n";
            default:
                throw new IllegalArgumentException("Entrada inv\u00e1lida!");
        }
           
    }//parse
    
}//classe privada IncludeSubdirParser ------------------------------------------



private final static class MatchLengthParser implements InputParser {
    
    @Override
    public String parse(final String input) throws IllegalArgumentException {
        
        String parsedInput = input;
        int matchLength = -1;
        
        try {
            matchLength = Integer.parseInt(parsedInput);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Digite valor num\u00e9rico v\u00e1lido"
                );            
        }
        
        if (matchLength < 1) 
            throw new IllegalArgumentException(
                "Valor deve ser maior ou igual a 1"
            );
        
        return parsedInput;
           
    }//parse
    
}//classe privada MatchLengthParser --------------------------------------------



private final static class MinimumTokenLengthParser implements InputParser {
    
    @Override
    public String parse(final String input) throws IllegalArgumentException {
        
        String parsedInput = input;
        int minimumTokenLength = -1;
        
        try {
            minimumTokenLength = Integer.parseInt(parsedInput);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Digite valor num\u00e9rico v\u00e1lido"
                );            
        }
        
        if (minimumTokenLength < 1) 
            throw new IllegalArgumentException(
                "Valor deve ser maior ou igual a 1"
            );
        
        return parsedInput;
           
    }//parse
    
}//classe privada MinimumTokenLengthParser -------------------------------------



private final static class OutputFileParser implements InputParser {
    
    @Override
    public String parse(final String input) throws IllegalArgumentException {
        
        String parsedInput = input;
        
        if (parsedInput.equals("//console")) return null;
        
        File testFile = new File(parsedInput);
                
        //Se o arquivo selecionado jah existir, este serah apagado
        if (testFile.exists()) testFile.delete();

        try {
            
            if (!testFile.createNewFile()) 
                throw new IllegalArgumentException(
                    "Imposs\u00edvel criar arquivo"
                );    
        }
        catch (IOException e) {
            
            throw new IllegalArgumentException("Imposs\u00edvel criar arquivo");    
        }
        
        return parsedInput;
           
    }//parse
    
}//classe privada OutputFileParser ---------------------------------------------



private final static class FilenameFilterRegexParser implements InputParser {
    
    @Override
    public String parse(final String input) throws IllegalArgumentException {
        
        String parsedInput = input;
        
        try {
     
            if ("teste".matches(parsedInput));

        }
        catch(PatternSyntaxException e) {
            
            throw new IllegalArgumentException(
                "Erro de sintaxe na posi\u00e7\u00e3o " + 
                (e.getIndex() + 1) + 
                ":\n" + parsedInput + "\n" +
                 repeatChar(' ', e.getIndex()) + '^'                           
            );              

        }
        
        return parsedInput;
           
    }//parse
    
}//classe privada FilenameFilterRegexParser ------------------------------------



private final static class FileExtensionsParser implements InputParser {
    
    @Override
    public String parse(final String input) throws IllegalArgumentException {
        
        String parsedInput = input;
        
        return parsedInput;
           
    }//parse
    
}//classe privada FileExtensionsParser -----------------------------------------

  
private final static class FilesFilter implements FileFilter {
    
    /*--------------------------------------------------------------------------
      Este metodo testa um arquivo ou diretorio e decide se serah
      ou nao aceito
    --------------------------------------------------------------------------*/
    @Override
    public boolean accept(File file) {

        //**** file pode ser arq. ou diretorio ****


        boolean isDir = file.isDirectory();

        if (isDir && !includeSubdirs) return false; 

        if (isDir) return true;
        
     
        //**** Se executou ateh aqui eh arquivo ****


        String filename = file.getName();
        
        int p = filename.lastIndexOf(".");
        
        if (p > 0) filename = filename.substring(0, p);        
        

        //Usuario definiu regex mas esta nao casa com nome do arq.
        if (
            filenameFilterRegex != null && !filename.matches(filenameFilterRegex)
        )
            return false;


        //**** Eh arq. que corresponde a regex ou regex == null ****


        //Usuario escolheu aceitar arquivo com qualquer extensao            
        if (fileExtensions == null) return true;


        //**** Ainda precisa verificar a extensao do arq. ****


        int i = filename.lastIndexOf('.'); 
        if (i == 0) return false;//nao eh extensao, eh nome do arq.

        String ext = filename.substring(i + 1) + " ";  

        //Retorna se a ext. do arq. pertence ou nao ao conj. de 
        //extensoes selecionado
        return (fileExtensions.contains(ext.toLowerCase()));                      

    }//accept    
    
}//classe privada FilesFilter---------------------------------------------------

    
}//classe FilenameMatch