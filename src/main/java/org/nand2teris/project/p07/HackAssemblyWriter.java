package org.nand2teris.project.p07;

import java.io.*;

import static org.nand2teris.project.p07.Symbol.*;

public class HackAssemblyWriter implements CodeWriter {
    private PrintWriter writer;
    private String fileName;

    public HackAssemblyWriter(String outFilePath) throws IOException {
        writer = new PrintWriter(new BufferedWriter(new FileWriter(outFilePath)));
        fileName = outFilePath.substring(outFilePath.lastIndexOf(File.separator) + 1);
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
    }

    @Override
    public void writeArithmetic(String command) throws IOException {
        switch (command) {
            case "add":
                add();
                break;
            case "sub":
                sub();
                break;
            case "neg":
                neg();
            case "eq":
                eq();
                //todo: add logical commands
        }
    }


    @Override
    public void writePushPop(CommandType pushOrPop, String segment, int index) throws IOException {
        if (pushOrPop == CommandType.PUSH) {
            pushSegment(convert(segment), index);
        } else if (pushOrPop == CommandType.POP) {
            popSegment(convert(segment), index);
        }
    }

    @Override
    public void close() {
        writer.close();
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

    private void sub() throws IOException {
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

    private void neg(){
        popD();
        writer.println("D=-D");
        pushD();
    }

    private void eq(){
        String setTrue = randomLabel();
        popD();
        writer.println("@SP");
        writer.println("M=M-1");
        writer.println("A=M");
        writer.println("D=D-M");

        writer.println("@" + setTrue);
        writer.println("D;JEQ");


        //set false
        writer.println("@SP");
        writer.println("A=M");
        writer.println("M=0");
        writer.println("@SP");
        writer.println("M=M+1");
        writer.println("@E"+setTrue);
        writer.println("0;JEQ");
        //set true
        writer.println("("+setTrue+")");
        writer.println("@SP");
        writer.println("A=M");
        writer.println("M=-1");
        writer.println("@SP");
        writer.println("M=M+1");
        writer.println("(E"+setTrue+")");
    }

    private void gt(){

    }

    private void lt(){

    }

    private void and(){

    }

    private void or(){

    }

    private void not(){

    }

    private void pushStatic(int index) {
        // D = RAM[@name.index]
        writer.println("@" + fileName + "." + index);
        writer.println("D=M");

        pushD();
    }

    private void popStatic(int index) {
        popD();

        // RAM[@name.index] = D
        writer.println("@" + fileName + "." + index);
        writer.println("M=D");
    }

    private void pushD() {
        writer.println(String.join(System.getProperty("line.separator"),
                "@SP",
                "A=M",
                "M=D",
                "@SP",
                "M=M+1"
        ));
    }

    private void pushSegment(Symbol segment, int index) throws IOException {
        switch (segment) {
            case CONST:
                writer.println("@" + index);
                writer.println("D=A");
                pushD();
                break;
            case STATIC:
                pushStatic(index);
                break;
            case POINTER:
                writer.println("@"+ (index == 0 ? "THIS" : "THAT"));
                writer.println("D=M");
                pushD();
                break;
            default:
                storeAddressInD(segment, index);

                //D = RAM[D]
                writer.println("A=D");
                writer.println("D=M");

                pushD();
        }
    }

    private void popSegment(Symbol segment, int index) {
        switch (segment) {
            case STATIC:
                popStatic(index);
                break;
            case POINTER:
                popD();
                writer.println("@"+ (index == 0 ? "THIS" : "THAT"));
                writer.println("M=D");
                break;
            default:
                storeAddressInD(segment, index);

                // RAM[R13] = D
                writer.println("@R13");
                writer.println("M=D");

                popD();

                // *R13 = D
                writer.println("@R13");
                writer.println("A=M");
                writer.println("M=D");
        }
    }

    /**
     * Pop to register D
     */
    private void popD() {
        writer.println("@SP");
        writer.println("M=M-1");
        writer.println("A=M");
        writer.println("D=M");
    }

    private void storeAddressInD(Symbol segment, int index) {
        writer.println("@" + index);
        writer.println("D=A");
        if (segment == TEMP) {
            writer.println("@5");
            writer.println("D=A+D");
        } else {
            writer.println("@" + segment);
            writer.println("D=M+D");
        }
    }

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private String randomLabel(){
        return randomAlphaNumeric(5);
    }
    private String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    private Symbol convert(String segment) {
        switch (segment.trim()) {
            case "local":
                return LCL;
            case "argument":
                return ARG;
            case "this":
                return THIS;
            case "that":
                return THAT;
            case "constant":
                return CONST;
            case "static":
                return STATIC;
            case "pointer":
                return POINTER;
            case "temp":
            default:
                return TEMP;
        }
    }
}
