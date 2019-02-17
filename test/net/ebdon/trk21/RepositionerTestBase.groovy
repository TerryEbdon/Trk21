package net.ebdon.trk21;

import static Quadrant.*

@groovy.util.logging.Log4j2('logger')
abstract class RepositionerTestBase extends GroovyTestCase {

  abstract protected void transit( final expectedRowOffset, final expectedColOffset ) ;

  final String errTransitBadPos    = 'In wrong quadrant %s at q.step %2d with offsets of [%d, %d]'
  final String msgTransitTestStart = "Transit in at most %2d steps with ShipVector: %s"
  final String msgTransitTestEnd   = "Transit in at most %2d steps with ShipVector: %s -- OK"
  final String msgQstep            = "q.step %2d"
  final String msgQuad             = "Quadrant [%2d, %2d]"
  final String msgSect             = "Sector [%2d, %2d]"
  final String msgStartStepQuad    = "$msgQstep starting with  $msgQuad $msgSect"
  // final String msgStepIn           = "$msgQstep starting with  $msgQuad $msgSect"
  final String msgStepNowIn        = "$msgQstep:        now in $msgQuad $msgSect"
  final String msgStepExpectIn     = "$msgQstep:  should be in $msgQuad"
  final String msgEndStepQuad      = "$msgQstep: finished with $msgQuad $msgSect"
  final String msgSetupAt          = "SetupAt $msgQuad $msgSect"

  TrekMock trek;
  Repositioner repositioner;

  final void setUp() {
    trek = new TrekMock();
    repositioner = new Repositioner( trek )
  }

  final def getCourseFrom( expectedRowOffset, expectedColOffset ) {

    final def course = [
      [0,1]: 1,   // "East"
      [1,0]: 7,   // "South"
      [1,1]: 8    // "South-East"
    ]

    def rv = course[ expectedRowOffset, expectedColOffset ]
    logger.info "Expected course is $rv"
    rv
  }

  final ShipVector shipWarpDir( warp, dir ) {
    assert warp > 0 && warp << 13
    assert dir > 0 && dir < 8.01 /// @todo Max course is weird.

    trek.ship.energyUsedByLastMove = warp * 8
    new ShipVector().tap {
      course      = dir
      warpFactor  = warp
      assert isValid()
    }
  }

  final void setupAt( qRow, qCol, sRow, sCol ) {
    logger.debug sprintf( msgSetupAt, qRow, qCol, sRow, sCol )
    // logger.debug "SetupAt $qRow, $qCol, $sRow, $sCol"
    trek.with {
      entQuadX = qRow
      entQuadY = qCol
      entSectX = sRow
      entSectY = sCol

      galaxy.clear()
      quadrant.clear()
      quadrant[entSectX,entSectY] = Thing.ship
    }
  }

  abstract protected ShipVector getTransitShipVector( final course ) ;
  abstract protected def getExpectedTransitCoords(
      final int stepNum,
      final expectedRowOffset,
      final expectedColOffset );

  final protected void transitSteps( final expectedRowOffset, final expectedColOffset, final maxSteps ) {
    logger.info "transitSteps called with $expectedRowOffset, $expectedColOffset, $maxSteps"
    final def course = getCourseFrom( expectedRowOffset, expectedColOffset )
    final boolean isOneOne = expectedRowOffset * expectedColOffset
    // ShipVector sv = isOneOne ? shipWarpDir( 12, course ) : shipWarpOne( course )
    ShipVector sv = getTransitShipVector( course )

    // logger.info "Transit in at most $maxSteps steps with ShipVector: $sv"
    logger.info sprintf( msgTransitTestStart, maxSteps, sv )
    setupAt 1, 1, 1, 1
    1.upto(maxSteps) { stepNum ->
      logger.info sprintf(
        msgStartStepQuad, stepNum,
        trek.entQuadX, trek.entQuadY, trek.entSectX, trek.entSectY )
      // logger.info "q.step $stepNum starting with Quadrant x,y: " +
      //   "${[trek.entQuadX,trek.entQuadY]} " +
      //   "sector x,y: ${[trek.entQuadX,trek.entQuadY]}"
      repositioner.repositionShip sv
      trek.with {
        // final int expectedRow = 1 + stepNum * expectedRowOffset
        // final int expectedCol = 1 + stepNum * expectedColOffset
        int expectedRow, expectedCol
        (expectedRow,expectedCol) = getExpectedTransitCoords(
            stepNum, expectedRowOffset, expectedColOffset )

        logger.info sprintf( msgStepNowIn, stepNum,
            entQuadX, entQuadY, entSectX, entSectY )
        logger.info sprintf( msgStepExpectIn, stepNum, entQuadX, entQuadY )

        final String badQuadRow = sprintf( errTransitBadPos,
            'row',stepNum, expectedRowOffset, expectedColOffset )
        final String badQuadCol = sprintf( errTransitBadPos,
            'col',stepNum, expectedRowOffset, expectedColOffset )

        assertEquals badQuadRow, expectedRow, entQuadX
        assertEquals badQuadCol, expectedCol, entQuadY
        assertEquals "In wrong quadrant col at q.step $stepNum", expectedCol, entQuadY /// @todo delete this line
        logger.info sprintf( msgEndStepQuad, stepNum,
          entQuadX, entQuadY, entSectX, entSectY )
      }
    }
    // logger.info 'transitSteps -- OK'
    logger.info sprintf( msgTransitTestEnd, maxSteps, sv )
  }
}
