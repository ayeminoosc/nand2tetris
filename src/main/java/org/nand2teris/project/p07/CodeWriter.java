package org.nand2teris.project.p07;

public interface CodeWriter {
    void writeArithmetic(String command);
    void writePushPop(CommandType pushOrPop, String segment, int index);
}
