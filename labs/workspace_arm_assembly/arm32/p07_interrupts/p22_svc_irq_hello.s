/* write_sys.s */

.data


greeting: .asciz "Hello world\n"
after_greeting:

.set size_of_greeting, after_greeting - greeting

.text

.globl main

main:
    push {r7, lr}

    /* Prepare the system call */
    mov r0, #1                  /* r0 ← 1 ; 0 = stdin, 1 = stdout */
    ldr r1, =greeting    /* r1 ← &greeting */
    mov r2, #size_of_greeting   /* r2 ← sizeof(greeting) */

    mov r7, #4                  /* select system call 'write' */
    swi #0                      /* perform the system call */

    mov r0, #0
    pop {r7, lr}
    bx lr

