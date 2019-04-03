package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError;

/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
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
@Newify(MockFor)
final class TrekTest extends GroovyTestCase {

  private final String msgPositionEnemy = 'Position enemy ships in quadrant %s: %03d';

  private Trek trek;

  @Override
  void setUp() {
    super.setUp()
    logger.info 'setUp'
    Trek.config = true
    trek = new Trek()
  }

  void testPositionEnterpriseInQuadrantGood() {
    logger.info 'testPositionEnterpriseInQuadrantGood'
    MockFor quadMock = MockFor( Quadrant )
    MockFor shipMock = MockFor( FederationShip )

    Coords2d targetQuad = [1,2]
    Coords2d targetSect = [3,4]
    Position targetPos = [targetQuad.clone(), targetSect.clone()]

    shipMock.demand.with {
      getPosition(2) { targetPos }
    }

    quadMock.demand.with {
      getRandomCoords { targetSect.toList() }
      putAt { Coords2d sector, Quadrant.Thing newThing ->
        assert sector == targetSect
        assert newThing == Quadrant.Thing.ship
      }
      getValid { true }
      dump {}
    }

    trek.with {
      quadMock.use {
        quadrant = new Quadrant()
        shipMock.use {
          ship = new FederationShip()
          // quadrant.clear()
          // ship.position.quadrant = targetQuad.clone()
          positionShipInQuadrant()

          logger.trace "Expecting a ship in $targetSect"
          // assert quadrant[ship.position.sector] == Quadrant.Thing.ship
          // assert quadrant.count { it.value == Quadrant.Thing.ship } == 1
        }
      }
    }
    logger.info 'testPositionEnterpriseInQuadrantGood -- OK'
  }

  void testPositionFedShipOutsideGalaxy() {
    logger.info 'testPositionFedShipOutsideGalaxy'
    MockFor shipMock = MockFor( FederationShip )
    MockFor quadMock = MockFor( Quadrant )

    Coords2d invalidCoords  = [0,0]
    Position invalidPos     = [invalidCoords.clone(), invalidCoords.clone()]

    shipMock.demand.with {
      getPosition { invalidPos }
    }

    quadMock.use { // No demands as shouldn't be accessed. Fail if accessed.
      shipMock.use {
        trek.ship = new FederationShip()
        shouldFail(PowerAssertionError) {
          trek.positionShipInQuadrant() // Fail: position outside galaxy.
        }
      }
    }
    logger.info 'testPositionFedShipOutsideGalaxy -- OK'
  }

  @SuppressWarnings('ExplicitCallToGetAtMethod')
  @Newify([Position,Coords2d])
  void testPositionGamePieces() {
    logger.info 'testPositionGamePieces...'

    MockFor ship          = MockFor( FederationShip )
    MockFor galaxy        = MockFor( Galaxy )
    MockFor quadrantSetup = MockFor( QuadrantSetup )

    ship.demand.getPosition { Position( Coords2d(1,2), Coords2d(3,4) ) }
    galaxy.demand.getAt { Coords2d c2d ->
      assert c2d == Coords2d(1,2)
      919
    }

    quadrantSetup.demand.with {
      positionEnemy { int numEnemy -> assert numEnemy == 9 }
      positionBases { int numBases -> assert numBases == 1 }
      positionStars { int numStars -> assert numStars == 9 }
    }

    ship.use {
      trek.ship = new FederationShip()
      galaxy.use {
        trek.galaxy = new Galaxy()
        quadrantSetup.use {
          trek.positionGamePieces()
        }
      }
    }

    logger.info 'testPositionGamePieces -- OK'
  }

  @SuppressWarnings('ExplicitCallToGetAtMethod')
  @Newify([Position,Coords2d])
  void testUpdateQuadrantAfterSkirmish() {
    MockFor shipMock      = MockFor( FederationShip )
    MockFor galaxy        = MockFor( Galaxy )
    MockFor quadrantSetup = MockFor( QuadrantSetup )
    MockFor enemyFleet    = MockFor( EnemyFleet )

    // Start of demand required for updateNumEnemyShipsInQuad
    shipMock.demand.getPosition { Position( Coords2d(1,2), Coords2d(3,4) ) }
    enemyFleet.demand.getNumKlingonBatCrInQuad { 3 }
    galaxy.demand.with {
      getAt { Coords2d c2d ->
        assert c2d == Coords2d(1,2)
        919
      }

      getAt { Coords2d c2d -> assert c2d == Coords2d(1,2); 919 } // enemy @ start
      putAt { Coords2d c2d, int newVal -> assert newVal ==  19 } // remove them
      getAt { Coords2d c2d -> assert c2d == Coords2d(1,2);  19 }
      putAt { Coords2d c2d, int newVal -> assert newVal == 319 } // sync with fleet
    }
    // end of demand required for updateNumEnemyShipsInQuad

    quadrantSetup.demand.updateAfterSkirmish { }

    shipMock.use {
      trek.ship = new FederationShip()
      galaxy.use {
        trek.galaxy = new Galaxy()
        enemyFleet.use {
          trek.enemyFleet = new EnemyFleet()
          quadrantSetup.use {
            trek.updateQuadrantAfterSkirmish()
          }
        }
      }
    }
  }

  void testNavComp() {
    MockFor navComp = MockFor( NavComp )
    navComp.demand.run { }

    navComp.use {
      trek.navComp()
    }
  }
}
