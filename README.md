scanner
=======

Because I couldn't find a decent tool to scan networks looking for open ports, I decided to write one. Just a few lines of code, no exception handling, i.e. if it fails it fails, tant pis. No retry either as it turns out, who wants to retry 65535 * n address/port combinations, where n is the number of addresses to scan?

Having said that, it is multi threaded, which helps a lot. Note that there are at least 1000 threads created, so probably not a good idea to run it on Android.

Please be responsible, i.e. no hacking. Remember, if you aren't getting paid for it what is the point, right?

So, to run the scanner from the command line:

=> java -jar scanner.jar ip-range [port-range (eg. 0-1024, optional)] timeout(in milliseconds)

There must be a lib folder in the execution folder, with the dependencies:

* commons-net-2.0.jar
* commons-lang-2.6.jar
* scala-library-2.11.0-M3.jar
* scala-reflect-2.11.0-M3.jar

To use from a Java/Scala app just instantiate the class new Scanner() and call whatever methods you want to. Three methods, one that takes an ip range and scans the entire network range, every port. One that takes the ip range and a port range, also the entire network range but only the ports specified, and one that takes the ip range, a list of specified ports and a timeout, which scans only the ports defined on the range defined.

JavaDoc all done of course, with live examples in the unit test.
