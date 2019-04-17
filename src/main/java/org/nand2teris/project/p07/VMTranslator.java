package org.nand2teris.project.p07;

import java.io.IOException;

public class VMTranslator {

    public static void main(String[]args) throws IOException {
        //read file
        //pare file
        //loop through and generate code

        Parser parser = new StackMachineParser(args[0]);
        CodeWriter writer = new HackAssemblyWriter(args[1]);

        while(parser.hasMoreCommands()){
            parser.next();
            if(parser.commandType() == CommandType.ARITHMETIC){
                writer.writeArithmetic(parser.instruction().command);
            }else if(parser.commandType() == CommandType.PUSH){
                writer.writePushPop(CommandType.PUSH, parser.instruction().memorySegment, parser.instruction().index);
            }else if(parser.commandType() == CommandType.POP){
                writer.writePushPop(CommandType.POP, parser.instruction().memorySegment, parser.instruction().index);
            }
        }

        writer.close();
    }
}

