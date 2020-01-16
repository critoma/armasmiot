.data
var1: .word 3
var2: .word 4

.text
.global _start
_start:
 ldr r0, adr_var1 @ load the mem addr of var1 via label adr_var1
 ldr r1, adr_var2
 ldr r2, [r0]
 str r2, [r1, #2]  @addr mode=offset; store the value from r2(0x03) ...r1 stays unmodified
 str r2, [r1, #4]! @addr mode=offset; store the value from r2(0x03) to the mem addr found in r1 plus 4; r1 is modified


 ldr r3, [r1], #4
bkpt

adr_var1: .word var1
adr_var2: .word var2
