.syntax unified    @ this is important!
.text
.global main

main:
    .code 32
    add r3, pc, #1   @ increase value of PC by 1 and add it to R3
    bx r3            @ branch + exchange to the address in R3 -> switch to Thumb state because LSB = 1

    .code 16         @ Thumb state
    cmp r0, #10      
    ite eq           @ if R0 is equal 10...
    addeq r1, #2     @ ... then R1 = R1 + 2
    addne r1, #3     @ ... else R1 = R1 + 3
    bkpt

