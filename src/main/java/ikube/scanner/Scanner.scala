package ikube.scanner

import java.net.{InetSocketAddress, Socket, InetAddress}
import java.util
import java.util.concurrent.{TimeUnit, Executors}

import org.apache.commons.lang.StringUtils
import org.apache.commons.net.util.SubnetUtils

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
class Scanner {

  /** Characters used for splitting the port range string. */
  val SEPARATOR_CHARACTERS = ",|; "
  /**
   * We define an executor with a few mode threads as the built in one has too few for good performance in this case.
   */
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(100))

  /**
   * This method will scan all ports on the entire range defined in the parameter list. The range
   * is in the format "192.168.1.0/24". In this particular case the range will be 0-255 for the network
   * segment part.
   *
   * @param addressRange the range of addresses to scan for ports on the network, in the format "192.168.1.0/24"
   * @param timeout the timeout to check first for pinging the machines, then trying the individual ports
   * @return the ip addresses and ports as a list, in the format "192.168.1.1:8080"
   */
  def scan(addressRange: String, timeout: Integer): util.List[String] = {
    val portRange = {
      List.range(0, 65535).toArray map (_.toString)
    }
    scan(addressRange, portRange, timeout)
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
  def scan(addressRange: String, portRange: String, timeout: Integer): util.List[String] = {
    val ports = StringUtils.split(portRange, SEPARATOR_CHARACTERS)
    scan(addressRange, ports, timeout)
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
  def scan(addressRange: String, portRange: Array[String], timeout: Integer): util.List[String] = {
    val reachableAddresses = new util.ArrayList[String]()
    val subnetUtils = new SubnetUtils(addressRange)
    val allAddresses = subnetUtils.getInfo.getAllAddresses
    val futures = Future.traverse(allAddresses.toList)(address => Future(
      Future.traverse(InetAddress.getAllByName(address).toList)(inetAddress => Future(
        if (inetAddress.isReachable(timeout)) {
          Future.traverse(portRange.toList)(port => Future(
            try {
              val socket = new Socket()
              try {
                socket.connect(new InetSocketAddress(address, Integer.parseInt(port)), timeout)
                reachableAddresses.add(address + ":" + port)
              } catch {
                case e: Exception =>
                  // Nothing
              } finally {
                socket.close()
              }
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

}