name := "Europa-Scala"

version := "0.1"

//scalaVersion := "2.9.2"

//libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

//libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.0-RC2"

//libraryDependencies += "org.scalesxml" %% "scales-xml" % "0.4.5"


scalaVersion := "2.10.2"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.2"

libraryDependencies += "org.scalaz" %% "scalaz-iteratee" % "7.0.2"

libraryDependencies += "org.scalaz" %% "scalaz-iterv" % "7.0.2"

parallelExecution in Test := false

//scalacOptions += "-explaintypes"

scalacOptions += "-deprecation"

scalacOptions += "-feature"

scalacOptions += "-language:postfixOps"

scalacOptions += "-language:implicitConversions"

//scalacOptions += "-Ylog-classpath"

traceLevel in test := 0

testOptions in Test += Tests.Argument("-oF")

testOptions in Test += Tests.Argument("-l")

testOptions in Test += Tests.Argument("tags.Nope tags.OpenDomains")
