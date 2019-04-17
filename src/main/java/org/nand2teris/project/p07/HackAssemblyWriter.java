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
