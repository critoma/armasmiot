.global main

main:
        mov     r1, #2     /* setting up initial variable a */
        mov     r2, #3     /* setting up initial variable b */
        cmp     r1, r2     /* comparing variables to determine which is bigger */
        blt     r1_lower   /* jump to r1_lower in case r2 is bigger (N==1) */
        mov     r0, r1     /* if branching/jumping did not occur, r1 is bigger (or the same) so store r1 into r0 */
        b       end        /* proceed to the end */
r1_lower:
        mov r0, r2         /* We ended up here because r1 was smaller than r2, so move r2 into r0 */
        b end              /* proceed to the end */
end:
        bx lr              /* THE END */

/*
int main() {
   int max = 0;
   int a = 2;
   int b = 3;
   if(a < b) {
    max = b;
   }
   else {
    max = a;
   }
   return max;
}
*/
