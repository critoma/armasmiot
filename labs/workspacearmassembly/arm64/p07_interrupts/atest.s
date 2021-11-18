.text

.global main

main:

 mov w0, #2 //mov r0, #2
 mov x2, #20

 b end

 mov w0, #3 //mov r0, #3
 mov x1, #20
end:

 ret //bx lr
