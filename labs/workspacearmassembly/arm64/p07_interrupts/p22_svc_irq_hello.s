
.data


greeting: .asciz "Hello world\n"
after_greeting:

.set size_of_greeting, after_greeting - greeting

.text

.globl main

main:
    /* Prepare the system call */
    mov X0, #1                  /* r0 ← 1 ; 0 = stdin, 1 = stdout */
    ldr X1, =greeting    /* r1 ← &greeting */
    mov X2, #size_of_greeting   /* r2 ← sizeof(greeting) */

    mov X8, #64                  /* select system call 'write' */
    svc 0                      /* perform the system call */

    mov X0, #0
    ret //bx lr

//
// Assembler program to print "Hello World!"
// to stdout.

//
// X0-X2 - parameters to linux function services
// X8 - linux function number
//


/*
//.global _start	            // Provide program starting address to linker
.global main

// Setup the parameters to print hello world
// and then call Linux to do it.
//_start:
main: 
        mov	X0, #1	        // 1 = StdOut
	ldr	X1, =helloworld // string to print
	mov	X2, #13	    // length of our string
	mov	X8, #64	    // linux write system call
	svc	0 	    // Call linux to output the string

//mov w0, #1

// Setup the parameters to exit the program
// and then call Linux to do it.

	mov     X0, #0      // Use 0 return code
        mov     X8, #93      // Service command code 93 terminates this program
        svc     0           // Call linux to terminate the program

.data
helloworld:      .ascii  "Hello World!\n"
*/
