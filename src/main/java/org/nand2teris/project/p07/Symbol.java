package org.nand2teris.project.p07;

public enum  Symbol {
    SP, LCL, ARG, THIS, THAT, R13, R14, R15, TEMP;

    @Override
    public String toString(){
        if(this == TEMP) return "5";
        else return name();
    }
}
