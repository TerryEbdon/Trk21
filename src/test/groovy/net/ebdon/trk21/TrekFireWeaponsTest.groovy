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
final class TrekFireWeaponsTest extends TrekTestBase {

  private MockFor shipMock;
  private MockFor battleMock;
  private MockFor fleetMock;
  private MockFor dcMock;
  private MockFor galaxyMock;
  private MockFor quadrantSetupMock;

  private final Coords2d c2d = [row:1, col:1]
  private final Position shipPosition = [c2d, c2d]

  @TypeChecked
  @Newify([MockFor,Trek])
  @Override void setUp() {
    super.setUp()
    trek              = Trek( ui )
    battleMock        = MockFor( Battle )
    shipMock          = MockFor( FederationShip )
    galaxyMock        = MockFor( Galaxy )
    quadrantSetupMock = MockFor( QuadrantSetup )
  }

  @Override void tearDown() {
    super.tearDown()
    shipMock          = null
    battleMock        = null
    fleetMock         = null
    dcMock            = null
    galaxyMock        = null
    quadrantSetupMock = null
  }

  void testFirePhasersCancel() {
    ui.inputValues = [0F]

    battleMock.use { // No demands, shouldn't be accessed
      shipMock.use { // No demands, shouldn't be accessed
        trek.ship = new FederationShip()
        trek.firePhasers()
      }
    }

    assert ui.empty
  }

  void testFireTorpedoCancel() {
    ui.inputValues = [0F]

    battleMock.use { // No demands, shouldn't be accessed
      // shipMock.use { // No demands, shouldn't be accessed
      //   trek.ship = new FederationShip()
        trek.fireTorpedo()
      // }
    }

    assert ui.empty
  }

  void testFireTorpedoWithCourse() {
    final float torpedoCourse = 1F

    ui.inputValues = [torpedoCourse]

    // fleetMock = new MockFor( EnemyFleet )

    battleMock.demand.fireTorpedo { float course ->
      assert course == torpedoCourse
    }

    battleMock.use {
      // shipMock.use { // No demands, shouldn't be accessed
        // trek.ship = new FederationShip()
        // dcMock.use {
          // fleetMock.use {
            trek.fireTorpedo()
          // }
        // }
      // }
    }

    assert ui.empty
  }

  void testFirePhasersWithTooMuchEnergy() {
    final float phaserEnergy = 4000F

    ui.inputValues = [phaserEnergy]

    shipMock.demand.getEnergyNow { phaserEnergy - 1 }

    battleMock.use { // No demands, shouldn't be accessed
      shipMock.use {
        trek.ship = new FederationShip()
        trek.firePhasers()
      }
    }

    assert ui.inputValues.empty
    assert ui.msgLog.empty
    assert ui.localMsgLog == [ 'phaser.refused.badEnergy' ]
  }

  void testFirePhasersWithGoodEnergy() {
    final float phaserEnergy = 1F

    ui.inputValues = [phaserEnergy]

    fleetMock = new MockFor( EnemyFleet )
    fleetMock.demand.getNumKlingonBatCrInQuad { 1 }

    shipMock.demand.with {
      getEnergyNow { phaserEnergy }
      getPosition { shipPosition }
    }
    battleMock.demand.phaserAttackFleet { float energy ->
      assert energy == phaserEnergy
    }

    galaxyMock.demand.with {
      getAt(2) { Coords2d shipQuad -> assert shipQuad == c2d; 200 }
      putAt    { Coords2d shpQd, int val -> assert shpQd == c2d && val == 0 }
      getAt(1) { Coords2d shipQuad -> assert shipQuad == c2d; 0 }
      putAt    { Coords2d shpQd, int val -> assert shpQd == c2d && val == 100 }
    }
    quadrantSetupMock.demand.updateAfterSkirmish { }

    battleMock.use {
      shipMock.use {
        trek.ship = new FederationShip()
        fleetMock.use {
          trek.enemyFleet = new EnemyFleet()
          galaxyMock.use {
            trek.galaxy = new Galaxy()
            quadrantSetupMock.use {
              trek.firePhasers()
            }
          }
        }
      }
    }

    assert ui.empty
  }
}