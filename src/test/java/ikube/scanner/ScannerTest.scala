package ikube.scanner

import org.junit.Test

class ScannerTest {

  @Test
  def scan() {
    var scanner = new Scanner()
    scanner.scan("192.168.1.0/24", 1000)
  }

}