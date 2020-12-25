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
        writer.println("// "+ command);
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
        writer.println("// " +pushOrPop + " " + segment + " " + index);
        if (pushOrPop.equals("push")) {
            pushSegment(convert(segment), index);
        } else if (pushOrPop.equals("pop")) {
            popSegment(convert(segment), index);
        }
    }

    @Override
    public void writeBranchingCommand(String command, String label){
        writer.println("// " + command + " " + label);
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
            writer.println("// function " + funcName + " "+ num);
            writeFunctionDeclare(funcName, Integer.parseInt(num));
        }else if(command.equals("call")){
            writer.println("// call " + funcName + " " + num);
            writeFunctionCall(funcName, Integer.parseInt(num));
        }else if(command.equals("return")){
            writer.println("// return ");
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
//        writer.println("//setup local seg");
//        writer.println("@SP");
//        writer.println("D=M");
//        writer.println("@LCL");
//        writer.println("M=D");

        writer.println("//start of Func Declare");
        for(int i=0 ;i< numberOfLocalVar; i++){
            writer.println("//init local args to 0");
            pushSegment(CONST, 0);
        }

        writer.println("");
    }

    public void writeInit(){
        writer.println("//bootstrap sys.init");
        writer.println("@256");
        writer.println("D=A");
        writer.println("@SP");
        writer.println("M=D");
        writeFunctionCall("Sys.init",0);

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
//        writer.println("//settings up args");
//        writer.println("@SP");
//        writer.println("D=M");
//        writer.println("@ARG");
//        writer.println("M=D-"+ numberOfArgs);

        // 2. save the caller's frame
        writer.println("//start of function call");
        //push return-address
        writer.println("//push return-address");
        writer.println("@" + returnAddress);
        writer.println("D=A");
        pushD();

        //push LCL
        writer.println("//push LCL");
        writer.println("@LCL");
        writer.println("D=M");
        pushD();

        //push ARG
        writer.println("//push ARG");
        writer.println("@ARG");
        writer.println("D=M");
        pushD();

        //push THIS
        writer.println("//push THIS");
        writer.println("@THIS");
        writer.println("D=M");
        pushD();

        //push THAT
        writer.println("//push THAT");
        writer.println("@THAT");
        writer.println("D=M");
        pushD();

        //ARG = SP - n - 5
        writer.println("//ARG = SP - n - 5 ");
        writer.println("//Reposition ARG (n = number of args.)");
        writer.println("@SP");
        writer.println("D=M");
        writer.println("@"+(numberOfArgs+5));
        writer.println("D=D-A");
        writer.println("@ARG");
        writer.println("M=D");

        //LCL = SP
        writer.println("//LCL = SP");
        writer.println("@SP");
        writer.println("D=M");
        writer.println("@LCL");
        writer.println("M=D");


        // 3. Jump to execute callee
        writer.println("//goto f");
        writeGoto(funcName);

        writer.println("("+ returnAddress + ")"); //marking return address

        writer.println("//end of Func Call");
        writer.println("");
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
        writer.println("//FRAME = *LCL");
        writer.println("//FRAME is a temporary variable");
        writer.println("//R14 will be use as FRAME");

        writer.println("@LCL");
        writer.println("D=M");
        writer.println("@R14");
        writer.println("M=D");
        writer.println();

        writer.println("//RET = *(FRAME - 5)");
        writer.println("//put the return address in a temp var");
        writer.println("//R13 will be use as RET");
        writer.println("@R14");
        writer.println("D=M");
        writer.println("@5");
        writer.println("A=D-A");
        writer.println("D=M");
        writer.println("@R13");
        writer.println("M=D");
        writer.println();

        writer.println("//*ARG = pop()");
        popD();
        writer.println("@ARG");
        writer.println("A=M");
        writer.println("M=D");
        writer.println();

        writer.println("//SP = ARG + 1");
        //@ARG
        //D = M + 1
        //@SP
        //M=D
        writer.println("@ARG");
        writer.println("D=M+1");
        writer.println("@SP");
        writer.println("M=D");
        writer.println();

        writer.println("//THAT = *(FRAME-1)");
        //@R14
        //D=M
        //A=D-1
        //D=M
        //@THAT
        //M=D
        writer.println("@R14");
        writer.println("D=M");
        writer.println("A=D-1");
        writer.println("D=M");
        writer.println("@THAT");
        writer.println("M=D");
        writer.println();

        writer.println("//THIS = *(FRAME-2)");
        //@R14
        //D=M
        //@2
        //A=D-A
        //D=M
        //@THIS
        //M=D
        writer.println("@R14");
        writer.println("D=M");
        writer.println("@2");
        writer.println("A=D-A");
        writer.println("D=M");
        writer.println("@THIS");
        writer.println("M=D");
        writer.println();

        writer.println("//ARG = *(FRAME-3)");
        //@R14
        //D=M
        //@3
        //A=D-A
        //D=M
        //@ARG
        //M=D
        writer.println("@R14");
        writer.println("D=M");
        writer.println("@3");
        writer.println("A=D-A");
        writer.println("D=M");
        writer.println("@ARG");
        writer.println("M=D");
        writer.println();

        writer.println("//LCL = *(FRAME-4)");
        //@R14
        //D=M
        //@4
        //A=D-A
        //D=M
        //@LCL
        //M=D
        writer.println("@R14");
        writer.println("D=M");
        writer.println("@4");
        writer.println("A=D-A");
        writer.println("D=M");
        writer.println("@LCL");
        writer.println("M=D");
        writer.println();

        // jump to return address
        writer.println("//goto RET");
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
