//http://mikeos.sourceforge.net/write-your-own-os.html
//http://mikeos.sourceforge.net/

1. Edit myfirst.asm

	BITS 16

start:
	mov ax, 07C0h		; Set up 4K stack space after this bootloader
	add ax, 288		; (4096 + 512) / 16 bytes per paragraph
	mov ss, ax
	mov sp, 4096

	mov ax, 07C0h		; Set data segment to where we're loaded
	mov ds, ax


	mov si, text_string	; Put string position into SI
	call print_string	; Call our string-printing routine

	jmp $			; Jump here - infinite loop!


	text_string db 'This is my cool new OS!', 0


print_string:			; Routine: output string in SI to screen
	mov ah, 0Eh		; int 10h 'print char' function

.repeat:
	lodsb			; Get character from string
	cmp al, 0
	je .done		; If char is zero, end of string
	int 10h			; Otherwise, print it
	jmp .repeat

.done:
	ret


	times 510-($-$$) db 0	; Pad remainder of boot sector with 0s
	dw 0xAA55		; The standard PC boot signature
	
2. compile with nasm the bootloader
nasm -f bin -o myfirst.bin myfirst.asm

3. copy our kernel to the first sector of the floppy disk image and generate ISO Image

Linux: dd status=noxfer conv=notrunc if=myfirst.bin of=myfirst.flp
Mac: dd conv=notrunc if=myfirst.bin of=myfirst.flp

Linux: mkisofs -o myfirst.iso -b myfirst.flp cdiso/
Mac: 
Create a new VMBox machine unknown and insert floppy as bootloading.


File -> New Image -> Create from folder: (cdiso) folder which contains *.flp file and type is Master DVD

You could use Disk Utility to create one. Click on the New Image button at the toolbar, and the tool will create a new .dmg file. 
You can then drag all the files you want to burn and afterwards (again using the Disk Utility) 
use the Convert… button to create a .cdr file (in the menu it’s called DVD/CD Master in the image format field).

The .cdr file is similar to the .iso file, so just rename it afterwards, and there you go.




Now we need a virtual floppy disk image to which we can write our bootloader-sized kernel. Copy mikeos.flp from the disk_images/ directory of the MikeOS bundle into your home directory, and rename it myfirst.flp. Then enter:

dd status=noxfer conv=notrunc if=myfirst.bin of=myfirst.flp
This uses the 'dd' utility to directly copy our kernel to the first sector of the floppy disk image. When it's done, we can boot our new OS using the QEMU PC emulator as follows:

qemu -fda myfirst.flp
And there you are! Your OS will boot up in a virtual PC. If you want to use it on a real PC, you can write the floppy disk image to a real floppy and boot from it, or generate a CD-ROM ISO image. For the latter, make a new directory called cdiso and move the myfirst.flp file into it. Then, in your home directory, enter:

mkisofs -o myfirst.iso -b myfirst.flp cdiso/
This generates a CD-ROM ISO image called myfirst.iso with bootable floppy disk emulation, using the virtual floppy disk image from before. Now you can burn that ISO to a CD-R and boot your PC from it! (Note that you need to burn it as a direct ISO image and not just copy it onto a disc.)