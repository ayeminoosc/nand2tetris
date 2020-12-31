package org.nand2teris.project.p10;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JackAnalyzer {
    private List<Path> filePaths;
    /**
     *
     * @param filePath a single fileName.jack, or a directory containing 0 or more such files
     */
    public JackAnalyzer(String filePath) throws IOException {
        filePaths = new ArrayList<>();
        if(filePath.endsWith(".jack")){
            filePaths.add(Paths.get(filePath));
        }else {
            Files.newDirectoryStream(Paths.get(filePath),
                    path -> path.toString().endsWith(".jack"))
                    .forEach(filePaths:: add);
        }
    }

    /**
     * For each file, goes through the following logic
     * 1. Creates a JackTokenizer  from fileName.jack
     * 2. Creates an output file named fileName.xml and prepare it for writing
     * 3. Creates and use a CompilationEngine to compile the input JackTokenizer into the output file.
     */
    public void analyze() {
        CompilationEngine engine = null;
        for(Path path: filePaths){
            try{
                engine = new CompilationEngine(path.toString(), path.toString()+".xml");
                engine.compileClass();
                engine.flush();
            }catch (Exception e){
                e.printStackTrace();
                if(engine !=null) engine.flush();
            }
        }
    }

    public static void main(String[]args) throws Exception {
        JackAnalyzer analyzer = new JackAnalyzer(args[0]);
        analyzer.analyze();
    }
}
