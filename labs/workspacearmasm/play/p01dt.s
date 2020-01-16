@ Compile as following:
@ cd /home/stud/armasm/critoma
@ as p01dt.s -o p01dt.o
/* link-edit either with ld (use main label _start:) or gcc (use label main:): */
/* ld -o p01dt.elf p01dt.o */
/* gcc -o p01dt.elf p01dt.o */

@ make sure gef is installed for gdb: 
/* https://azeria-labs.com/debugging-with-gdb-introduction/ | https://github.com/hugsy/gef */

@ gdb p01.elf
@ break _start | break main
@ run 
@ apropos registers
@ info registers
@ nexi 
@ stepi
@ x/4i $pc
@ x/4xw $pc
@ vmmap
/* 
as p01dt.s -o p01dt.o
gcc -o p01dt.elf p01dt.o
gdb p01dt.elf
rm -rf *.elf *.o 
*/

.section .text
.global main

main:
 mov r0, pc
 mov r1, #2
 add r2, r1, r1 /* r2 <- r1 + r1 */
 bkpt

