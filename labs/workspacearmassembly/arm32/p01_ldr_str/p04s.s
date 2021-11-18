@ draw RAM
.data
var1: .word 9
var2: .word 7

.text
.global main
main:

  ldr r0, addrvar1
  ldr r1, addrvar2
  ldr r2, [r0]
  str r2, [r1]

  bkpt

addrvar1: .word var1
addrvar2: .word var2
