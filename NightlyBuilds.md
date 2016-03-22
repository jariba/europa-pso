# Automated Builds #
[Bitten](http://bitten.edgewall.org/) is our continuous integration server, whenever changes are committed to the EUROPA repository, a new build on the different platforms supported is kicked off.

At a high level, here is what the autobuild does :
  * Build the different variants (debug,optimized,profile) and library types (static,shared)
  * Run all regresion tests
  * Create binary distribution
  * Install binary distribution
  * Test makeproject tool and examples against binary distribution

The results of the autobuild are displayed at all times on the [Build Status page](http://babelfish.arc.nasa.gov/trac/europa/build).

### Canon Systems ###
| **OS** | **OS Version** | **Machine Name** | **CPU** | **Memory** | **Avg Auto-Build Duration  (Includes tests and all variants)** | **OS Details** |
|:-------|:---------------|:-----------------|:--------|:-----------|:---------------------------------------------------------------|:---------------|
| Linux  | Red Hat Enterprise Linux WS release 4 (Nahant Update 7) | selene.arc.nasa.gov | Intel(R) Xeon(TM) 2 CPUs @ 3.40GHz each | 1024MB RAM | ~35 min                                                        | Linux selene.arc.nasa.gov 2.6.9-42.0.3.ELsmp !#1 SMP Mon Sep 25 17:28:02 EDT 2006 i686 i686 i386 GNU/Linux |
| Mac OS | Mac OS X Version 10.5.6 (9G55) | irock.arc.nasa.gov | PowerPC G5 (2.2) 2 CPUs @ 2 GHz each | 1536MB RAM | ~60 min                                                        | Darwin imagebot 9.6.0 Darwin Kernel Version 9.6.0: Mon Nov 24 17:39:01 PST 2008; root:xnu-1228.9.59~1/RELEASE\_PPC Power Macintosh |
| Solaris | Solaris 10     | farnsworth.arc.nasa.gov | UltraSPARC-III 2 CPUs @ 600Mhz each | 2048MB RAM | ~3 hours                                                       | SunOS farnsworth.arc.nasa.gov 5.10 Generic\_118833-33 sun4u sparc SUNW,Sun-Blade-1000 |
|Windows |Windows XP SP2 + cygwin|flight.arc.nasa.gov|         |            |~110 min                                                        |                |

### Creating A New Autobuild ###

You'll need to set up a new bitten slave and ask Javier to register it with the bitten master.

The slave config files for the current autobuild machines can be found [here](http://code.google.com/p/europa-pso/source/browse/ThirdParty/trunk/autobuild)

### Trac mirror for bitten ###

The googlecode repository is mirrored at the machine babelfish at Ames so that the bitten builds can run.
In case the job that keeps the mirror up to date is down, here are the 2 commands that you need to issue to update it manually :

```
svn pdel --revprop -r 0 svn:sync-lock https://babelfish.arc.nasa.gov/svn/europa 
svnsync sync https://babelfish.arc.nasa.gov/svn/europa 
```

Only people with babelfish accounts will be able to run these commands succesfully.
The first line drops any locks on the repository that may have been left there by incomplete svnsyncs.
The second line performs the update.
If you're forced to do this, notify Javier so that the autosync job is brought back up, don't set up your own one. Because of access restrictions on babelfish,  there can only be one active automated job at any time.