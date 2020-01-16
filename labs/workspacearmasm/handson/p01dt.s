.section .text
.global main

main:
 mov r0, pc
 mov r1, #2
 add r2, r1, r1 /* r2 <- r1 + r1 */
 bkpt

