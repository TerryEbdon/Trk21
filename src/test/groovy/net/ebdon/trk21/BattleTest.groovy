package net.ebdon.trk21;

import static GameSpace.*;
import static ShipDevice.*;
import groovy.mock.interceptor.StubFor;
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

  private StubFor fleetStub;
  private StubFor dcStub;
  private StubFor shipStub;

  @Override void setUp() {
    super.setUp()
    dcStub = new StubFor( DamageControl )
    dcStub.use { dc = new DamageControl() }

    shipStub = new StubFor( FederationShip )
    shipStub.use { ship = new FederationShip() }

    fleetStub = new StubFor( EnemyFleet )
    fleetStub.use { enemyFleet = new EnemyFleet() }

    battle = new Battle( enemyFleet, ship, dc, this.&reporter, this.&attackReporter )
  }

  private void reporter() { }
  private void attackReporter() { }

  void testBattleHitOnFleetShip() {
    if ( notYetImplemented() ) return
    assert false
  }

  /// @todo implement testBattleFireTorpedo()
  void testBattleFireTorpedo() {
    if ( notYetImplemented() ) return
    assert false
  }

  void testBattlePhaserAttackFleet() {
    if ( notYetImplemented() ) return
    assert false
  }

  void testBattleGetNextTarget() {
    logger.info 'testBattleGetNextTarget'
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

    1.upto(9) { targetExpected ->
      def target = battle.getNextTarget()

      assert target.name.contains( "$targetExpected" )
      assert convertToRow( targetExpected ) == target.sector.row
      assert convertToCol( targetExpected ) == target.sector.col
      assert targetExpected == target.id
    }
    logger.info 'testBattleGetNextTarget -- OK'
  }

  private static int convertToRow( final int shipNo ) {
    1 + (shipNo / 8).toInteger()
  }

  private static int convertToCol( final int shipNo ) {
    1 + shipNo % 8
  }
}
