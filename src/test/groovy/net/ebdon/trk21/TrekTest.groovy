package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
import static Quadrant.*;
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
final class TrekTest extends GroovyTestCase {

  final String msgPositionEnemy = 'Position enemy ships in quadrant %s: %03d';

  Trek trek;

  @Override
  void setUp() {
    logger.info 'setUp'
    trek = new Trek();
    trek.ship = new FederationShip() /// @todo Mock this.
  }

  void testGame() {
    logger.info 'testGame'

    assert !trek.valid // No position so trek should be invalid
    trek.with {
      ship.position.quadrant.row  = 1
      ship.position.quadrant.col  = 2
      ship.position.sector.row    = 3
      ship.position.sector.col    = 4

      logger.info "Set $ship.position"

      assert ship.position.quadrant.row == 1
      assert ship.position.quadrant.col == 2
      assert ship.position.sector.row   == 3
      assert ship.position.sector.col   == 4

      entQuadX = 1
      entQuadY = 2
      entSectX = 3
      entSectY = 4

      logger.info "Set $ship.position"

      assert ship.position.quadrant.row == 1
      assert ship.position.quadrant.col == 2
      assert ship.position.sector.row   == 3
      assert ship.position.sector.col   == 4

      entQuadX = entQuadY = entSectX = entSectY = 1
      assert ship.position.valid
      assert valid
    }
    logger.info 'testGame -- OK'
  }

  void testPositionEnterpriseInQuadrantGood() {
    logger.info 'testPositionEnterpriseInQuadrantGood'
    trek.with {
      quadrant.clear()
      ship.position.quadrant = new Coords2d( row: 3, col: 4 )
      logger.info 'testPositionEnterpriseInQuadrantGood set ship quadrant ' + ship.position.quadrant
      assert ship.position.quadrant.row == 3
      assert ship.position.quadrant.col == 4
      positionShipInQuadrant()

      logger.info 'Expecting a ship in ' + logFmtCoords( [entSectX,entSectY] )
      assert quadrant[entSectX,entSectY] == Thing.ship
      assert quadrant.count { it.value == Thing.ship } == 1
    }
    logger.info 'testPositionEnterpriseInQuadrantGood -- OK'
  }

  void testPositionEnterpriseInQuadrantBad() {
    trek.with {
      shouldFail {  // Trying to position ship in quadrant [0,0]
        logger.info 'testPositionEnterpriseInQuadrantBad' + logFmtCoords( [entSectX,entSectY] )
        positionShipInQuadrant()
        ship.position.quadrant = new Coords2d( row:1, col:1 )
        logger.info 'testPositionEnterpriseInQuadrant' + ship.position.quadrant
        positionShipInQuadrant() // fail: empty board.
      }
    }
    logger.info 'testPositionEnterpriseInQuadrantBad -- OK'
  }

  @SuppressWarnings('ExplicitCallToGetAtMethod')
  @Newify([MockFor,Position,Coords2d])
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
  @Newify([MockFor,Position,Coords2d])
  void testUpdateQuadrantAfterSkirmish() {
    MockFor ship          = MockFor( FederationShip )
    MockFor galaxy        = MockFor( Galaxy )
    MockFor quadrantSetup = MockFor( QuadrantSetup )
    MockFor enemyFleet    = MockFor( EnemyFleet )

    // Start of demand required for updateNumEnemyShipsInQuad
    ship.demand.getPosition { Position( Coords2d(1,2), Coords2d(3,4) ) }
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

    ship.use {
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

  private void setupAt( final int qRow, final int qCol, final int sRow, final int sCol ) {
    trek.with {
      entSectX = qRow
      entSectY = qCol
      entQuadX = sRow
      entQuadY = sCol

      galaxy.clear()
      quadrant.clear()
      quadrant[entSectX,entSectY] = Thing.ship
    }
  }
}
