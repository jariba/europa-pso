name := "Europa-Scala"

version := "0.1"

scalaVersion := "2.9.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.8" % "test"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.4"

libraryDependencies += "com.codecommit" %% "anti-xml" % "0.3"

parallelExecution in Test := false

scalacOptions += "-explaintypes"

scalacOptions += "-deprecation"

traceLevel in test := 0

testOptions in Test += Tests.Argument("-oF")