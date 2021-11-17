@
@ Assembler program to flash three LEDs connnected to the
@ Raspberry Pi GPIO port.
@
@ r6 - loop variable to flash lights 10 times
@
.include "gpiomacros.s"

.global _start	            @ Provide program starting address to linker
_start: 
	@GPIOExport  pin17
        @GPIOExport  pin27
        @GPIOExport  pin22

	GPIOExport pin21 
        nanoSleep

        @GPIODirectionOut pin17
        @GPIODirectionOut pin27
        @GPIODirectionOut pin22

        GPIODirectionOut pin21
        @ setup a loop counter for 10 or 100 iterations
        mov         r6, #100

loop:   
	@GPIOWrite   pin17, high
        @nanoSleep
        @GPIOWrite   pin17, low
        @GPIOWrite   pin27, high
        @nanoSleep
        @GPIOWrite   pin27, low
        @GPIOWrite   pin22, high
        @nanoSleep
        @GPIOWrite   pin22, low

	GPIOWrite   pin21, high
        nanoSleep
        GPIOWrite   pin21, low
        @decrement loop counter and see if we loop
        subs    r6, #1      @ Subtract 1 from loop register setting status register
        bne     loop        @ If we haven't counted down to 0 then loop

_end:   
	mov     R0, #0      @ Use 0 return code
        mov     R7, #1      @ Service command code 1 terminates this program
        svc     0           @ Linus command to terminate program

pin17:      .asciz  "17"
pin27:      .asciz  "27"
pin22:      .asciz  "22"
pin21:      .asciz  "21"
low:        .asciz  "0"
high:       .asciz  "1"
