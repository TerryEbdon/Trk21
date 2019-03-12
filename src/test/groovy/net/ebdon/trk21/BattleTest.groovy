package net.ebdon.trk21;

import groovy.util.logging.Log4j;
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
  Battle battle;
  def enemyFleet;
  def ship;
  def dc;

  StubFor fleetStub;
  StubFor dcStub;
  StubFor shipStub;

  @Override void setUp() {
    dcStub = new StubFor( DamageControl )
    dcStub.use { dc = new DamageControl( new Expando() ) }

    shipStub = new StubFor( FederationShip )
    shipStub.use { ship = new FederationShip() }

    fleetStub = new StubFor( EnemyFleet )
    fleetStub.use { enemyFleet = new EnemyFleet() }

    battle = new Battle( enemyFleet, ship, dc, this.&reporter, this.&attackReporter )
  }

  def reporter() {

  }

  def attackReporter() {

  }

  def testBattleHitOnFleetShip() {
    if ( notYetImplemented() ) return
    assert false
  }

  /// @todo implement testBattleFireTorpedo()
  def testBattleFireTorpedo() {
    if ( notYetImplemented() ) return
    assert false
  }

  def testtBattlePhaserAttackFleet() {
    if ( notYetImplemented() ) return
    assert false
  }

  void testBattleGetNextTarget() {
    logger.info 'testBattleGetNextTarget'
    fleetStub.demand.with {
      getNumKlingonBatCrInQuad(1..18) {9}
      getMaxKlingonBCinQuad(1..18)    {9}
      getKlingons(9..10) {
        def ships = new int[10][4]
        1.upto(9) { shipNo ->
          final int row = BattleTest.convertToRow( shipNo )
          final int col = BattleTest.convertToCol( shipNo )

          ships[shipNo] = [shipNo,row,col,200]
          // println "ships[$shipNo] = ${ships[shipNo]}"
        }
        ships
      }
    }

    1.upto(9) { targetExpected ->
      def target = battle.getNextTarget()
      assertTrue    target.name.contains( "$targetExpected" )
      assertEquals  convertToRow( targetExpected ), target.sector.row
      assertEquals  convertToCol( targetExpected ), target.sector.col
      assertEquals  targetExpected, target.id
    }
    logger.info 'testBattleGetNextTarget -- OK'
  }

  static int convertToRow( shipNo ) {
    1 + (shipNo / 8).toInteger()
  }

  static int convertToCol( shipNo ) {
    1 + shipNo % 8
  }
}
