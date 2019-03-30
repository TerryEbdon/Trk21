package net.ebdon.trk21;

import static GameSpace.*;
import static Quadrant.*;
import groovy.mock.interceptor.MockFor;

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

@Newify(MockFor)
@groovy.util.logging.Log4j2('logger')
final class RepositionerLinearTest extends RepositionerTestBase {

  private void setupAtCentre() {
    setupAt 4, 4, 4, 4
  }

  @Newify([Position,Coords2d])
  void testBlocked() {
    MockFor trekMock = MockFor( Trek )
    ShipVector sv = shipWarpOne( 1F )
    TestUi ui = new TestUi()
    Map fakeQuadrant = [
      contains  : { int z1, int z2 -> true },
      isOccupied: { int z1, int z2 -> true },
      [1,1]     : Thing.ship,
      [1,2]     : Thing.star
    ]

    final Coords2d c2d = [1,1]
    Position shipPos = [c2d,c2d.clone()]

    Map fakeShip = [energyUsedByLastMove: 8, position: shipPos]

    trekMock.demand.with {
      getShip         { fakeShip }
      getQuadrant(4)  { fakeQuadrant }
      blockedAtSector { int row, int col -> assert [row, col] == [1, 2] }
      getQuadrant     { fakeQuadrant }
    }

    trekMock.use {
      Repositioner rp = new Repositioner()
      rp.trek = new Trek( ui )
      rp.repositionShip sv
      assert rp.moveAborted == true
      assert fakeQuadrant[1,1] == Thing.ship
    }
  }

  void testBlockedOld() {
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

  void testIntraQuadrantNew() {
    Map fakeQuadrant = [
      contains: { int z1, int z2 ->   true }, // Should never leave quadrant
      isOccupied: { int z1, int z2 -> false } // empty quadrant
    ]
    final int startSectorCol       = 4                    // Start here
    final int transitSectorCol     = startSectorCol + 1   // travel through
    final int endSectorCol         = transitSectorCol + 1 // Arrive here

    fakeQuadrant[4,startSectorCol]   = Thing.ship
    fakeQuadrant[4,transitSectorCol] = Thing.emptySpace
    fakeQuadrant[4,endSectorCol]     = Thing.emptySpace

    final Coords2d c2d          = [4,startSectorCol]
    Position shipPos            = [c2d,c2d.clone()]
    final Coords2d targetSector = [4,endSectorCol]

    final int energyUse  = endSectorCol - startSectorCol
    final float wfNeeded = energyUse / 8F

    final ShipVector sv = new ShipVector( course: 1F, warpFactor: wfNeeded )
    Map fakeShip = [energyUsedByLastMove: energyUse, position: shipPos]

    MockFor trekMock = MockFor( Trek )
    trekMock.demand.with {
      getShip         { fakeShip }
      getQuadrant(6)  { fakeQuadrant }
    }

    TestUi ui = new TestUi()
    trekMock.use {
      Repositioner rp = new Repositioner()
      rp.trek = new Trek( ui )
      rp.repositionShip sv
      assert rp.moveAborted == false
      assert fakeShip.size() == 2
      assert fakeShip.position == new Position( quadrant: c2d, sector: targetSector )
      assert fakeQuadrant.size() == 5
      assert fakeQuadrant[4,startSectorCol]   == Thing.emptySpace
      assert fakeQuadrant[4,transitSectorCol] == Thing.emptySpace
      assert fakeQuadrant[4,endSectorCol]     == Thing.ship
      }
  }

  void testIntraQuadrantOld() {
    logger.info 'intraQuadrantTest'

    setupAtCentre()
    ShipVector sv = new ShipVector()
    sv.course = 1F
    sv.warpFactor = 0.5F
    trek.with {
      ship.energyUsedByLastMove = 2
      repositioner.repositionShip sv
      assert [entQuadX,entQuadY] == [4,4]
      assert [entSectX,entSectY] == [4,6]
      assert quadrant[4,6] == Thing.ship
    }
    logger.info 'intraQuadrantTest OK'
  }

  private ShipVector shipWarpOne( float dir ) {
    shipWarpDir 1F, dir
  }

  void testSlowBoundaryTransition() {
    logger.info 'testSlowBoundaryTransition...'
    setupAt 6,4,6,3
    trek.ship.energyUsedByLastMove = 4
    ShipVector sv = new ShipVector().tap {
      course = 7F
      warpFactor = 0.5F
      assert isValid()
    }

    logger.info "sv: $sv"
    final List<Integer> targetQuadrant  = [trek.entQuadX + 1, trek.entQuadY]
    final List<Integer> targetSector    = [trek.entSectX,     trek.entSectY]
    repositioner.repositionShip sv

    trek.with {
      assert [entQuadX,entQuadY] == targetQuadrant
      assert [entSectX,entSectY] == targetSector
      assert quadrant[targetSector] == Thing.ship
    }
    logger.info 'testSlowBoundaryTransition -- OK'
  }

  @groovy.transform.TypeChecked
  void testExtraQuadrant() {
    logger.info 'extraQuadrantTest'
    setupAtCentre()
    trek.with {
      ship.energyUsedByLastMove = 8
      final List targetSector = [entSectX, entSectY]
      repositioner.repositionShip shipWarpOne( 1F )
      quadrant.dump()
      assert [entQuadX,entQuadY] == [4,5]
      assert [entSectX,entSectY] == targetSector
      assert quadrant[targetSector] == Thing.ship
    }
    logger.info 'extraQuadrantTest -- OK'
  }

  @SuppressWarnings('JUnitTestMethodWithoutAssert') // asserts are in transit()
  void testTransitGalaxy() {
    logger.info 'testTransitGalaxy'

    [0,1].eachPermutation { List<Integer> courseIncrements ->
      transit( *courseIncrements )
    }
    logger.info 'testTransitGalaxy OK'
  }

  @groovy.transform.TypeChecked
  @Override protected ShipVector getTransitShipVector( final float course ) {
    shipWarpOne course
  }

  @groovy.transform.TypeChecked
  @Override protected List<Integer> getExpectedTransitCoords(
        final int stepNum, final int expectedRowOffset, final int expectedColOffset ) {
    final int expectedRow = 1 + stepNum * expectedRowOffset
    final int expectedCol = 1 + stepNum * expectedColOffset
    [expectedRow, expectedCol]
  }

  @Override protected void transit( final int expectedRowOffset, final int expectedColOffset ) {
    logger.info 'Transit with expected offsets: {}, {}',
      expectedRowOffset, expectedColOffset

    assert expectedRowOffset != expectedColOffset // Not [0,0] or [1,1]
    final int maxSteps = maxCoord - 1
    logger.info "Calling transitSteps for $maxSteps steps"
    transitSteps expectedRowOffset, expectedColOffset, maxSteps
    logger.info 'Transit with expected offsets: {}, {} -- OK',
      expectedRowOffset, expectedColOffset
  }
}
