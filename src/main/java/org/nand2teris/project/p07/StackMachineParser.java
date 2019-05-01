package org.nand2teris.project.p07;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StackMachineParser implements Parser {

    private BufferedReader reader;
    private String currentCommand;
    private String nextCommand;

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
    }

    private String readNext() throws IOException {
        String line = reader.readLine();
        if (line != null && (line.trim().startsWith("//")|| line.trim().length() == 0 )) return readNext();
        else return line;
    }

    @Override
    public CommandType commandType() {
        if (currentCommand == null) return null;
        String command = currentCommand.split(" ")[0].trim();
        switch (command) {
            case "if-go":
                return CommandType.IF;
            case "goto":
                return CommandType.GOTO;
            case "label":
                return CommandType.LABEL;
            case "function":
                return CommandType.FUNCTION;
            case "call":
                return CommandType.CALL;
            case "pop":
                return CommandType.POP;
            case "push":
                return CommandType.PUSH;
            default:
                return CommandType.ARITHMETIC;
        }
    }

    @Override
    public Instruction instruction() {
        if (currentCommand == null) return null;
        String[] tokens = currentCommand.split(" ");
        if (tokens[0].equals("push") || tokens[0].equals("pop"))
            return new Instruction(tokens[0], tokens[1], Integer.parseInt(tokens[2]));
        else return new Instruction(tokens[0]);
    }
}
