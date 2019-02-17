package net.ebdon.trk21;

final class ShipVectorTest extends GroovyTestCase {
  ShipVector sv = new ShipVector();

  void testGoodValues() {
    def rnd = new Random()
    for ( float course = 1; course < 8; course += rnd.nextInt( 9 ) / 10  ) {
      1.upto(12) { warp ->
        sv = new ShipVector( course: course, warpFactor: warp )
        assertEquals(
          "$sv",
          true,
          sv.isValid()
        )
      }
    }
  }

  void testBadvalues() {
    sv.with {
      assertFalse "$sv", isValid()  // Fail: All zero values
      course = 1
      [ 0.25,   // WF 0.25 is used to move 2 sectors.
        0.5,    // WF 0.25 is used to move 4 sectors.
        1       // WF 0.25 is used to move 1 quadrant (8 sectors).
      ].each { wf ->
        warpFactor = wf
        assertTrue "$sv", isValid()
      }
    }
  }
}
