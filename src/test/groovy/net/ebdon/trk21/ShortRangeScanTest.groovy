package net.ebdon.trk21;

import groovy.test.GroovyTestCase
import groovy.mock.interceptor.MockFor;
import groovy.transform.TypeChecked;
/**
 * @file
 * @author      Terry Ebdon
 * @date        March 2019
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
final class ShortRangeScanTest extends TrekTestBase {

  private MockFor shipMock;
  private MockFor game;
  private MockFor quadrant;

  private final String dummyCondition = 'flummoxed'
  private final String dummyQuadRow   = '. . . . . . . .'

  private final int dummyEnergy    = 12345
  private final int torpedoCount   = 99
  private final int dummyFleetSize = 88
  private final int dummySolarYear = 98765
  private final int rowsInQuad     = 8;

  private final Coords2d dummyCoords = [row:1, col:2]
  private final Position dummyPosition = [ dummyCoords.clone(), dummyCoords.clone() ]

  // @TypeChecked
  @Override void setUp() {
    super.setUp()
    game = new MockFor( TrekCalendar )
    game.demand.getCurrentSolarYear { dummySolarYear }
    quadrant = new MockFor( Quadrant )
  }

  private void resetShip() {
    shipMock = new MockFor( FederationShip )

    shipMock.demand.with {
      shortRangeScan  { galaxy -> }
      attemptDocking  { quadrant -> }
      getCondition(3) { dummyCondition }
      getPosition(1)  { dummyPosition.clone() }
      getCondition(1) { dummyCondition }
      getPosition(2)  { dummyPosition.clone() }
      getEnergyNow    { dummyEnergy }
      getNumTorpedoes { torpedoCount }
    }
  }

  @Newify(MockFor)
  @SuppressWarnings('JUnitTestMethodWithoutAssert')
  void testShortRangeScan() {
    resetShip()
    MockFor dcMock = MockFor( DamageControl )
    MockFor galaxy = MockFor( Galaxy )
    dcMock.demand.isDamaged { false }
    quadrant.demand.displayOn {
      rowsInQuad.times { ui.outln dummyQuadRow }
    }

    shipMock.use {
      quadrant.use {
          galaxy.use {
            new ShortRangeScan(
              new FederationShip(),
              new Quadrant(),
              new Galaxy(),
              ui,
              dummySolarYear,
              dummyFleetSize
            ).scan()
          }
      }
    }
    checkScanOutput()
  }

  @TypeChecked
  private void checkScanOutput() {
    ui.with {
      assert localMsgLog[0] == 'sensors.shortRange.header'
      assert localMsgLog[1] == 'sensors.shortRange.divider'

      (2..5).each {
        assert localMsgLog[it] == "sensors.shipStatus.${it - 1}"
      }

      assert msgLog == [dummyQuadRow] * rowsInQuad
    }
  }
}
