package ikube.scanner

import java.net._
import java.util
import java.util.concurrent.{Executors, TimeUnit}

import org.apache.commons.lang.StringUtils
import org.apache.commons.net.util.SubnetUtils
import org.slf4j.LoggerFactory

import scala.concurrent._
import scala.concurrent.duration.Duration

/**
 * Bla, bla, bla...
 *
 * @author Michael Couck
 * @version 01.00
 * @since 17-11-2014
 */
class Scanner {

  val SEPARATOR_CHARACTERS = ",|; "
  val LOGGER = LoggerFactory.getLogger(this.getClass.getName)

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(100))

  def scan(addressRange: String, timeout: Integer): util.ArrayList[String] = {
    val portRange = {
      List.range(0, 65535).toArray map (_.toString)
    }
    scan(addressRange, portRange, timeout)
  }

  def scan(addressRange: String, portRange: String, timeout: Integer): util.ArrayList[String] = {
    val ports = StringUtils.split(portRange, SEPARATOR_CHARACTERS)
    scan(addressRange, ports, timeout)
  }

  def scan(addressRange: String, portRange: Array[String], timeout: Integer): util.ArrayList[String] = {
    val reachableAddresses = new util.ArrayList[String]()
    val subnetUtils = new SubnetUtils(addressRange)
    val allAddresses = subnetUtils.getInfo.getAllAddresses
    val futures = Future.traverse(allAddresses.toList)(address => Future(
      Future.traverse(InetAddress.getAllByName(address).toList)(inetAddress => Future(
        try {
          if (inetAddress.isReachable(timeout)) {
            Future.traverse(portRange.toList)(port => Future(
              try {
                val socket = new Socket()
                try {
                  socket.connect(new InetSocketAddress(address, Integer.parseInt(port)), timeout)
                  reachableAddresses.add(address + ":" + port)
                  LOGGER.debug("Connected to : " + inetAddress + ", on port : " + port)
                } catch {
                  case e: Exception => if (LOGGER.isDebugEnabled) {
                    LOGGER.error("Exception trying to connect to : " + address + ", on port : " + port, e)
                  }
                } finally {
                  socket.close()
                }
              } catch {
                case e: Exception => if (LOGGER.isDebugEnabled) {
                  LOGGER.error("Exception trying to disconnect from : " + address + ", on port : " + port, e)
                }
              }
            )).map(_.head)
          }
        }
      ))
    ))
    Await.ready(futures.map(_.head), Duration.apply(Int.MaxValue, TimeUnit.SECONDS))
    reachableAddresses
  }

}