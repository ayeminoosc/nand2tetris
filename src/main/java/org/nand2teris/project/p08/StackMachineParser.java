package org.nand2teris.project.p08;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class StackMachineParser implements Parser {

    private BufferedReader reader;
    private String currentCommand;
    private String nextCommand;
    private Instruction inst;

    public StackMachineParser(String filePath) throws IOException {
        reader = Files.newBufferedReader(Paths.get(filePath));
        next();
    }

    @Override
    public boolean hasMoreCommands() {
        return nextCommand != null;
    }

    @Override
    public void next() throws IOException {
        currentCommand = nextCommand;
        nextCommand = readNext();
        calculateInstruction();
    }

    private String readNext() throws IOException {
        String line = reader.readLine();
        if (line != null && (line.trim().startsWith("//")|| line.trim().length() == 0 )) return readNext();
        return line == null ? line : line.replaceFirst("//.*", "").trim();
    }

    @Override
    public CommandType commandType() {
        if (currentCommand == null) return null;
        String command = currentCommand.split(" ")[0].trim();
        switch (command) {
            case "if-goto":
            case "goto":
            case "label":
                return CommandType.BRANCHING;
            case "function":
            case "call":
            case "return":
                return CommandType.FUNCTION;
            case "pop":
            case "push":
                return CommandType.MEMORY_ACCESS;
            default:
                return CommandType.ARITHMETIC_OR_LOGICAL;
        }
    }

    @Override
    public Instruction instruction() {
        return inst;
    }

    private void calculateInstruction(){
        if (currentCommand == null) return;
        String[] tokens = currentCommand.split(" ");
        if (tokens[0].equals("push") || tokens[0].equals("pop") ||
                tokens[0].equals("label") || tokens[0].equals("goto") || tokens[0].equals("if-goto")){
            inst =  new Instruction(tokens[0], Arrays.copyOfRange(tokens, 1, tokens.length ));
        } else {
            inst = new Instruction(tokens[0]);
        }
    }
}
