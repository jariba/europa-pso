## Checklist for committing anything to the repository ##

  1. Get the latest from svn.
  1. Go to $PLASMA\_HOME. run "ant clean autobuild", which will run the entire autobuild procedure, or at a minimum "ant clean build test" which will make sure at least build tests pass for a specific variant. Breaking the build is never ok, so you should be pretty confident your changes won't break it if you take the shortcut
  1. Make sure all the tests passed
  1. Commit. Provide a meaningful comment, do separate commits for each feature/bug fix.
  1. Watch your email, bitten will email the person who committed and europa-build whenever a build fails. You should also check the [build page](http://babelfish.arc.nasa.gov/trac/europa/build) to make sure your commit didn't break any of the automated builds, EUROPA is built on multiple platforms and with different variants so the fact that all tests passed on your particular platform/variant is no guarantee that it'll do the same for all others.
