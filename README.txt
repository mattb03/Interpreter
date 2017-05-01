


In order to create the .class files in the /bin directory and the /bin directory itself:

    $ make

In order to clean .class files in the /bin directory
    $make clean

In order to run the different cases:

  ** see the makefile and you can see how to run
     all the different good cases with 'make'

To test different input:
    modify prog.hav and then run "make hav"



**Below is the README.txt that comes with the installHavabol.zip
=================================================================

to install HavaBol on your linux system (you will need sudo privleges)

1.) Download the installHavabol.zip file (its in the repo)
2.) unzip the file   $ unzip installHavaBol.zip
3.) run the install.sh script  $ ./install.sh

DONE!

** make sure you have java installed as it is required for the interpreter **

now you can invoke 'hav' or './hav'  (depending on your PATH) from anywhere on your system!




the 'hav' executable should live in /usr/bin     --> /usr/bin/hav
the 'Interpreter' folder should live int /etc    -->   /etc/Interpreter

And that should do it. Now you can just execute 'hav' from anywhere in your system.
the only argument passed to it should be the source file.

ie.     $ ./hav helloworld.hav


**Note:  if you wanna check out the source, after install, goto /etc/Interpreter and look around!

**Also the programmer Manual for HavaBol is under the Interpreter directory **

/etc/Interpreter/HavaBolManual.docx

Use libre office to open it
