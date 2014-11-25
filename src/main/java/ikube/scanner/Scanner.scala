package ikube.scanner

import java.net.{InetSocketAddress, Socket, InetAddress}
import java.util
import java.util.concurrent.{TimeUnit, Executors}

import org.apache.commons.lang.StringUtils
import org.apache.commons.net.util.SubnetUtils

import scala.collection.JavaConversions._
import scala.concurrent._
import scala.concurrent.duration.Duration

/**
 * This is a simple utility to scan a network and report what ports are open on the
 * scanning range specified. It is multi threaded, however there is no retry, and very
 * little exception handling.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 17-11-2014
 */
object Scanner {

  /** Characters used for splitting the port range string. */
  val SEPARATOR_CHARACTERS = ",|; "
  /**
   * We define an executor with a few mode threads as the built in one has too few for good performance in this case.
   */
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(250))

  /**
   * Main method to execute from the command line, takes the ip range, then optionally the
   * port range, and then the timeout in milliseconds for the pinging, anything else gets an
   * exception.
   *
   * @param args and array of strings, the ip range(eg. 192.168.1.0/24), optionally the
   *             port range(e.g. 0-1024) and then the timeout for the socket access in milliseconds,
   *             and the verbose flag
   */
  def main(args: Array[String]): Unit = {
    try {
      println("Scanning ip range and ports and timeout : " + args(0) + ", " + args(1))
      var addressesAndPorts: util.List[String] = null
      if (args.length == 2) {
        addressesAndPorts = scan(args(0), Integer.parseInt(args(1)), verbose = false)
      } else if (args.length == 3) {
        addressesAndPorts = scan(args(0), args(1), Integer.parseInt(args(2)), verbose = false)
      } else if (args.length == 4) {
        addressesAndPorts = scan(args(0), args(1), Integer.parseInt(args(2)), args(3).toBoolean)
      } else {
        throw new RuntimeException()
      }
      println("Open addresses and ports : ")
      addressesAndPorts.foreach {
        addressAndPort => addressesAndPorts
          println("    : " + addressAndPort)
      }
    } catch {
      case e: Exception =>
        println("Usage: java -jar scanner.jar ip-range [port-range] timeout-millis")
        throw e
    }
    System.exit(0)
  }

  /**
   * This method will scan all ports on the entire range defined in the parameter list. The range
   * is in the format "192.168.1.0/24". In this particular case the range will be 0-255 for the network
   * segment part.
   *
   * @param addressRange the range of addresses to scan for ports on the network, in the format "192.168.1.0/24"
   * @param timeout the timeout to check first for pinging the machines, then trying the individual ports
   * @return the ip addresses and ports as a list, in the format "192.168.1.1:8080"
   */
  def scan(addressRange: String, timeout: Integer, verbose: Boolean): util.List[String] = {
    val portRange = {
      List.range(0, 65535).toArray map (_.toString)
    }
    scan(addressRange, portRange, timeout, verbose)
  }

  /**
   * This method will scan a network, the addresses specified in the format "192.168.1.1/24",
   * for the port range specified in the parameter list.
   *
   * @param addressRange the range of addresses to scan for ports on the network, in the format "192.168.1.0/24"
   * @param portRange the range of ports to scan, in the format "80,8080,3306,1521,..."
   * @param timeout the timeout to check first for pinging the machines, then trying the individual ports
   * @return the ip addresses and ports as a list, in the format "192.168.1.1:8080"
   */
  def scan(addressRange: String, portRange: String, timeout: Integer, verbose: Boolean): util.List[String] = {
    val ports = StringUtils.split(portRange, SEPARATOR_CHARACTERS)
    scan(addressRange, ports, timeout, verbose)
  }

  /**
   * This method will scan a network, the addresses specified in the format "192.168.1.1/24",
   * for the port range specified in the parameter list.
   *
   * @param addressRange the range of addresses to scan for ports on the network, in the format "192.168.1.0/24"
   * @param portRange the range of ports to scan, in the format "80,8080,3306,1521,..."
   * @param timeout the timeout to check first for pinging the machines, then trying the individual ports
   * @return the ip addresses and ports as a list, in the format "192.168.1.1:8080"
   */
  def scan(addressRange: String, portRange: Array[String], timeout: Integer, verbose: Boolean): util.List[String] = {
    val reachableAddresses = new util.ArrayList[String]()
    val subnetUtils = new SubnetUtils(addressRange)
    val allAddresses = subnetUtils.getInfo.getAllAddresses
    val futures = Future.traverse(allAddresses.toList)(address => Future(
      Future.traverse(InetAddress.getAllByName(address).toList)(inetAddress => Future(
        if (inetAddress.isReachable(timeout)) {
          Future.traverse(portRange.toList)(port => Future(
            try {
              val addressAndPort = scan(address, Integer.parseInt(port), timeout, verbose)
              reachableAddresses.add(addressAndPort)
            } catch {
              case e: Exception => // Again nothing
            }
          )).map(_.head)
        }
      ))
    ))
    Await.ready(futures.map(_.head), Duration.apply(Int.MaxValue, TimeUnit.SECONDS))
    reachableAddresses
  }

  /**
   * Scans a specific address and port, returning the address and port combination
   * if the address is reachable and the port is open on the machine.
   *
   * @param address the ip address to try to connect to
   * @param port the port to connect to on the remove machine
   * @param timeout the time to wait for the socket in milliseconds
   * @return the ip address and the port concatenated if this address is reachable and open
   */
  def scan(address: String, port: Integer, timeout: Integer, verbose: Boolean): String = {
    val socket = new Socket()
    try {
      if (verbose) {
        println("    scanning : " + address + ":" + port)
      }
      socket.connect(new InetSocketAddress(address, port), timeout)
      address + ":" + port
    } catch {
      case e: Exception => throw e
    } finally {
      socket.close()
    }
  }

}