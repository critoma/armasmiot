.global main

main:
        mov     r0, #0     /* setting up initial variable a */
loop:
        cmp     r0, #4     /* checking if a==4 */
        beq     end        /* proceeding to the end if a==4 */
        add     r0, r0, #1 /* increasing a by 1 if the jump to the end did not occur */
        b loop             /* repeating the loop */
end:
        bx lr              /* THE END */

/*
int main() {
   int a = 0;
   while(a < 4) {
     a= a+1;
   }
   return a;
}
*/
