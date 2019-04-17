package org.nand2teris.project.p07;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.nand2teris.project.p07.Symbol.*;

public class HackAssemblyWriter implements CodeWriter {
    private PrintWriter writer;

    public HackAssemblyWriter(String outFilePath) throws IOException {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outFilePath)));
    }

    @Override
    public void writeArithmetic(String command) throws IOException {
        switch (command){
            case "add": add();break;
            case "sub": sub();break;
        }
    }

    private void add() throws IOException {
        writer.println("@SP");
        writer.println("M=M-1");
        writer.println("A=M");
        writer.println("D=M");
        writer.println("@SP");
        writer.println("M=M-1");
        writer.println("A=M");
        writer.println("M=M+D");
        writer.println("@SP");
        writer.println("M=M+1");
    }

    private void sub() throws IOException{
        writer.println("@SP");
        writer.println("M=M-1");
        writer.println("A=M");
        writer.println("D=M");
        writer.println("@SP");
        writer.println("M=M-1");
        writer.println("A=M");
        writer.println("M=M-D");
        writer.println("@SP");
        writer.println("M=M+1");
    }


    @Override
    public void writePushPop(CommandType pushOrPop, String segment, int index) throws IOException {
        //for now support only
        if(segment.equals("constant")){
            writer.println("@" + index);
            writer.println("D=A");
            writer.println("@SP");
            writer.println(("A=M"));
            writer.println("M=D");
            writer.println("@SP");
            writer.println("M=M+1");
        }else if(pushOrPop == CommandType.PUSH){
            writePush(convert(segment), index);
        }else if(pushOrPop == CommandType.POP){
            writePop(convert(segment), index);
        }
    }

    @Override
    public void close() {
        writer.close();
    }

    private void writePush(Symbol segment, int index) throws IOException {
        writer.println("//PUSH to " + segment.name() + " index = " + index);
        writer.println("@" + index);
        writer.println("D=A");
        assignAddressToDRegistor(segment);
        writer.println("A=D");
        writer.println("D=M");
        writer.println("@SP");
        writer.println("A=M");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("M=M+1");
    }

    private void assignAddressToDRegistor(Symbol segment) {
        if(segment == TEMP){
            writer.println("@5");
            writer.println("D=A+D");
        }else{
            writer.println("@"+segment);
            writer.println("D=M+D");
        }
    }

    private void writePop(Symbol segment, int index){
        writer.println("//POP to " + segment.name() + " index = " + index);
        writer.println("@" + index);
        writer.println("D=A");
        assignAddressToDRegistor(segment);
        writer.println("@R13");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("M=M-1");
        writer.println("A=M");
        writer.println("D=M");
        writer.println("@R13");
        writer.println("A=M");
        writer.println("M=D");
    }

    private Symbol convert(String segment){
        switch (segment.trim()){
            case "local": return LCL;
            case "argument": return ARG;
            case "this": return THIS;
            case "that": return THAT;
            case "temp": return TEMP;
            default: return TEMP;
        }
    }
}
