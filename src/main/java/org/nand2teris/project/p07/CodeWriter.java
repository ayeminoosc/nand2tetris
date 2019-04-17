package org.nand2teris.project.p07;

import java.io.IOException;

public interface CodeWriter {
    void writeArithmetic(String command) throws IOException;
    void writePushPop(CommandType pushOrPop, String segment, int index) throws IOException;
    void close();
}
