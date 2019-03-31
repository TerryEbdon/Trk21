package net.ebdon.trk21;

import static Quadrant.*;
import org.apache.logging.log4j.Level;

/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
 * @copyright   Terry Ebdon, 2019
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@groovy.util.logging.Log4j2('logger')
abstract class RepositionerTestBase extends GroovyTestCase {

  abstract protected void transit( final int expectedRowOffset, final int expectedColOffset );

  final String errTransitBadPos    = 'In wrong quadrant %s at q.step %2d with offsets of [%d, %d]';
  private final String msgTransitTestStart = 'Transit in at most %2d steps with ShipVector: %s';
  private final String msgTransitTestEnd   = 'Transit in at most %2d steps with ShipVector: %s -- OK';
  final String msgQstep            = 'q.step %2d';
  final String msgQuad             = 'Quadrant [%2d, %2d]';
  final String msgSect             = 'Sector [%2d, %2d]';
  final String msgStartStepQuad    = "$msgQstep starting with  %s";
  final String msgStepNowIn        = "$msgQstep:        now in %s";
  final String msgStepExpectIn     = "$msgQstep:  should be in %s";
  final String msgEndStepQuad      = "$msgQstep: finished with %s";
  final String msgSetupAt          = "SetupAt $msgQuad $msgSect";

  TrekMock trek;
  Repositioner repositioner;

  @groovy.transform.TypeChecked
  final void setUp() {
    trek = new TrekMock();
    repositioner = new Repositioner( trek )
  }

  @groovy.transform.TypeChecked
  private float getCourseFrom( final int expectedRowOffset, final int expectedColOffset ) {
    final Map course = [
      [0,1]: 1F,   // "East"
      [1,0]: 7F,   // "South"
      [1,1]: 8F    // "South-East"
    ]

    final float rv = course[ expectedRowOffset, expectedColOffset ]
    logger.info "Expected course is $rv"
    rv
  }

  @groovy.transform.TypeChecked
  final ShipVector shipWarpDir( final float warp, final float dir ) {
    assert warp > 0F && warp < 13F
    assert dir  > 0F && dir  < 8.01F /// @todo Max course is weird.

    trek.ship.energyUsedByLastMove = warp * 8
    new ShipVector().tap {
      course      = dir
      warpFactor  = warp
      assert isValid()
    }
  }

  @groovy.transform.TypeChecked
  final void setupAt( final int qRow, final int qCol, final int sRow, final int sCol ) {
    logger.printf Level.INFO, msgSetupAt, qRow, qCol, sRow, sCol
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

  abstract protected ShipVector getTransitShipVector( final float course ) ;
  abstract protected List<Integer> getExpectedTransitCoords(
      final int stepNum,
      final int expectedRowOffset,
      final int expectedColOffset );

  final protected void transitSteps(
      final int expectedRowOffset, final int expectedColOffset, final int maxSteps ) {
    logger.info "transitSteps called with $expectedRowOffset, $expectedColOffset, $maxSteps"
    final float course = getCourseFrom( expectedRowOffset, expectedColOffset )
    // final boolean isOneOne = expectedRowOffset * expectedColOffset
    // ShipVector sv = isOneOne ? shipWarpDir( 12, course ) : shipWarpOne( course )
    ShipVector sv = getTransitShipVector( course )

    logger.printf Level.INFO, msgTransitTestStart, maxSteps, sv
    setupAt 1, 1, 1, 1
    1.upto(maxSteps) { stepNum ->
      logger.printf Level.INFO, msgStartStepQuad, stepNum, trek.ship.position
      logger.trace trek.ship

      repositioner.repositionShip sv
      trek.with {
        int expectedRow, expectedCol
        (expectedRow,expectedCol) = getExpectedTransitCoords(
            stepNum, expectedRowOffset, expectedColOffset )

        logger.printf Level.INFO, msgStepNowIn,    stepNum, ship.position
        logger.printf Level.INFO, msgStepExpectIn, stepNum, ship.position.quadrant

        final String badQuadRow = sprintf( errTransitBadPos,
            'row',stepNum, expectedRowOffset, expectedColOffset )
        final String badQuadCol = sprintf( errTransitBadPos,
            'col',stepNum, expectedRowOffset, expectedColOffset )

        assertEquals badQuadRow, expectedRow, entQuadX
        assertEquals badQuadCol, expectedCol, entQuadY
        assertEquals "In wrong quadrant col at q.step $stepNum", expectedCol, entQuadY /// @todo delete this line
        logger.printf Level.INFO, msgEndStepQuad, stepNum, trek.ship.position
      }
    }
    logger.printf Level.INFO, msgTransitTestEnd, maxSteps, sv
  }
}
