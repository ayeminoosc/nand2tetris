package org.nand2teris.project.p08;

import java.io.IOException;

public interface CodeWriter {
    void writeArithmeticCommand(String command) throws IOException;
    void writeMemoryAccessCommand(String command, String segment, int index) throws IOException;
    void writeBranchingCommand(String command, String label);
    void writeFunctionCommand();
    void close();
}
