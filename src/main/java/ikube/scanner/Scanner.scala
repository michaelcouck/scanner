package ikube.scanner

import java.net._
import java.util.concurrent.{TimeUnit, Executors}

import org.apache.commons.lang.StringUtils
import org.apache.commons.net.util.SubnetUtils
import org.slf4j.LoggerFactory

import scala.collection.immutable.IntMap.Nil
import scala.concurrent._
import scala.concurrent.duration.Duration

/**
 * Bla, bla, bla...
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
class Scanner {

  var SEPERATOR_CHARACTERS = ",|; "
  var LOGGER = LoggerFactory.getLogger(this.getClass.getName)

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1000))

  def scan(addressRange: String, timeout: Integer) {
    val portRange = {
      List.range(0, 65535).toArray map (_.toString)
    }
    scan(addressRange, portRange, timeout)
  }

  def scan(addressRange: String, portRange: String, timeout: Integer) {
    val ports = StringUtils.split(portRange, SEPERATOR_CHARACTERS)
    scan(addressRange, ports, timeout)
  }

  def scan(addressRange: String, portRange: Array[String], timeout: Integer) {
    val subnetUtils = new SubnetUtils(addressRange)
    val allAddresses = subnetUtils.getInfo.getAllAddresses
    val futures = Future.traverse(allAddresses.toList)(address => Future(
      Future.traverse(InetAddress.getAllByName(address).toList)(inetAddress => Future(
        Future.traverse(portRange.toList)(port => Future(
          try {
            val socket = new Socket()
            socket.connect(new InetSocketAddress(address, Integer.parseInt(port)), timeout)
            LOGGER.error("Connected to : " + inetAddress + ", on port : " + port)
            socket.close()
            inetAddress
          } catch {
            case e: Exception => if (LOGGER.isDebugEnabled) {
              LOGGER.error("Exception trying to connect to : " + address + ", on port : " + port, e)
            }
          }
        )).map(_.head)
      ))
    ))
    Await.ready(futures.map(_.head), Duration.apply(Int.MaxValue, TimeUnit.SECONDS))
  }

}