/* for (int i = 0; i < 100; i++) a[i] =  i; / see why may not work */
@ use as for assembling and gcc for link-editing

.text

.global main
main:
 ldr r1, =addr_of_a
 mov r2, #0
loop:
 cmp r2, #100
 beq end
 add r3, r1, r2, LSL #2 @ r3 <- r1 + r2*4
 str r2, [r3]           @ *r3 <- r2
 add r2, r2, #1         @ r2 <- r2 + 1
b loop
end:
 bx lr

addr_of_a: .word 0x00000000
