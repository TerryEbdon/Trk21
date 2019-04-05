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

@Newify([MockFor,Position,Coords2d])
@groovy.util.logging.Log4j2('logger')
final class RepositionerLinearTest extends RepositionerTestBase {

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
    Position shipPos = [c2d.clone(),c2d.clone()]

    MockFor shipMock = MockFor( FederationShip )
    shipMock.demand.with {
      getId                   { 'testBlocked' }
      getEnergyUsedByLastMove { 8 }
      getTracked(1)           { false }
      getPosition(2)          { shipPos }
      getWeapon(2)            { false }
      getPosition(1)          { shipPos }
    }

    trekMock.demand.with {
      getQuadrant(0)     { fakeQuadrant }
      blockedAtSector(0) { int row, int col -> assert [row, col] == [1, 2] }
      getQuadrant(0)     { fakeQuadrant }
    }

    trekMock.use {
      shipMock.use {
        Repositioner rp = new Repositioner(
          ship:     new FederationShip(),
          ui:       ui,
          quadrant: fakeQuadrant
        )
        rp.repositionShip sv
        assert rp.moveAborted    == true
        assert fakeQuadrant[c2d] == Thing.ship
        assert ui.localMsgLog    == ['blockedAtSector']
        assert ui.argsLog        == [[Thing.star, 2, 1]]
      }
    }
  }

  void testIntraQuadrant() {
    Map fakeQuadrant = [
      contains: { int x, int y -> (1..8).containsAll( x, y ) },
      isOccupied: { int z1, int z2 -> false } // empty quadrant
    ]
    final int startSectorCol       = 4                    // Start here
    final int transitSectorCol     = startSectorCol + 1   // travel through
    final int endSectorCol         = transitSectorCol + 1 // Arrive here

    fakeQuadrant[4,startSectorCol]   = Thing.ship
    fakeQuadrant[4,transitSectorCol] = Thing.emptySpace
    fakeQuadrant[4,endSectorCol]     = Thing.emptySpace

    final Coords2d c2d          = [4,startSectorCol]
    final Coords2d startSector  = c2d.clone()
    Position shipPos            = [c2d.clone(),c2d.clone()]
    final Coords2d targetSector = [4,endSectorCol]

    final int energyUse  = endSectorCol - startSectorCol
    final float wfNeeded = energyUse / 8F

    final ShipVector sv = new ShipVector( course: 1F, warpFactor: wfNeeded )

    MockFor trekMock = MockFor( Trek ) // No demnds, should never be accessed.

    MockFor shipMock = MockFor( FederationShip )
    shipMock.demand.with {
      getId                   { 'testIntraQuadrant' }
      getEnergyUsedByLastMove { energyUse }
      getTracked(1)           { false }
      getPosition(5)          { shipPos }
      getWeapon(0)            { false }   // Only called if there's a collision.
      getPosition(0..1)       { shipPos } // 0..1, depending on the log level.
    }

    TestUi ui = new TestUi()
    trekMock.use {
      shipMock.use {
        Repositioner rp = new Repositioner(
          ship:     new FederationShip(),
          ui:       ui,
          quadrant: fakeQuadrant
        )
        rp.repositionShip sv
        assert rp.moveAborted == false  // Didn't hit object or edge of quadrant.
        assert shipPos == Position( quadrant: c2d, sector: targetSector )
        assert fakeQuadrant.size() == 5 + 2 // +2 for quadrant[Coords2d]
        assert fakeQuadrant[startSector]        == Thing.emptySpace
        assert fakeQuadrant[4,transitSectorCol] == Thing.emptySpace
        assert fakeQuadrant[targetSector]       == Thing.ship
      }
    }
  }

  private ShipVector shipWarpOne( float dir ) {
    shipWarpDir 1F, dir
  }

  void testSlowBoundaryTransition() {
    Map fakeQuadrant = [
      contains  : { int z1, int z2 -> z1 in minCoord..maxCoord && z2 in minCoord..maxCoord },
      isOccupied: { int z1, int z2 -> false } // empty quadrant
    ]

    final Coords2d startSector    = [6, 3]
    Position shipPos              = [Coords2d(6,4), startSector.clone()]

    final Position toPos = [ Coords2d(7,4), startSector.clone() ]

    MockFor shipMock = MockFor( FederationShip )
    shipMock.demand.with {
      getId                   { 'testSlowBoundaryTransition' }
      getEnergyUsedByLastMove { 4 }
      getTracked(1)           { false }
      getPosition(5)          { shipPos }
      getWeapon(0)            { false }   // Only called if there's a collision.
      getPosition(7)          { shipPos }
    }
    final ShipVector sv = [course: 7F, warpFactor: 0.5F]

    fakeQuadrant[ startSector.toList() ] = Thing.ship
    for ( int row in (1 + startSector.row)..maxCoord ) {
      fakeQuadrant[row, startSector.col] = Thing.emptySpace
    }

    MockFor trekMock = MockFor( Trek ) // No demnds, should never be accessed.

    TestUi ui = new TestUi()
    trekMock.use {
      shipMock.use {
        Repositioner rp = new Repositioner(
          ship:     new FederationShip(),
          ui:       ui,
          quadrant: fakeQuadrant
        )
        rp.repositionShip sv

        assert shipPos == toPos
        assert fakeQuadrant.size() == 3 + 2 + 1// sectors lists + closures + sectors Coords2d
        assert fakeQuadrant[toPos.sector] == Thing.ship
        assert rp.moveAborted == true // Aborted in-quadrant move @ edge then jumped.
      }
    }
  }

  void testExtraQuadrant() {
    Map fakeQuadrant = [
      contains  : { int z1, int z2 -> z2 in 1..maxCoord },
      isOccupied: { int z1, int z2 -> false } // empty quadrant
    ]
    final int startSectorCol       = 4              // Start here
    final int endSectorCol         = startSectorCol // Arrive here

    fakeQuadrant[4,startSectorCol]   = Thing.ship
    for ( int col in 5..maxCoord ) {
      fakeQuadrant[4, col] = Thing.emptySpace
    }

    final Coords2d c2d            = [4,startSectorCol]
    Position shipPos              = [c2d.clone(),c2d.clone()]
    final Coords2d targetSector   = [4,endSectorCol]
    final Coords2d targetQuadrant = [4,5]
    final Position targetPosition = [quadrant: targetQuadrant, sector: targetSector]
    final int energyUse = 8
    final ShipVector sv = shipWarpOne( 1F )

    MockFor shipMock = MockFor( FederationShip )
    shipMock.demand.with {
      getId                   { 'testExtraQuadrant' }
      getEnergyUsedByLastMove { 8 }
      getTracked(1)           { false }
      getPosition(14)         { shipPos }
      getWeapon(0)            { false }   // Only called if there's a collision.
      getPosition(0..1)       { shipPos } // 0..1, depending on the log level.
    }

    MockFor trekMock = MockFor( Trek ) // No demnds, should never be accessed.

    TestUi ui = new TestUi()
    trekMock.use {
      shipMock.use {
        Repositioner rp = new Repositioner(
          ship:     new FederationShip(),
          ui:       ui,
          quadrant: fakeQuadrant
        )
        rp.repositionShip sv

        assert shipPos == targetPosition
        assert fakeQuadrant.size() == 5 + 1 + 2 // sectors[list] + sectors[c2d] + closures
        assert fakeQuadrant[targetSector] == Thing.ship // Warp 1, so move 8 sectors crossing edge.
        assert rp.moveAborted == true // Aborted in-quadrant move @ edge then jumped.
      }
    }
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
