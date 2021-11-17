

1. Create Native C++ Project
File -> New -> New Project ... => Native C++ (name ToUpper2)

* Make sure that SDK and SDK is installed and configured into Android Studio IDE
File -> Project Structure ... 
(e.g. Android SDK location = /Users/ctoma/Library/Android/sdk)
(e.g. Android NDK location = /Users/ctoma/Library/Android/ndk/21.4.7075529)
(e.g. JDK under Gradle settings >= 11)

* make sure you have in /Users/ctoma/Workspaces/AndroidStudioProjects/ToUpper2/local.properties
sdk.dir=/Users/ctoma/Library/Android/sdk
ndk.dir=/Users/ctoma/Library/Android/sdk/ndk/21.4.7075529

2. In native-lib.cpp from ToUpper2/app/src/cpp/native-lib.cpp

-------------------------------------------------------------

//#include <jni.h>
//#include <string>

//extern "C" JNIEXPORT jstring JNICALL
//Java_com_example_toupper2_MainActivity_stringFromJNI(
//        JNIEnv* env,
//        jobject /* this */) {
    //std::string hello = "Hello from C++";
    //return env->NewStringUTF(hello.c_str());
//}
#include <jni.h>
#include <string>

/*
// ARM 32 bits
@
@ Assembler program to convert a string to
@ all upper case.
@
@ R1 - address of output string
@ R0 - address of input string
@ R4 - original output string for length calc.
@ R5 - current character being processed
@

.global mytoupper	     @ Allow other files to call this routine

mytoupper:	PUSH	{R4-R5}	@ Save the registers we use.
	MOV	R4, R1
@ The loop is until byte pointed to by R1 is non-zero
loop:	LDRB	R5, [R0], #1	@ load character and increment pointer
@ If R5 > 'z' then goto cont
	CMP	R5, #'z'	    @ is letter > 'z'?
	BGT	cont
@ Else if R5 < 'a' then goto end if
	CMP	R5, #'a'
	BLT	cont	@ goto to end if
@ if we got here then the letter is lower case, so convert it.
	SUB	R5, #('a'-'A')
cont:	@ end if
	STRB	R5, [R1], #1	@ store character to output str
	CMP	R5, #0		@ stop on hitting a null character
	BNE	loop		@ loop if character isn't null
	SUB	R0, R1, R4  @ get the length by subtracting the pointers
	POP	{R4-R5}		@ Restore the register we use.
	BX	LR		@ Return to caller
 */

/*
// ARM 64 bits
// Assembler program to convert a string to
// all upper case.
//
// X1 - address of output string
// X0 - address of input string
// X4 - original output string for length calc.
// W5 - current character being processed
//

.global mytoupper	     // Allow other files to call this routine

mytoupper:
	MOV	X4, X1
// The loop is until byte pointed to by X1 is non-zero
loop:	LDRB	W5, [X0], #1	// load character and increment pointer
// If W5 > 'z' then goto cont
	CMP	W5, #'z'	    // is letter > 'z'?
	B.GT	cont
// Else if W5 < 'a' then goto end if
	CMP	W5, #'a'
	B.LT	cont	// goto to end if
// if we got here then the letter is lower case, so convert it.
	SUB	W5, W5, #('a'-'A')
cont:	// end if
	STRB	W5, [X1], #1	// store character to output str
	CMP	W5, #0		// stop on hitting a null character
	B.NE	loop		// loop if character isn't null
	SUB	X0, X1, X4  // get the length by subtracting the pointers
	RET		// Return to caller
*/

/*
// portable C/C++
int mytoupper( const char * input, char * output)
{
    int i = 0;

    while( input[i] != NULL ) {
        if (input[i] >= 'a' && input[i] <= 'z') {
            output[i] = input[i] - ('a' - 'A');
        } else
        {
            output[i] = input[i];
        }
        i++;
    }
    output[i] = NULL;
    return i;
}
*/

extern "C" int mytoupper( const char * input, char * output);

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_toupper2_MainActivity_toupperJNI(
        JNIEnv* env,
        jobject /* this */,
        jstring input) {
    char upperStr[255];

    mytoupper(env->GetStringUTFChars(input, NULL), upperStr);
    return env->NewStringUTF(upperStr);
}


-------------------------------------------------------------

3. Add ARM assembly file under ToUpper2/app/src/main/cpp - upper.s and 
write down ARM Assembly for 32 bits or 64 bits depending of the smartphone CPU - this arm7v-a for Samsung J5 and the Google Play does not publish anymore unless ARM assembly 64 bits - see the comment in the C/C++ file above:

// Assembler program to convert a string to
// all upper case.
// R1 - address of output string
// R0 - address of input string
// R4 - original output string for length calc.
// R5 - current character being processed

.global mytoupper	     // Allow other files to call this routine

mytoupper:	PUSH	{R4-R5}	// Save the registers we use.
	MOV	R4, R1
// The loop is until byte pointed to by R1 is non-zero
loop:	LDRB	R5, [R0], #1	// load character and increment pointer
// If R5 > 'z' then goto cont
	CMP	R5, #'z'	    // is letter > 'z'?
	BGT	cont
// Else if R5 < 'a' then goto end if
	CMP	R5, #'a'
	BLT	cont	// goto to end if
// if we got here then the letter is lower case, so convert it.
	SUB	R5, #('a'-'A')
cont:	// end if
	STRB	R5, [R1], #1	// store character to output str
	CMP	R5, #0		// stop on hitting a null character
	BNE	loop		// loop if character isn't null
	SUB	R0, R1, R4  // get the length by subtracting the pointers
	POP	{R4-R5}		// Restore the register we use.
	BX	LR		// Return to caller


-------------------------------------------------------------

4. Add the assembly file in the CMake list for native compiling (e.g. file: /Users/ctoma/Workspaces/AndroidStudioProjects/ToUpper2/app/src/main/cpp/CMakeLists.txt)


add_library( # Sets the name of the library.
        toupper2

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp upper.s )
#        native-lib.cpp )



-------------------------------------------------------------

5. In the project Gradle file make sure you have the hardware ARM CPU target 
(e.g. /Users/ctoma/Workspaces/AndroidStudioProjects/ToUpper2/app/build.gradle)

android {
   compileSdk 30

   // ...

   buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
        debug {
            ndk {
                abiFilters "armeabi-v7a"
                //abiFilters "x86", "armeabi-v7a", "arm64-v8a", "armeabi"
                //abiFilters ABI_FILTERS
            }
        }
    }
}

-------------------------------------------------------------

6. In the GUI XML file from the MVC / MVP pattern edit the user interface controls
(e.g. /Users/ctoma/Workspaces/AndroidStudioProjects/ToUpper2/app/src/main/res/layout/activity_main.xml)

<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">
<!--
    <TextView
        android:id="@+id/sample_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
-->
    <EditText
        android:id="@+id/enterText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="92dp"
        android:layout_marginTop="144dp"
        android:ems="10"
        android:hint="Enter some text"
        android:inputType="textPersonName"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
   <TextView
        android:id="@+id/convertedText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="92dp"
        android:layout_marginTop="272dp"
        android:text="Converted text"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
    <Button
        android:id="@+id/convert"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="180dp"
        android:layout_marginTop="412dp"
        android:onClick="convertMessage"
        android:text="Convert Text"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
</androidx.constraintlayout.widget.ConstraintLayout>

-------------------------------------------------------------

7. In the Kotlin source file add controller and handler implementation for button click which triggers via JNI C code which call AR Assembly on 32 bits or 64 bits
(e.g. /Users/ctoma/Workspaces/AndroidStudioProjects/ToUpper2/app/src/main/java/com/example/toupper2/MainActivity.kt)

package com.example.toupper2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.example.toupper2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        //binding.sampleText.text = stringFromJNI()

    }

    /**
     * A native method that is implemented by the 'toupper2' native library,
     * which is packaged with this application.
     */
    //external fun stringFromJNI(): String

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //external fun toupperJNI(input: String): String
    //companion object {
    //    // Used to load the 'native-lib' library on application startup.
    //    init {
    //        System.loadLibrary("native-lib")
    //    }
    //}

    external fun toupperJNI(input: String): String
    companion object {
        // Used to load the 'toupper2' library on application startup.
        init {
            System.loadLibrary("toupper2")
        }
    }


    /** Called when the user taps the Send button */
    fun convertMessage(v: View) {
        // Do something in response to button
        val editText = findViewById<EditText>(R.id.enterText)
        val message = toupperJNI(editText.text.toString())
        val textView = findViewById<TextView>(R.id.convertedText).apply {
            text = message
        }
    }
}


-------------------------------------------------------------
My Full Android Project Path = /Users/ctoma/Workspaces/AndroidStudioProjects/ToUpper2
 
Build - > Rebuild Project - plugin in USB Samsung J5 and Run -> Run 'app'

