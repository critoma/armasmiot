# wget -O ~/.gdbinit-gef.py -q https://github.com/hugsy/gef/raw/master/gef.py
# mv ~/.gdbinit ~/.gdbinit.original
# echo source ~/.gdbinit-gef.py >> ~/.gdbinit


# 1. Data Types
as p01dt.s -o p01dt.o
gcc -o p01dt.elf p01dt.o
gdb p01dt.elf
rm -rf *.elf *.o 

# 2. Simple load and store
as p02ls.s -o p02ls.o
gcc -o p02ls.elf p02ls.o

as p02load.s -o p02load.o
gcc -o p02load.elf p02load.o

as p02store.s -o p02store.o
gcc -o p02store.elf p02store.o

# 3. Pre-, Post- indexed and Offset address mode for load and store + multiple ldm/stm
as p03lspostpre.s -o p03lspostpre.o
gcc -o p03lspostpre.elf p03lspostpre.o

as p03ldmstm.s -o p03ldmstm.o

as p03pp.s -o p03pp.o
gcc -o p03pp.elf p03pp.o

# 4. 

