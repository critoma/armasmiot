.text
.global main

main:
     .code 32         @ ARM mode
     add r2, pc, #1   @ put PC+1 into R2
     bx r2            @ branch + exchange to R2

    .code 16          @ Thumb mode
     mov r0, #1
    
	bkpt