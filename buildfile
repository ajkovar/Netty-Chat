require 'buildr/scala'

# Version number for this release
VERSION_NUMBER = "1.0.0"
# Group identifier for your projects
GROUP = "Netty"
COPYRIGHT = ""

# Specify Maven 2.0 remote repositories here, like this:
repositories.remote << "http://www.ibiblio.org/maven2/"
repositories.remote << 'https://repository.jboss.org/nexus/content/repositories/releases/'

NETTY = "org.jboss.netty:netty:jar:3.2.4.Final"
JSON = "org.json:json:jar:20090211"
JODA = ["joda-time:joda-time:jar:2.0", "org.joda:joda-convert:jar:1.1"]

desc "Chat server and client written using netty"
define "Netty-Chat" do

  project.version = VERSION_NUMBER
  project.group = GROUP
  manifest["Implementation-Vendor"] = COPYRIGHT
  manifest["Main-Class"] = "rosy.async.HttpServer"
  compile.with NETTY, JSON, JODA, Dir[_() + '/src/main/lib/*']
  package(:jar)

  run.using :main => "rosy.async.Server"
end
