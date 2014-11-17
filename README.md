scanner
=======

Because I couldn't find a decent tool to scan networks looking for open ports, I decided to write one. Just a few lines of code, no exception handling, i.e. if it fails it fails, tant pis. No retry either as it turns out, who wants to retry 65535 * n address/port combinations, where n is the number of addresses to scan?

Having said that, it is multi threaded, which helps a lot. Note that there are at least 1000 threads created, so probably not a good idea to run it on Android.

Please be responsible, i.e. no hacking. Remember, if you aren't getting paid for it what is the point, right?
