Apart from build tools, EUROPA is fairly self contained.  However, Windows support is all done through Cygwin right now, so you'll need a functional install of that first.

### Build tools ###

For Cygwin ensure that you've got the following packages:

```
openssh
flex
bison
gcc
g++
mingw libraries for gcc/g++
make
svn
swig
unzip
```

After you've done that you'll need to install ant and jam (assuming you have already installed java):

For jam:

```
 1) download ftp://anonymous@ftp.perforce.com/pub/jam/jam-2.5.zip
 2) extract with unzip
 3) execute `make`
 4) execute `./jam0.exe`
 5) copy the file from the bin.* directory to somewhere in your path
```

For ant just follow Apache's instructions for a windows install (they've got a regular binary installer)

Define ANT\_HOME in your environment using export and DOS style path names. e.g. `export ANT_HOME=C:\\progra~1\\apache-ant-1.7.3` (do not go too deep into the \bin directory, because the system will automatically go to \bin for the executables.)

Also, add a JAVA\_HOME in your environment the same way as for ANT\_HOME.

### Source checkout ###

Check the EUROPA source out as described [here](BuildingEuropa#Building_EUROPA.md).


A couple of other environmental constants you need to define:
```
export EUROPA_HOME=$PLASMA_HOME/dist/europa 
export LD_LIBRARY_PATH=$EUROPA_HOME/lib:./build/lib:.
```


There are two ways to run EUROPA:

1. in java. Go to the PLASMA directory, and choose among the following two options:
```
1) non-optimized mode: ant
2) optimization mode: ant -Dproject.mode=o
```

2. in C++. To run a make, you have to define $EUROPA\_HOME in the linux way:
e.g./home/YOUR\_USR\_NAME/PLASMA/dist/europa
Then go to the PLASMA directory, and type in `make`.