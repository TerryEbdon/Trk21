package net.ebdon.trk21;

import groovy.util.logging.Log4j
import static GameSpace.*
import static ShipDevice.*
import groovy.mock.interceptor.StubFor

@groovy.util.logging.Log4j2('logger')
class BattleTest extends GroovyTestCase {
  Battle battle;
  def enemyFleet;
  def ship;
  def dc;

  StubFor fleetStub
  StubFor dcStub
  StubFor shipStub

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

  void testBattle() {
    fleetStub.demand.with {
      getNumKlingonBatCrInQuad(1..18) {9}
      getMaxKlingonBCinQuad(1..18)    {9}
      getKlingons(9..10) {
        def ships = new int[10][4]
        1.upto(9) { shipNo ->
          final int row = BattleTest.convertToRow( shipNo )
          final int col = BattleTest.convertToCol( shipNo )

          ships[shipNo] = [0,row,col,200]
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
    }
  }

  static int convertToRow( shipNo ) {
    1 + (shipNo / 8).toInteger()
  }

  static int convertToCol( shipNo ) {
    1 + shipNo % 8
  }
}
