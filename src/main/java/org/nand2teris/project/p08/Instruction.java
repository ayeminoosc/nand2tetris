package org.nand2teris.project.p08;

public class Instruction{
    String command;
    String[] args;

    public Instruction(String command, String[]args) {
        this.command = command;
        this.args = args;
    }

    public Instruction(String command) {
        this.command = command;
    }
}