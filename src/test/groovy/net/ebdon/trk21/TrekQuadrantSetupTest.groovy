package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
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

/// JUnitTestMethodWithoutAssert is suppressed
/// as the asserts are in mocked methods.
@SuppressWarnings('JUnitTestMethodWithoutAssert')
@groovy.util.logging.Log4j2('logger')
final class TrekQuadrantSetupTest extends GroovyTestCase {

  private Trek trek;
  private final int numEnemy    = 1;
  private final int numBases    = 2;
  private final int numStars    = 3;
  private final int lrScanValue = numEnemy * 100 + numBases * 10 + numStars;

  private final int starRow        = 4;
  private final int currentQuadRow = 1;
  private final int currentQuadCol = 2;

  private MockFor ship;
  private MockFor galaxy;
  private MockFor quadrant;
  private MockFor enemyFleet;

  @Newify(MockFor)
  @Override void setUp() {
    super.setUp()
    ship       = MockFor( FederationShip )
    galaxy     = MockFor( Galaxy )
    quadrant   = MockFor( Quadrant )
    enemyFleet = MockFor( EnemyFleet )
  }

  @Newify([Coords2d,Position])
  private void shipSetup() {
    ship.demand.getPosition(2) {
      logger.debug 'Mocked ship.getPosition() called'
      Position( Coords2d(currentQuadRow,currentQuadCol), Coords2d(3,4) )
    }
  }

  private void galaxySetup() {
    galaxy.demand.getAt(4) { int row, int col ->
      logger.debug "Mock Galaxy.getAt called with [$row,$col]"
      assert row == currentQuadRow && col == currentQuadCol
      lrScanValue
    }
  }

  private void quadrantSetup( int numThings, Quadrant.Thing thingTypeToDeploy ) {
    quadrant.demand.getValid { true }
    1.upto(numThings) { thingNo ->
      quadrant.demand.getEmptySector {
        logger.debug "Getting empty sector for star $thingNo"
        [starRow, thingNo]
      }
      quadrant.demand.putAt { List<Integer> coords, Quadrant.Thing thing ->
        logger.debug "putAt[$coords] called with star $thingNo"
        assert coords == [starRow,thingNo]
        assert thing == thingTypeToDeploy
      }
    }
  }

  private void enemySetup() {
    enemyFleet.with {
      enemyFleet.demand.setNumKlingonBatCrInQuad { int newNumEnemy ->
        assert newNumEnemy == numEnemy
      }
      enemyFleet.demand.resetQuadrant { }
      for ( int enemyNum in 1..numEnemy ) {
        demand.positionInSector { int enemyShipNo, List<Integer> enemyPos ->
          assert enemyShipNo == enemyNum
        }
      }
    }
  }

  private void runTest( Closure methodTotest, arg ) {
    ship.use {
      trek.ship = new FederationShip()
      quadrant.use {
        trek.quadrant = new Quadrant()
        methodTotest arg
      }
    }
  }

  void testPositionStars() {
    logger.info 'testPositionStars'
    quadrantSetup numStars, Quadrant.Thing.star
    trek = new Trek()
    runTest trek.&positionStars, numStars
    logger.info 'testPositionStars -- OK'
  }

  void testPositionBases() {
    logger.debug 'testPositionBases'
    quadrantSetup numBases, Quadrant.Thing.base
    trek = new Trek()
    runTest trek.&positionBases, numBases
    logger.debug 'testPositionBases -- OK'
  }

  void testPositionEnemy() {
    logger.debug 'testPositionEnemy'
    quadrantSetup numEnemy, Quadrant.Thing.enemy
    enemySetup()
    enemyFleet.use {
      trek = new Trek()
      runTest trek.&positionEnemy, numEnemy
    }
    logger.debug 'testPositionEnemy -- OK'
  }
}
