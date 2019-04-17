package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
/**
 * @file
 * @author      Terry Ebdon
 * @date        April 2019
 * @copyright   Terry Ebdon, 2019
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
class GalaxySetupTest extends GroovyTestCase {
  private GalaxySetup gs;
  private MockFor     galaxyMock;
  private MockFor     qvMock;
  private MockFor     fleetMock;
  private MockFor     randMock;
  private Galaxy      galaxy;
  private EnemyFleet  fleet;

  private void runAddTest( final String gamePieceName ) {
    logger.debug "$name / $gamePieceName -- BEGIN"
    galaxyMock.use {
      fleetMock.use {
        randMock.use {
          galaxy = new Galaxy()
          fleet = new EnemyFleet()
          gs = new GalaxySetup( galaxy, fleet )
          gs.currentQuadrant = new Coords2d(1, 1)
          gs."add$gamePieceName"()
        }
      }
    }
    logger.debug "$name / $gamePieceName -- END"
  }

  @SuppressWarnings('UnnecessaryPackageReference')
  @Override void setUp() {
    super.setUp()
    galaxyMock = new MockFor( Galaxy )
    fleetMock  = new MockFor( EnemyFleet )
    randMock   = new MockFor( java.util.Random )
    qvMock     = new MockFor( QuadrantValue )
  }

  @SuppressWarnings('JUnitTestMethodWithoutAssert')
  void testAddEnemy() {
    demandsForAddEnemy()
    runAddTest 'Enemy'
  }

  private void demandsForAddEnemy() {
    randMock.demand.nextFloat {
      logger.debug 'nextFloat returning 0F for enemy demands'
      0F
    }
    fleetMock.demand.eachShipNo { Closure closure -> }
    galaxyMock.demand.getBoardSize {
      logger.debug 'in getBoardSize()'
      64
    }
  }

  private void demandsForAddBases() {
    randMock.demand.nextFloat {
      logger.debug 'nextFloat returning 0.99F for base demands'
      0.99F
    }
  }

  private void demandsForAddStars() {
    randMock.demand.nextInt {
      logger.debug 'nextInt returning 8 for star demands'
      8
    }
  }

  void testAddBases() {
    demandsForAddBases()
    runAddTest 'Bases'
    assert gs.basesInQuad == 1
  }

  private void demandsForAddGamePieces() {
    final Coords2d thisQuadCoords = [1, 1]
    galaxyMock.demand.eachCoords2d { Closure closure ->
      logger.debug 'in eachC2d'
      closure thisQuadCoords.clone()
    }
    demandsForAddEnemy()
    demandsForAddBases()
    demandsForAddStars()
    qvMock.demand.getValue { 919 }
    galaxyMock.demand.putAt { Coords2d atC2d, int newVal ->
      assert atC2d  == thisQuadCoords
      assert newVal == 919
    }
  }

  void testAddStars() {
    demandsForAddStars()
    runAddTest 'Stars'
    assert gs.starsInQuad == 9
  }

  @SuppressWarnings('UnnecessarySetter')
  @SuppressWarnings('UnnecessaryGetter')
  void testAddShip() {
    logger.debug "$name -- BEGIN"
    fleetMock.demand.getNumKlingonBatCrTotal   { 0 }
    fleetMock.demand.setNumKlingonBatCrTotal   { int i -> assert i == 1 }
    fleetMock.demand.getNumKlingonBatCrRemain  { 0 }
    fleetMock.demand.setNumKlingonBatCrRemain  { int i -> assert i == 1 }

    galaxyMock.use {
      fleetMock.use {
        randMock.use {
          galaxy = new Galaxy()
          fleet = new EnemyFleet()
          gs = new GalaxySetup( galaxy, fleet )
          gs.currentQuadrant = new Coords2d(1, 1)
          gs.addShip 1, 99F
          assert gs.enemyInQuad == 0
          gs.addShip 1, 0
          assert gs.enemyInQuad == 1
        }
      }
    }
    logger.debug "$name -- END"
  }

  void testAddGamePieces() {
    logger.debug "$name -- BEGIN"
    demandsForAddGamePieces()

    galaxyMock.use {
      fleetMock.use {
        randMock.use {
          qvMock.use {
            galaxy = new Galaxy()
            fleet = new EnemyFleet()
            gs = new GalaxySetup( galaxy, fleet )
            gs.addGamePieces()
          }
        }
      }
    }

    gs.with {
      assert enemyInQuad == 0
      assert basesInQuad == 1
      assert starsInQuad == 9
    }
    logger.debug "$name -- END"
  }
}
