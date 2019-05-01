// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/07/StackArithmetic/SimpleAdd/SimpleAdd.tst

load SimplyGt.asm,
output-file SimplyGt.out,
compare-to SimplyGt.cmp,
output-list RAM[0]%D2.6.2 RAM[256]%D2.6.2 RAM[257]%D2.6.2 RAM[258]%D2.6.2;

set RAM[0] 256,  // initializes the stack pointer

repeat 120 {      // enough cycles to complete the execution
  ticktock;
}

output;          // the stack pointer and the stack base
