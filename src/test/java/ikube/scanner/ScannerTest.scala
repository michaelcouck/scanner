package ikube.scanner

import java.util

import org.junit.{Assert, Before, Ignore, Test}

/**
  * @author Michael Couck
  * @version 01.00
  * @since 17-11-2014
  */
@Ignore
class ScannerTest {

  val timeout = "100"
  val portRange = "0-1024"
  val ipRange = "192.168.1.1/24"
  val ipAddress = "192.168.1.43"
  val port = 8500

  val scanner = Scanner

  @Before
  def before() {
    scanner.exit = false
  }

  @Test(expected = classOf[RuntimeException])
  def fail() {
    scanner.main(Array(ipRange))
  }

  @Test
  def rangeTimeout() {
    scanner.main(Array(ipRange, timeout))
    Assert.assertTrue(scanner.addressesAndPorts.size() > 0)
  }

  @Test
  def rangePortTimeout() {
    scanner.main(Array(ipRange, portRange, timeout))
    Assert.assertTrue(scanner.addressesAndPorts.size() > 0)
  }

  @Test
  def rangePortTimeoutVerbose() {
    scanner.main(Array(ipRange, portRange, timeout, "true"))
    Assert.assertTrue(scanner.addressesAndPorts.size() > 0)
  }

  @Test
  def rangePortTimeoutVerboseForce() {
    scanner.main(Array(ipRange, portRange, timeout, "false", "true"))
    Assert.assertTrue(scanner.addressesAndPorts.size() > 0)
  }

  @Test
  def scanRangeVerboseForce() {
    val addresses = scanner.scan(ipRange, Integer.parseInt(timeout), verbose = false, force = false).toArray
    addresses.foreach(address => println("Reachable address and port : " + address))
    Assert.assertTrue(addresses.nonEmpty)
  }

  @Test
  def scanSingleAddress(): Unit = {
    val addressAndPort = scanner.scanAddressPortTimeoutVerbose(ipAddress, port, 1000, verbose = true)
    Assert.assertEquals(ipAddress + ":" + port, addressAndPort)
  }

  @Test
  def networkScan() {
    val addresses = scanner.scan(ipRange, "0-65535", 102400, verbose = false, force = false).toArray
    util.Arrays.sort(addresses)
    addresses.foreach(address => println("IP & port : " + address))
  }

}