name := "DrillDown"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache
)     

libraryDependencies += "com.google.code.gson" % "gson" % "2.2.4"

libraryDependencies += "commons-logging" % "commons-logging" % "1.1.1"

libraryDependencies += "commons-codec" % "commons-codec" % "1.5"

libraryDependencies += "commons-lang" % "commons-lang" % "2.6"

libraryDependencies += "org.apache.solr" % "solr-solrj" % "4.7.0"

play.Project.playJavaSettings
