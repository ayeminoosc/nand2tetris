package org.nand2teris.project.p08;

public enum  Symbol {
    SP, LCL, ARG, THIS, THAT, R13, R14, R15, TEMP, CONST, STATIC, POINTER;
    //CONST, TEMP, STATIC, POINTER are not really translated into memory segment location, instead they are use for readability

    @Override
    public String toString(){
        if(this == TEMP) return "5";
        else return name();
    }
}
