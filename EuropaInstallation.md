**Note** : Starting with EUROPA 2.1.2 (available since Sept 2, 2008),  EUROPA is provided in a binary distribution, to
> which this installation page applies.  To build and EUROPA from source, see BuildingEuropa.


## Supported Platforms ##

EUROPA is currently supported on Linux, Mac OS, Windows and Solaris. A platform is officially supported if and only if there is an automated build regularly scheduled for it. You can see the specs for the systems used to test the EUROPA releases [here](NightlyBuilds.md).

## Software Requirements ##
  * JRE 1.5
  * Python
  * [libantlr3c](http://code.google.com/p/europa-pso/wiki/BuildingEuropa#Install)
  * [Ant](http://ant.apache.org/bindownload.cgi)

## Installing Europa ##

After downloading the appropriate EUROPA distribution for your system (available [here](EuropaDownload.md)), just unzip and  set the EUROPA\_HOME environment variable.
For example, assuming that you have the EUROPA distribution in your ~/tmp directory and want to install EUROPA in your ~/europa directory, using bash you would do (modify appropriately for your os+shell)  :

```
% mkdir ~/europa
% cd ~/europa
% unzip ~/tmp/europa-2.1.2-linux.zip 
% export EUROPA_HOME=~/europa
% export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$EUROPA_HOME/lib      # DYLD_LIBRARY_PATH on a Mac
% export DYLD_BIND_AT_LAUNCH=YES                                  # Only needed on Mac OS X
```


You are now ready to use EUROPA. If you are new to EUROPA, you are ready to create your first EUROPA project, as described in the [Quick Start](QuickStart.md)

## Static Libraries ##

The basic EUROPA distribution contains shared libraries only, static libraries are bundled in a separate zip file (they would otherwise more than double the size of the EUROPA distribution).

If you want to use static libraries, get the appropriate zip file from the [download page](EuropaDownload.md)  then unzip it under your $EUROPA\_HOME directory, the libraries will be put in the $EUROPA\_HOME/lib directory.

To continue the example above, this is how you would add the corresponding static libraries to your EUROPA installation :

```
% cd $EUROPA_HOME
% unzip ~/tmp/europa-2.1.2-linux-static-libs.zip 
```