.text
 
.globl main
 
main:
    push {r7, lr}
    
    @ Open an input file for reading
ldr r0,=binaryfile      @ set Name for input file
mov r1,#0
ldr r2,=0666            @ permissions
mov r7,#5
swi 0 

@ Save the file handle in memory:
ldr r1,=Handle     @  load input file handle
str r0,[r1]              @  save the file handle


loop:
ldr r0,=Handle           @ load input file handle
ldr r0,[r0]
ldr r1,=readBuffer
mov r2,#16 
mov r7, #3
swi 0                    @ read the integer into R0
    
    mov r0, #0
    pop {r7, lr}
    bx lr

Handle: .skip 4
readBuffer: .skip 80
binaryfile: .asciz "test.bin"
