#include<stdio.h>
#include<unistd.h>

int main () {
    pid_t pid[20];
    int i, index;
    FILE *fPointer = popen("ls -al", "r");
    if (fPointer == 0) {
        fprintf(stderr, "Could not execute\n");
    }
    for (i = 0; i < 20; i++) {
        index = i;
        if (index == i) {

            //pid[index] = fork();
            printf ("i am %d\n", index);
        }
    }
    // java -classpath bin havabol.HavaBol for_loop.txt
    system("java -classpath 
    return 0;    
}
