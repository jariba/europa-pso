resolvers += "Sonatype Maven2 Snapshots Repository" at "http://oss.sonatype.org/content/groups/scala-tools"

resolvers += ScalaToolsSnapshots
    
addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.1.1")
