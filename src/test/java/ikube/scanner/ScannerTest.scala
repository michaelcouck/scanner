package ikube.scanner

import org.junit.{Assert, Test}

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-11-2014
 */
class ScannerTest {

  @Test
  def scan() {
    val scanner = new Scanner()
    val addresses = scanner.scan("192.168.1.0/24", 250).toArray
    addresses.foreach(address => println(address))
    Assert.assertTrue(addresses.length > 0)
  }

}