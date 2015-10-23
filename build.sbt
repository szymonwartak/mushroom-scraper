name := "mushroom-scraper"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.3.5",
  "org.seleniumhq.selenium" % "selenium-java" % "2.42.2",
  "joda-time" % "joda-time" % "2.4",
  "org.joda" % "joda-convert" % "1.7"
)
