// Adds 1+...+100.
// int i = 1;
// int sum = 0;
// While (i <= 100){
// sum += i;
// i++;
// }


@100
D=A
@i
M=D
@sum
M=0
@0
D=A
(loop)
@sum
D=M
@i
D= D+M
M= M - 1
@sum
M=D
@i
D=M
@loop
D;JGT


(end)
@end
0;JMP

