package org.nand2teris.project.p08;

import java.io.IOException;

public interface Parser {
    boolean hasMoreCommands() throws IOException;

    void next() throws IOException;

    CommandType commandType();

    Instruction instruction();
}


