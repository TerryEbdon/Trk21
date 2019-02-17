package net.ebdon.trk21;

import static GameSpace.*
import static Quadrant.*

@groovy.util.logging.Log4j2('logger')
final class RepositionerLinearTest extends RepositionerTestBase {
// if ( notYetImplemented() ) return

  private def getLog() {logger}

  private void setupAtCentre() {
    setupAt 4, 4, 4, 4
  }

  void testBlocked() {
    logger.info 'testBlocked'

    setupAt( *topLeftCoords, *topLeftCoords )
    minCoord.upto(maxCoord) { row ->
      minCoord.upto(maxCoord) { col ->
        trek.with {
          quadrant[row,col] = (row * col == 1 ? Thing.ship : Thing.star ) // fill quadrant with stars
        }
      }
    }
    trek.quadrant.dump()
    ShipVector sv = shipWarpOne(1)
    repositioner.repositionShip sv
    trek.with {
      assertTrue moveBlocked
      assertTrue 'Ship in wrong quadrant', [entQuadX, entQuadY] == topLeftCoords
      assertTrue 'Ship in wrong sector',   [entSectX, entSectY] == topLeftCoords
      quadrant.dump()
      assertTrue 'Ship doesn\'t occupy sector', quadrant[topLeftCoords] == Thing.ship
    }
    logger.info 'testBlocked OK'
  }

  void testIntraQuadrant() {
    logger.info 'intraQuadrantTest'

    setupAtCentre()
    ShipVector sv = new ShipVector()
    sv.course = 1
    sv.warpFactor = 0.5
    trek.with {
      ship.energyUsedByLastMove = 2
      repositioner.repositionShip sv
      assertEquals "In wrong quadrant", [4,4], [entQuadX,entQuadY]
      assertEquals "In wrong sector",   [4,6], [entSectX,entSectY]
      assertEquals Thing.ship, quadrant[4,6]
    }
    logger.info 'intraQuadrantTest OK'
  }

  private ShipVector shipWarpOne( dir ) {
    shipWarpDir 1, dir
  }

  void testSlowBoundaryTransition() {
    logger.info 'testSlowBoundaryTransition...'
    setupAt 6,4,6,3
    trek.ship.energyUsedByLastMove = 4
    ShipVector sv = new ShipVector().tap {
      course = 7
      warpFactor = 0.5
      assert isValid()
    }

    logger.info "sv: $sv"
    final def targetQuadrant  = [trek.entQuadX + 1, trek.entQuadY]
    final def targetSector    = [trek.entSectX,     trek.entSectY]
    repositioner.repositionShip sv

    trek.with {
      assertEquals "In wrong quadrant", targetQuadrant, [entQuadX,entQuadY]

      assertEquals "In wrong sector",     targetSector, [entSectX,entSectY]
      assertEquals "Ship not in sector",  Thing.ship,   quadrant[targetSector]
    }
    logger.info 'testSlowBoundaryTransition -- OK'
  }

  void testExtraQuadrant() {
    logger.info 'extraQuadrantTest'
    setupAtCentre()
    ShipVector sv = shipWarpOne( 1 )
    trek.with {
      ship.energyUsedByLastMove = 8
      final def targetSector = [entSectX, entSectY]
      repositioner.repositionShip sv
      quadrant.dump()
      assertEquals "In wrong quadrant",   [4,5],        [entQuadX,entQuadY]

      assertEquals "In wrong sector",     targetSector, [entSectX,entSectY]
      assertEquals "Ship not in sector",  Thing.ship,   quadrant[targetSector]
    }
    logger.info 'extraQuadrantTest -- OK'
  }

  void testTransitGalaxy() {
    logger.info 'testTransitGalaxy'

    // [0,1].permutations().eachCombination { courseIncrements ->
    [0,1].eachPermutation() { courseIncrements ->
      logger.info "Transit with offsets of $courseIncrements"
      transit( *courseIncrements )
      logger.info "Transit with offsets of $courseIncrements -- OK"
    }
    logger.info 'testTransitGalaxy OK'
  }

  @Override
  protected ShipVector getTransitShipVector( final course ) {
    shipWarpOne course
  }

  @Override
  protected def getExpectedTransitCoords( final int stepNum, final expectedRowOffset, final expectedColOffset ) {
    final int expectedRow = 1 + stepNum * expectedRowOffset
    final int expectedCol = 1 + stepNum * expectedColOffset
    [expectedRow, expectedCol]
  }

  final void transit( final expectedRowOffset, final expectedColOffset ) {
    logger.info sprintf( "Transit with expected offsets: %d, %d",
      expectedRowOffset, expectedColOffset )

    assert expectedRowOffset != expectedColOffset // Not [0,0] or [1,1]
    final int maxSteps = maxCoord - 1
    logger.info "Calling transitSteps for $maxSteps steps"
    transitSteps expectedRowOffset, expectedColOffset, maxSteps
    logger.info sprintf( "Transit with expected offsets: %d, %d -- OK",
      expectedRowOffset, expectedColOffset )
  }
}
