/* We need to set up the stack pointer before C code can run
   This memory address is the end of the on-board 128MB of
   RAM for QEMU versatilepb */
.global _start
_start:
	mov r0, #0x08
	ldr r1, =interrupt_table
	ldr r3, =interrupt_table_end
keep_loading:
	ldr r2, [r1, #0x0]
	str r2, [r0, #0x0]
	add r0, r0, #0x4
	add r1, r1, #0x4
	cmp r1, r3
	bne keep_loading

	ldr sp, =0x07FFFFFF
	bl main

/* http://infocenter.arm.com/help/topic/com.arm.doc.dui0203j/CHDEFDJG.html */
interrupt_table:
	ldr pc, svc_entry_address
	nop
	nop
	nop
	ldr pc, irq_entry_address
	svc_entry_address: .word svc_entry
	irq_entry_address: .word irq_entry
interrupt_table_end:
