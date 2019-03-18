package net.ebdon.trk21;

import static GameSpace.*;
import static Quadrant.*;
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
      assert moveBlocked
      assert [entQuadX, entQuadY] == topLeftCoords
      assert [entSectX, entSectY] == topLeftCoords
      quadrant.dump()
      assert quadrant[topLeftCoords] == Thing.ship
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
      assert [entQuadX,entQuadY] == [4,4]
      assert [entSectX,entSectY] == [4,6]
      assert quadrant[4,6] == Thing.ship
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
      assert [entQuadX,entQuadY] == targetQuadrant
      assert [entSectX,entSectY] == targetSector
      assert quadrant[targetSector] == Thing.ship
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
      assert [entQuadX,entQuadY] == [4,5]
      assert [entSectX,entSectY] == targetSector
      assert quadrant[targetSector] == Thing.ship
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
