.data
var1: .word 3
var2: .word 4

.text
.global main
main:
 ldr r0, adr_var1 @ load the mem addr of var1 via label adr_var1
 ldr r1, adr_var2
 ldr r2, [r0]
 str r2, [r1]
bkpt

adr_var1: .word var1
adr_var2: .word var2
