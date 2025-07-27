package net.ebdon.trk21;

import static GameSpace.*;
import static Quadrant.*;
import groovy.mock.interceptor.MockFor;
import groovy.test.GroovyTestCase

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
@Newify([MockFor,Position,Coords2d])
final class RepositionerDiagonalTest extends RepositionerTestBase {

  @SuppressWarnings('JUnitTestMethodWithoutAssert')
  final void testTransitDiagonal() {
    transit 1, 1
  }

  void testTrackingImpact() {
    MockFor trekMock = MockFor( Trek )
    ShipVector sv = new ShipVector( course: 2, warpFactor: 1 )
    TestUi ui = new TestUi()
    Map fakeQuadrant = [
      contains  : { int x, int y -> (1..8).containsAll( x, y ) },
      isOccupied: { int z1, int z2 -> z2 >= 3 },
      [2,6]     : Thing.torpedo,
      [1,7]     : Thing.star
    ]

    final Coords2d c2d = [1,1]
    final Coords2d targetSector = [1,7]
    Coords2d startSector = [2, 6]

    Position torpedoPos = [ c2d.clone(), startSector.clone() ]

    MockFor torpedoMock = MockFor( Torpedo )
    torpedoMock.demand.with {
      getId                   { 'testTrackingImpact' }
      getEnergyUsedByLastMove { 8 }
      getPosition(3)          { torpedoPos }
      getPosition(1)          { torpedoPos }
    }

    trekMock.demand.with {
      getQuadrant(0)     { fakeQuadrant }
      blockedAtSector(0) { int row, int col -> assert [row, col] == [1, 2] }
      getQuadrant(0)     { fakeQuadrant }
    }

    trekMock.use {
      torpedoMock.use {
        Repositioner rp = new TorpedoRepositioner(
          ship:     new Torpedo(),
          ui:       ui,
          quadrant: fakeQuadrant
        )
        rp.repositionShip sv
        assert rp.moveAborted    == true
        assert fakeQuadrant[startSector]  == Thing.emptySpace
        assert fakeQuadrant[targetSector] == Thing.torpedo
        assert rp.thingHit == Thing.star
        assert ui.localMsgLog    == ['repositioner.position', 'impactAtSector']
        assert ui.argsLog        == [[1.2928932F, 6.7071066F], [Thing.star, 7, 1]]
      }
    }
  }

  @Override
  protected ShipVector getTransitShipVector( final float course ) {
    shipWarpDir 12F, course
  }

  @groovy.transform.TypeChecked
  @Override protected List<Integer> getExpectedTransitCoords(
      final int stepNum, final int expectedRowOffset, final int expectedColOffset ) {
    [8,8]
  }

  @Override void transit( final int expectedRowOffset, final int expectedColOffset ) {
    logger.info "Transit with expected offsets: $expectedRowOffset, $expectedColOffset"
    assert expectedRowOffset == expectedColOffset

    final float cornerToCornerDistance = Math.sqrt( 2 * maxCoord ** 2 )
    final float stepSize = Math.sin( Math.toRadians( 45 ) )
    final int maxSteps = Math.round( cornerToCornerDistance / stepSize )
    logger.info "Calling transitSteps for $maxSteps steps"
    transitSteps expectedRowOffset, expectedColOffset, maxSteps

    logger.info 'Transit with expected offsets: ' +
      "$expectedRowOffset, $expectedColOffset -- OK"
  }
}
