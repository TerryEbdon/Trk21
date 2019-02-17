package net.ebdon.trk21;

import static GameSpace.*

@groovy.util.logging.Log4j2('logger')
/// todo Lots of `1` and `8` instances are hard-coded in this class.
final class GameSpaceTest extends GroovyTestCase {

  void testSectorIsInsideQuadrantGood() {
    logger.info 'testSectorIsInsideQuadrantGood'

    minCoord.upto(maxCoord) {
      minCoord.upto(maxCoord) { jt ->
        assertTrue sectorIsInsideQuadrant( [ it, jt ] ) // Good coords
      }
    }
  }

  void testSectorIsInsideQuadrantBad() {
    logger.info 'testSectorIsInsideQuadrantBad'

    minCoord.upto(maxCoord) {
      minCoord.upto(maxCoord) { jt ->
        assertFalse sectorIsInsideQuadrant( [-it,-jt ] ) // fail
        assertFalse sectorIsInsideQuadrant( [-it, jt ] ) // fail
        assertFalse sectorIsInsideQuadrant( [ it,-jt ] ) // fail
      }

      [-it,0].permutations {
        assertFalse sectorIsInsideQuadrant( it )
      }
    }

    [-9,0].permutations().eachCombination {
      assertFalse sectorIsInsideQuadrant( it )
    }
  }

  void testDistance() {
    Coords2d posFrom  = new Coords2d( row: 1, col: 2 )
    Coords2d posTo    = new Coords2d( row: 8, col: 8 )
    logger.info "Calc distance from $posFrom to $posTo"
    final float distance = distanceBetween( posFrom, posTo )
    logger.info sprintf('Distance is: %+1.6f', distance )
  }
}
