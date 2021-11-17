.text
.global main
main:
 mov r1, #2
 mov r2, #2
 cmp r1, r2
 beq case_equal
case_dif:
 mov r0, #2
 b end
case_equal:
 mov r0, #1
end:
 bx lr
