package org.nand2teris.project.p07;

public class Instruction{
    String command;
    String memorySegment;
    int index;

    public Instruction(String command, String memorySegment, int index) {
        this.command = command;
        this.memorySegment = memorySegment;
        this.index = index;
    }

    public Instruction(String command) {
        this.command = command;
    }
}