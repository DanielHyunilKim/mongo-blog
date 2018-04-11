## CS 61 Lab 3B
##### Daniel Kim

Lab 3 Part B for CS61 (Databases) for Professor Palmer. This application is a blog engine that processes transactions from stdin. It can upload posts and comments, delete posts or comments, and show all the contents of a blog. 

### In the .tar file
* README.md
* Lab3B.java
* Lab3B.in
* Lab3B.out
* makefile
### Building and running the program
* Must have `mongob-driver`, `bson`, and `mongodb-driver-core` jar files in the `$CLASSPATH`.

#### Build
```bash
$ make
```
#### Run
```bash
$ make run
```
#### Clean
```bash
$ make clean
```
### Testing
* The application uses Lab3B.in as its stdin; the file contains a series of post and comment uploads in different blogs, and also deletions. The input calls `show` after each upload or deletion, which gets streamed to Lab3B.out. 