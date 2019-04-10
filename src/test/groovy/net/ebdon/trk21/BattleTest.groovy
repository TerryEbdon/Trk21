package net.ebdon.trk21;

import static GameSpace.*;
import static ShipDevice.*;

import groovy.mock.interceptor.StubFor;
import groovy.mock.interceptor.MockFor;
/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
 * @copyright
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
class BattleTest extends GroovyTestCase {
  private Battle battle;
  private EnemyFleet enemyFleet;
  private FederationShip ship;
  private DamageControl dc;
  private TestUi ui;

  private StubFor fleetStub;
  private StubFor dcStub;
  private StubFor shipStub;
  private Expando target;
  private MockFor fleetMock;

  private static int convertToRow( final int shipNo ) {
    1 + (shipNo / 8).toInteger()
  }

  private static int convertToCol( final int shipNo ) {
    1 + shipNo % 8
  }

  @Override void setUp() {
    super.setUp()
    dcStub = new StubFor( DamageControl )
    dcStub.use { dc = new DamageControl() }

    shipStub = new StubFor( FederationShip )
    shipStub.use { ship = new FederationShip() }

    fleetStub = new StubFor( EnemyFleet )
    fleetStub.use { enemyFleet = new EnemyFleet() }
    ui = new TestUi()
    battle = new Battle( enemyFleet, ship, dc, ui )
  }

  private void fleetTarget( final String name ) {
    target = [id: 9, name: name, sector: new Coords2d(1,1)]
  }

  private void hitOnFleetMock( boolean fleetShipExists, final int hitAmount ) {
    fleetMock = new MockFor( EnemyFleet ).tap {
      demand.hitOnShip { int id, int amount ->
        assert id == target.id
        assert amount == hitAmount
      }
      demand.shipExists { int id ->
        assert id == target.id
        fleetShipExists
      }
      if ( fleetShipExists ) {
        demand.energy { id ->
          assert id == target.id
          hitAmount // Assume remaining == hitAmount to simplify the test.
        }
      }
    }
  }

  void testHitOnFleetShipNotDestroyed() {
    fleetTarget 'testHitOnFleetShipNotDestroyed'
    final int hitAmount = 100
    final int targetEnergyRemaining = hitAmount

    hitOnFleetMock true, hitAmount
    runHitOnFleet hitAmount

    assert ui.localMsgLog == ['battle.hitOntargetAt','battle.targetEnergyLeft']
    assert ui.argsLog[0] == [hitAmount, target.name, target.sector.row, target.sector.col]
    assert ui.argsLog[1] == [targetEnergyRemaining]
  }

  @groovy.transform.TypeChecked
  private void runHitOnFleet( final int hitAmount ) {
    fleetMock.use {
      new Battle(
        enemyFleet: new EnemyFleet(),
        ui: ui
      ).hitOnFleetShip( target, hitAmount )
    }
  }

  void testHitOnFleetShipDestroyed() {
    fleetTarget 'testHitOnFleetShipDestroyed'
    final int hitAmount = 200

    hitOnFleetMock false, hitAmount
    runHitOnFleet hitAmount

    assert ui.localMsgLog == ['battle.hitOntargetAt','battle.enemy.destroyed']
    assert ui.argsLog[0] == [hitAmount, target.name, target.sector.row, target.sector.col]
    assert ui.argsLog[1] == []
  }

  void testBattlePhaserAttackFleet() {
    if ( notYetImplemented() ) return
    assert false
  }

  void testGetNextTarget() {
    logger.info 'testGetNextTarget -- BEGIN'
    fleetStub.demand.with {
      getNumKlingonBatCrInQuad(1..18) { 9 }
      getMaxKlingonBCinQuad(1..18)    { 9 }
      getKlingons(9..10) {
        int[][] ships = new int[10][4]
        1.upto(9) { shipNo ->
          final int row = BattleTest.convertToRow( shipNo )
          final int col = BattleTest.convertToCol( shipNo )

          ships[shipNo] = [shipNo,row,col,200]
          logger.trace "ships[$shipNo] = ${ships[shipNo]}"
        }
        ships
      }
    }

    for ( int targetExpected in 1..9 ) {
      final Expando target = battle.getNextTarget()

      assert target.name.contains( "$targetExpected" )
      assert convertToRow( targetExpected ) == target.sector.row
      assert convertToCol( targetExpected ) == target.sector.col
      assert targetExpected == target.id
    }
    logger.info 'testGetNextTarget -- END'
  }

  void testGetNextTargetEmptyFleet() {
    logger.info 'testGetNextTargetEmptyFleet -- BEGIN'
    fleetStub.demand.with {
      getMaxKlingonBCinQuad          { 1 }
      getNumKlingonBatCrInQuad(1..2) { 0 } // 2nd call depends on log level
    }

    final Expando target = battle.getNextTarget()

    assert target == null
    assert ui.empty
    logger.info 'testGetNextTargetEmptyFleet -- END'
  }

  void testGetNextTargetDeadShip() {
    logger.info 'testGetNextTargetDeadShip -- BEGIN'
    fleetStub.demand.with {
      getMaxKlingonBCinQuad          { 1 }
      getNumKlingonBatCrInQuad(2..4) { 1 } // Extra 2 calls depends on log level
      getKlingons(1..2) {
        int[][] ships = new int[2][4]
        ships[1] = [1,1,1,0]  // 1 ship; it's dead.
        ships
      }
    }

    final Expando target = battle.getNextTarget()

    assert target == null
    assert ui.empty
    logger.info 'testGetNextTargetDeadShip -- END'
  }
}
