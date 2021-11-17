.text
.global main 

main: 
 /* FM.1 PROLOGUE of a non-leaf function – main */
 push {r11, lr} /* Start of the prologue. Saving Frame Pointer and LR onto the stack */ 
 add r11, sp, #0 /* Setting up the bottom of the stack frame */ 
 sub sp, sp, #16 /* End of the prologue. Allocating some buffer on the stack */ 
 
 /* FM.2 BODY of a non-leaf function – main */
 mov r0, #1 /* setting up local variables (a=1). This also serves as setting up the first parameter for the max function */ 
 mov r1, #2 /* setting up local variables (b=2). This also serves as setting up the second parameter for the max function */ 
 bl func1     /* Calling/branching to function max */
 
 /* FM.3 EPILOGUE of a non-leaf function – main */
 sub sp, r11, #0 /* Start of the epilogue. Readjusting the Stack Pointer */
 /* End of the epilogue. Restoring Frame pointer from the stack, jumping to previously saved LR via direct load into PC */
 pop {r11, pc}
 
func1:
 /* F1.1 PROLOGUE of a non-leaf function – func1 */
 push {r11, lr} /* Start of the prologue. Saving Frame Pointer and LR onto the stack */ 
 add r11, sp, #0 /* Setting up the bottom of the stack frame */ 
 sub sp, sp, #16 /* End of the prologue. Allocating some buffer on the stack */ 
 
 /* F1.2 BODY of a non-leaf function – func1 */
 mov r0, #3 /* setting up local variables (a=3). This also serves as setting up the first parameter for the max function */ 
 mov r1, #4 /* setting up local variables (b=4). This also serves as setting up the second parameter for the max function */ 
 bl func2     /* Calling/branching to function max */

 /* F1.3 EPILOGUE of a non-leaf function – func1 or main */
 sub sp, r11, #0 /* Start of the epilogue. Readjusting the Stack Pointer */ 
 /* End of the epilogue. Restoring Frame pointer from the stack, jumping to previously saved LR via direct load into PC */
 pop {r11, pc}

func2: 
 /* F2.1 PROLOGUE of a non-leaf function – func2 */
 push {r11} /* Start of the prologue. Saving Frame Pointer onto the stack */ 
 add r11, sp, #0 /* Setting up the bottom of the stack frame */ 
 sub sp, sp, #12 /* End of the prologue. Allocating some buffer on the stack */ 
 
 /* F2.2 BODY of a non-leaf function – func2 – do nothing or something useful */
 mov r6, #1

 /* F2.2 EPILOGUE of a non-leaf function – func2 */
 add sp, r11, #0 /* Start of the epilogue. Readjusting the Stack Pointer */ 
 pop {r11} /* restoring frame pointer */ 
 bx lr /* End of the epilogue. Jumping back to main via LR register */
