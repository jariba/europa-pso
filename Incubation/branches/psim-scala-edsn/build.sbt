name := "psim-scala"

version := "0.1"

scalaVersion := "2.10.1"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

//libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.4"

parallelExecution in Test := false

scalacOptions += "-explaintypes"

scalacOptions += "-deprecation"

traceLevel in test := 0
