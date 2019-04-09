package net.ebdon.trk21;

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
final class TrekShortRangeScanTest extends TrekTestBase {

  private MockFor shipMock;
  private MockFor game;
  private MockFor enemyFleet;

  private final String dummyCondition = 'flummoxed'

  private final int dummyEnergy    = 12345
  private final int torpedoCount   = 99
  private final int dummyFleetSize = 88
  private final int dummySolarYear = 98765

  private final Coords2d dummyCoords = [row:1, col:2]
  private final Position dummyPosition = [ dummyCoords.clone(), dummyCoords.clone() ]

  // @TypeChecked
  @Override void setUp() {
    super.setUp()
    game = new MockFor( TrekCalendar )
    game.demand.getCurrentSolarYear { dummySolarYear }
    enemyFleet = new MockFor( EnemyFleet )
    enemyFleet.demand.getNumKlingonBatCrRemain { dummyFleetSize }
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
  void testOffline() {
    MockFor dcMock = MockFor( DamageControl )
    dcMock.demand.isDamaged { true }

    MockFor quadMock = MockFor( Quadrant ) // No demands, shouldn't be called.
    shipMock = MockFor( FederationShip )   // No demands, shouldn't be called.
    dcMock.use {
      trek = new Trek( ui )
      trek.damageControl = new DamageControl()
      quadMock.use {
        trek.quadrant = new Quadrant()
        shipMock.use {
          trek.ship = new FederationShip()
          trek.shortRangeScan()
        }
      }
    }
    assert ui.localMsgLog == ['sensors.shortRange.offline']
  }

  @Newify(MockFor)
  @SuppressWarnings('JUnitTestMethodWithoutAssert')
  void testShortRangeScan() {
    resetShip()
    MockFor dcMock = MockFor( DamageControl )
    dcMock.demand.isDamaged { false }

    enemyFleet.use {
      game.use {
        trek = new Trek( ui )
        trek.quadrant.clear()
        trek.enemyFleet = new EnemyFleet()

        shipMock.use {
          trek.ship = new FederationShip()
          game.use {
            trek.game = new TrekCalendar()
            dcMock.use {
              trek.damageControl = new DamageControl()
              trek.shortRangeScan()
            }
          }
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

      assert msgLog == ['. . . . . . . . '] * 8
      // assert msgLog[8].contains( "STARDATE: $dummySolarYear" )
      // assert msgLog[8].contains( "CONDITION: $dummyCondition" )
      // assert msgLog[9].contains( 'QUADRANT:' )
      // assert msgLog[9].contains( "SECTOR: $dummyCoords.col - $dummyCoords.row" )
      // assert msgLog[10].contains( "ENERGY: $dummyEnergy" )
      // assert msgLog[10].contains( "PHOTON TORPEDOES: $torpedoCount" )
      // assert msgLog[11].contains( "KLINGONS:    $dummyFleetSize" )
    }
  }
}
