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
final class QuadrantSetupTest extends GroovyTestCase {

  private QuadrantSetup quadrantSetup;
  private final int numEnemy    = 2;
  private final int numBases    = 1;
  private final int numStars    = 3;

  private final int rowToPositionIn = 4;

  private MockFor quadrant;
  private MockFor enemyFleet;

  @Newify(MockFor)
  @Override void setUp() {
    super.setUp()
    quadrant   = MockFor( Quadrant )
    enemyFleet = MockFor( EnemyFleet )
  }

  private void setupQuadrantMock( int numThings, Quadrant.Thing thingTypeToDeploy ) {
    quadrant.demand.getValid { true }
    1.upto(numThings) { thingNo ->
      quadrant.demand.getEmptySector {
        logger.debug "Getting empty sector for star $thingNo"
        [rowToPositionIn, thingNo]
      }
      quadrant.demand.putAt { List<Integer> coords, Quadrant.Thing thing ->
        logger.debug "putAt[$coords] called with star $thingNo"
        assert coords == [rowToPositionIn,thingNo]
        assert thing == thingTypeToDeploy
      }
    }
  }

  @SuppressWarnings('UnnecessarySetter')
  private void enemySetup() {
    enemyFleet.with {
      enemyFleet.demand.getValid { true }
      enemyFleet.demand.setNumKlingonBatCrInQuad { int newNumEnemy ->
        assert newNumEnemy == numEnemy
      }
      enemyFleet.demand.resetQuadrant { }
      for ( int enemyNum in 1..numEnemy ) {
        demand.positionInSector { int enemyShipNo, List<Integer> enemyPos ->
          logger.debug 'PositionInSector called with enemy ship {}, position {}',
              enemyShipNo, enemyPos
        }
      }
    }
  }

  @SuppressWarnings(['NoDef','MethodParameterTypeRequired'])
  private void runTest( Closure methodToTest, arg ) {
    quadrant.use {
      quadrantSetup.quadrant = new Quadrant()
      methodToTest arg
    }
  }

  void testPositionStars() {
    logger.info 'testPositionStars'
    setupQuadrantMock numStars, Quadrant.Thing.star
    quadrantSetup = new QuadrantSetup()
    runTest quadrantSetup.&positionStars, numStars
    logger.info 'testPositionStars -- OK'
  }

  void testPositionBases() {
    logger.debug 'testPositionBases'
    setupQuadrantMock numBases, Quadrant.Thing.base
    quadrantSetup = new QuadrantSetup()
    runTest quadrantSetup.&positionBases, numBases
    logger.debug 'testPositionBases -- OK'
  }

  void testPositionEnemy() {
    logger.debug 'testPositionEnemy'
    setupQuadrantMock numEnemy, Quadrant.Thing.enemy
    enemySetup()
    enemyFleet.use {
      quadrantSetup = new QuadrantSetup()
      quadrantSetup.enemyFleet = new EnemyFleet()
      runTest quadrantSetup.&positionEnemy, numEnemy
    }
    logger.debug 'testPositionEnemy -- OK'
  }

  void testUpdateAfterSkirmishNoEnemies() {
    enemyFleet.demand.getValid { true }
    quadrantSetup = new QuadrantSetup()
    quadrant.demand.findEnemies {
      [:] // Weapons fired when enemy not present.
    }
    afterSkirmish()
  }

  void testUpdateAfterSkirmishWithEnemies() {
    log.info 'testUpdateAfterSkirmishWithEnemies -- Begin'
    enemyFleet.demand.getValid { true }
    enemyFleet.demand.isShipAt(numEnemy) { List<Integer> coords ->
      logger.debug 'Asked if an enemy ship is at {}', coords
      assert coords.size() == 2
      assert (1..8).containsAll( coords )
      false
    }
    quadrant.demand.findEnemies {
      Map<List<Integer>,Quadrant.Thing> map = [:]
      map[rowToPositionIn,1] = Quadrant.Thing.enemy
      map[rowToPositionIn,2] = Quadrant.Thing.enemy
      map
    }
    quadrant.demand.removeEnemy(numEnemy) { sectorCoords ->
      logger.debug sectorCoords
    }
    quadrantSetup = new QuadrantSetup()
    afterSkirmish()
    log.info 'testUpdateAfterSkirmishWithEnemies -- End'
  }

  private void afterSkirmish() {
    enemyFleet.use {
      quadrantSetup.enemyFleet = new EnemyFleet()
      quadrant.use {
        quadrantSetup.quadrant = new Quadrant()
        quadrantSetup.updateAfterSkirmish()
      }
    }
  }
}
