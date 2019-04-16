// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

(start)
@24576
D=M
@key
M=D


//counter=8192 because we need to render 8192
@8192
D=A
@counter
M=D

//i=16383
@16383
D=A
@i
M=D

(loop)
//if key else
@key
D=M
@white
D;JEQ

//render black screen
@i
D=M
D=D+1
M=D
A=D;
M=-1;
@endif
0;JEQ
//render white screen
(white)
@i
D=M
D=D+1
M=D
A=D;
M=0;

(endif)
@counter
M=M-1;
D=M
@loop
D;JGT


@start
0;JMP
