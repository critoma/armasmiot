# armasmiot
IoT and Embedded OS Intro in C, ARM Assembly, Java - node.js

# Example of development and debugging of ARM Assembly on Raspberry Pi 3:
1. Install GEF as plugin for GDB
https://github.com/hugsy/gef

wget -O ~/.gdbinit-gef.py -q https://github.com/hugsy/gef/raw/master/gef.py
echo source ~/.gdbinit-gef.py >> ~/.gdbinit

gdb -q /usr/bin/touch
gef help

2. Compile ARM Assembly programs
cd /home/pi/workspacearmasm
cd /home/pi/warmasm

2.1 Data Types:
as -o p01dt.o p01dt.s
ld -o p01dt p01dt.o

./p01dt

./p01dt ; echo $?

gdb -q ./p01dt
br _start
run
nexti 1
nexti 1
nexti 1
quit 

2.2 Load/Store

as -o p02ls.o p02ls.s
ld -o p02ls p02ls.o

gdb -q ./p02ls
disassemble _start
br _start
run
nexti 1
nexti 1
nexti 1
x/w 0x10090
nexti 1
quit
