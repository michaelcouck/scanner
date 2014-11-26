package ikube.scanner

import org.junit.{Assert, Test}

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-11-2014
 */
class ScannerTest {

  val timeout = "250"
  val portRange = "0-1024"
  val ipRange = "192.168.1.0/24"

  val scanner = Scanner

  @Test
  def main() {
    scanner.main(Array(ipRange, timeout))
    scanner.main(Array(ipRange, portRange, timeout))
    try {
      scanner.main(Array(ipRange))
      Assert.fail("Only one argument, need at least two")
    } catch {
      case e: Exception => // Expected
    }
  }

  @Test
  def scan() {
    val addresses = scanner.scan(ipRange, Integer.parseInt(timeout), verbose = false).toArray
    addresses.foreach(address => println(address))
    Assert.assertTrue(addresses.length > 0)
  }

}