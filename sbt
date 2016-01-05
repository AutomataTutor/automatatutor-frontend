JAVA7PATH="/Library/Java/JavaVirtualMachines/jdk1.8.0_05.jdk/Contents/Home/bin/java" # Path to an installation of Java7. Later versions do not work with sbt0.12
$JAVA7PATH -version
$JAVA7PATH -Xmx1024M -Xss16M -XX:MaxPermSize=512m -XX:+CMSClassUnloadingEnabled -jar `dirname $0`/sbt-launch.jar "$@"
