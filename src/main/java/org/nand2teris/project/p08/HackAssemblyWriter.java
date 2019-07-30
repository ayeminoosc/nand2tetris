package org.nand2teris.project.p08;

import java.io.*;

import static org.nand2teris.project.p08.Symbol.*;

public class HackAssemblyWriter implements CodeWriter {
    private PrintWriter writer;
    private String fileName;

    public HackAssemblyWriter(String outFilePath) throws IOException {
        writer = new PrintWriter(new BufferedWriter(new FileWriter(outFilePath)));
        fileName = outFilePath.substring(outFilePath.lastIndexOf(File.separator) + 1);
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
    }

    @Override
    public void writeArithmeticCommand(String command) throws IOException {
        switch (command) {
            case "add":
                add();
                break;
            case "sub":
                sub();
                break;
            case "neg":
                neg();
                break;
            case "and":
            case "or":
                andOr(command);
                break;
            case "not":
                not();
                break;
            default:
                logicalOperator(command);
                break;
        }
    }


    @Override
    public void writeMemoryAccessCommand(String pushOrPop, String segment, int index) throws IOException {
        if (pushOrPop.equals("push")) {
            pushSegment(convert(segment), index);
        } else if (pushOrPop.equals("pop")) {
            popSegment(convert(segment), index);
        }
    }

    @Override
    public void writeBranchingCommand(String command, String label){
        if(command.equals("label")){
            writeLabel(label);
        }else if(command.equals("goto")){
            writeGoto(label);
        }else if(command.equals("if-goto")){
            writeIfGoto(label);
        }
    }

    @Override
    public void writeFunctionCommand(String command, String funcName, String num) {
        if(command.equals("function")) {
            writeFunctionDeclare(funcName, Integer.parseInt(num));
        }else if(command.equals("call")){
            writeFunctionCall(funcName, Integer.parseInt(num));
        }else if(command.equals("return")){
            writeFunctionReturn();
        }
    }

    @Override
    public void close() {
        writer.close();
    }

    /**
     * VM implementation for Function Declare
     * 1. mark label for function entry
     * 2. Set up the local segment of the called function
     * 3. init local args to 0
     * @param funcName
     * @param numberOfLocalVar
     */
    private void writeFunctionDeclare(String funcName, int numberOfLocalVar) {
        // 1. mark lable for function entry
        writer.println("(" + funcName + ")");

        // 2. Set up the local segment of the called function
        writer.println("@SP");
        writer.println("D=A");
        writer.println("@LCL");
        writer.println("M=D");

        // 3. init local args to 0
        for(int i=0 ;i< numberOfLocalVar; i++){
            pushSegment(CONST, 0);
        }

    }

    /**
     *  VM implementation for Function call
     *  1. Sets arg
     *  2. Saves the caller's frame (return_address, LCL, ARG, THIS, THAT)
     *  3. Jump to execute callee
     * @param funcName
     * @param numberOfArgs
     */
    private void writeFunctionCall(String funcName, int numberOfArgs){
        String returnAddress = randomLabel();
        //  1. setting up args
        writer.println("@SP");
        writer.println("D=M");
        writer.println("@ARG");
        writer.println("M=D-"+ numberOfArgs);

        // 2. save the caller's frame

        // 2.1 saving return address
        writer.println("@" + returnAddress);
        writer.println("D=A");
        pushD();

        // 2.2 saving LCL
        writer.println("@LCL");
        writer.println("D=M");
        pushD();

        // 2.3 saving ARG
        writer.println("@ARG");
        writer.println("D=M");
        pushD();

        // 2.4 saving THIS
        writer.println("@THIS");
        writer.println("D=M");
        pushD();

        // 2.5 saving THAT
        writer.println("@THAT");
        writer.println("D=M");
        pushD();

        // 3. Jump to execute callee
        writeGoto(funcName);

        writer.println("("+ returnAddress + ")"); //marking return address
    }

    /**
     * VM implementation for Function Return
     * 1. copy the return value onto argument 0
     * 2. restore segment pointers of the caller
     * 3. clear the stack
     * 4. set SP for the caller
     * 5. jump to the return address within the caller's code
     */
    private void writeFunctionReturn(){
        // 1. copy the return value onto argument 0
        popSegment(ARG, 0);

        // 2. restore segment pointers of the caller
        // RAM[SP] = RAM[LCL]
        writer.println("@LCL");
        writer.println("D=M");
        writer.println("@SP");
        writer.println("M=D");
        popSegment(THAT, 0);
        popSegment(THIS, 0);
        popSegment(ARG, 0);
        popSegment(LCL, 0);
        //storing return_address in R13 General purpose register
        popD();
        writer.println("@R13");
        writer.println("M=D");

        // 4. set SP for the caller
        // (RAM[SP] = RAM[LCL])
        writer.println("@ARG");
        writer.println("D=M");
        writer.println("@SP");
        writer.println("M=D");

        // 3. clear the stack
        //stack is auto clear because we move SP pointer

        // 5. jump to return address
        writer.println("@R13");
        writer.println("D=M");
        writer.println("A=D");
        writer.println("0;JEQ");
    }

    private void writeLabel(String label) {
        writer.println("(" + label + ")");
    }

    private void writeGoto(String label) {
        writer.println("@" + label);
        writer.println("0;JMP");
    }

    private void writeIfGoto(String command) {
        popD();
        writer.println("@"+command);
        writer.println("D;JNE");
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
        writer.flush();
        pushD();
    }

    private void logicalOperator(String operator){
        String setTrue = randomLabel();

        popD();
        writer.println("@SP");
        writer.println("M=M-1");
        writer.println("A=M");
        writer.println("D=M-D");

        writer.println("@" + setTrue);

        switch(operator){
            case "eq":
                writer.println("D;JEQ");break;
            case "lt":
                writer.println("D;JLT");break;
            case "gt":
                writer.println("D;JGT");break;
            case "lte":
                writer.println("D;JLE");break;
            case "gte":
                writer.println("D;JGE");break;
        }

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

    private void andOr(String andOr){
        andOr = andOr.equals("and") ? "M=D&M" : "M=D|M";
        popD();
        writer.println(String.join(System.getProperty("line.separator"),
                "@SP",
                "M=M-1",
                "A=M",
                andOr,
                "@SP",
                "M=M+1"
        ));
    }

    private void not(){
        writer.println(String.join(System.getProperty("line.separator"),
                "@SP",
                "M=M-1",
                "A=M",
                "M=!M",
                "@SP",
                "M=M+1"
        ));
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

    private void pushSegment(Symbol segment, int index) {
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
