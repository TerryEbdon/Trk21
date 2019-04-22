package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
import groovy.transform.TypeChecked;
import net.ebdon.trk21.battle_management.AfterSkirmish
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
  private final Position shipPosition = [c2d.clone(), c2d.clone()]

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

  @Newify(MockFor)
  void testFirePhasers() {
    MockFor pmMock = MockFor( PhaserManager )
    MockFor asMock = MockFor( AfterSkirmish )
    shipMock.demand.with {
      getPosition { shipPosition }
    }
    pmMock.demand.fire { true }
    asMock.demand.updateQuadrant { Quadrant.Thing thingDestroyed, EnemyFleet enemyFleet ->
      assert thingDestroyed == Quadrant.Thing.emptySpace
    }
    fleetMock = new MockFor( EnemyFleet )

    shipMock.use {
      trek.ship = new FederationShip()
      fleetMock.use {
        pmMock.use {
          asMock.use {
            trek.enemyFleet = new EnemyFleet()
            trek.firePhasers()
          }
        }
      }
    }

    assert ui.empty
  }

  @Newify(MockFor)
  void testFireTorpedoCancel() {

    MockFor tmMock = MockFor( TorpedoManager )
    MockFor asMock = MockFor( AfterSkirmish )
    tmMock.demand.fire{ Quadrant quad ->  false }
    tmMock.use {
      asMock.use { // No demands, shouldn't be called.
        trek.fireTorpedo()
      }
    }

    assert ui.empty
  }

  @Newify(MockFor)
  void testFireTorpedo() {
    MockFor tmMock = MockFor( TorpedoManager )
    MockFor asMock = MockFor( AfterSkirmish )
    tmMock.demand.fire{ Quadrant quad ->  true }
    tmMock.demand.getThingDestroyed { Quadrant.Thing.star }
    asMock.demand.updateQuadrant { Quadrant.Thing thingDestroyed, EnemyFleet enemyFleet ->
      assert thingDestroyed == Quadrant.Thing.star
    }
    shipMock.demand.with {
      getPosition { shipPosition }
    }

    tmMock.use {
      asMock.use {
        shipMock.use {
          trek.ship = new FederationShip()
          trek.fireTorpedo()
        }
      }
    }

    assert ui.empty
  }

  void testFirePhasersCancelOld() {
    ui.inputValues = [0F]

    battleMock.use { // No demands, shouldn't be accessed
      shipMock.use { // No demands, shouldn't be accessed
        trek.ship = new FederationShip()
        trek.firePhasers()
      }
    }

    assert ui.empty
  }

  void testFireTorpedoCancelOld() {
    ui.inputValues = [0F]

    battleMock.use { // No demands, shouldn't be accessed
      // shipMock.use { // No demands, shouldn't be accessed
      //   trek.ship = new FederationShip()
        trek.fireTorpedo()
      // }
    }

    assert ui.empty
  }

  @Newify([MockFor, Coords2d])
  void testFireTorpedoMissed() {
    final float torpedoCourse = 1F

    ui.inputValues = [torpedoCourse]

    battleMock.demand.fireTorpedo { float course, Quadrant quadrant ->
      assert course == torpedoCourse
    }
    battleMock.demand.getThingDestroyed { Quadrant.Thing.emptySpace }
    Position shipPos = [Coords2d(1,1), Coords2d(1,1)]
    shipMock.demand.with {
      getNumTorpedoes { 3000 }
      getPosition     { shipPos }
    }

    galaxyMock.demand.with {
      getAt(2) { Coords2d shipQuad -> assert shipQuad == shipPos.quadrant; 200 }
      putAt    { Coords2d shpQd, int val -> assert shpQd == shipPos.quadrant && val == 0 }
      getAt(1) { Coords2d shipQuad -> assert shipQuad == shipPos.quadrant; 0 }
      putAt    { Coords2d shpQd, int val -> assert shpQd == shipPos.quadrant && val == 100 }
    }

    MockFor qvMock = MockFor( QuadrantValue )
    qvMock.demand.getEnemy { 2 }

    quadrantSetupMock.demand.updateAfterSkirmish { }

    fleetMock = MockFor( EnemyFleet )
    fleetMock.demand.getNumKlingonBatCrInQuad { 1 }

    battleMock.use {
      shipMock.use {
        trek.ship = new FederationShip()
        galaxyMock.use {
          trek.galaxy = new Galaxy()
          fleetMock.use {
            trek.enemyFleet = new EnemyFleet()
            qvMock.use {
              quadrantSetupMock.use {
                trek.fireTorpedo()
              }
            }
          }
        }
      }
    }

    assert ui.inputValues.empty
    assert ui.localMsgLog.empty
  }

  void testFirePhasersWithTooMuchEnergyOld() {
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

  void testFirePhasersWithGoodEnergyOld() {
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
