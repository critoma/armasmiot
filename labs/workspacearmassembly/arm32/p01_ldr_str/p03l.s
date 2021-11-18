@ draw RAM

.data
.balign 4
myvar1:
 .word 5

myvar2:
 .word -2


.text @ actually .code
.balign 4
.global main @ label main =>  linker gcc / label _start => linker ld

main:
 ldr r1, addr_myvar1 /* r1 <- &myvar1 <=> r1 <- addr_myvar1 */
 ldr r1, [r1]
 ldr r2, =myvar2
 ldr r2, [r2]

 add r0, r1, r2

 bx lr @ bkpt

 addr_myvar1: .word myvar1
 addr_myvar2: .word myvar2
