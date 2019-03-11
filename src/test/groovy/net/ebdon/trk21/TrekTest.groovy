package net.ebdon.trk21;

import static Quadrant.*;
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
final class TrekTest extends GroovyTestCase {

  final String errShipNotInSector = 'Ship not in expected sector';

  final String msgPositionEnemy = "Position enemy ships in quadrant %s: %03d";

  Trek trek;

  @Override
  void setUp() {
    logger.info 'setUp'
    trek = new Trek();
    trek.ship = new FederationShip() /// @todo Mock this.
  }

  void testGame() {
    logger.info 'testGame'

    assertFalse "No position so trek should be invalid\n$trek", trek.isValid()
    trek.with {
      ship.position.quadrant.row  = 1
      ship.position.quadrant.col  = 2
      ship.position.sector.row    = 3
      ship.position.sector.col    = 4

      logger.info "Set $ship.position"

      assertEquals "Wrong position: $ship.position", 1, ship.position.quadrant.row
      assertEquals "Wrong position: $ship.position", 2, ship.position.quadrant.col
      assertEquals "Wrong position: $ship.position", 3, ship.position.sector.row
      assertEquals "Wrong position: $ship.position", 4, ship.position.sector.col

      entQuadX = 1
      entQuadY = 2
      entSectX = 3
      entSectY = 4

      logger.info "Set $ship.position"

      assertEquals "Wrong position: $ship.position", 1, ship.position.quadrant.row
      assertEquals "Wrong position: $ship.position", 2, ship.position.quadrant.col
      assertEquals "Wrong position: $ship.position", 3, ship.position.sector.row
      assertEquals "Wrong position: $ship.position", 4, ship.position.sector.col

      entQuadX = entQuadY = entSectX = entSectY = 1
      assertTrue "position should be valid", ship.position.isValid()
      assertTrue "trek should be valid\n$trek", isValid()
    }
    logger.info 'testGame -- OK'
  }

  void testPositionEnterpriseInQuadrantGood() {
    logger.info 'testPositionEnterpriseInQuadrantGood'
    trek.with {
      quadrant.clear()
      ship.position.quadrant = new Coords2d( row: 3, col: 4 )
      logger.info 'testPositionEnterpriseInQuadrantGood set ship quadrant ' + ship.position.quadrant
      assertEquals 'ship has wrong quadrant row', 3, ship.position.quadrant.row
      assertEquals 'ship has wrong quadrant col', 4, ship.position.quadrant.col
      positionShipInQuadrant()

      logger.info "Expecting a ship in " + logFmtCoords( [entSectX,entSectY] )
      assertEquals errShipNotInSector, Thing.ship, quadrant[entSectX,entSectY]
      assertEquals 'Expect 1 ship', 1, quadrant.count { it.value == Thing.ship }
    }
    logger.info 'testPositionEnterpriseInQuadrantGood -- OK'
  }

  void testPositionEnterpriseInQuadrantBad() {
    trek.with {
      shouldFail {  // Trying to position ship in quadrant [0,0]
        logger.info 'testPositionEnterpriseInQuadrantBad' + logFmtCoords( [entSectX,entSectY] )
        positionShipInQuadrant()
        ship.position.quadrant = new Coords2d( row:1, col:1 )
        // logger.info 'testPositionEnterpriseInQuadrant' + logFmtCoords( [entSectX,entSectY] )
        logger.info 'testPositionEnterpriseInQuadrant' + ship.position.quadrant
        positionShipInQuadrant() // fail: empty board.
      }
    }
    logger.info 'testPositionEnterpriseInQuadrantBad -- OK'
  }

  void testPositionGamePieces() {
    logger.info 'testPositionGamePieces...'
    setupAt( *topLeftCoords, *topLeftCoords )
    trek.with {
      enemyFleet.numKlingonBatCrTotal   = 9
      enemyFleet.numKlingonBatCrRemain  = 9
      galaxy[ *topLeftCoords ] = 999

      logger.info sprintf(
        msgPositionEnemy,
        logFmtCoords(entSectX,entSectY), galaxy[entQuadX,entQuadY]
      )
      positionGamePieces()
      galaxy.dump()
      quadrant.dump()

      assertEquals 1, quadrant.count { logger.trace "s.Checking $it : ${it.value == Thing.ship }"; it.value == Thing.ship }
      assertEquals 9, quadrant.count { logger.trace "b.Checking $it : ${it.value == Thing.base }"; it.value == Thing.base }
      assertEquals 9, quadrant.count { logger.trace "e.Checking $it : ${it.value == Thing.enemy}"; it.value == Thing.enemy }
      assertEquals 9, quadrant.count { logger.trace "*.Checking $it : ${it.value == Thing.star }"; it.value == Thing.star }
    }
    logger.info 'testPositionGamePieces -- OK'
  }

  private void setupAt( qRow, qCol, sRow, sCol ) {
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

  private void setupAtCentre() {
    setupAt( 4, 4, 4, 4 )
  }

  /// @deprecated
  private ShipVector shipWarpOne( dir ) {
    trek.ship.energyUsedByLastMove = 8
    new ShipVector().tap {
      course      = dir
      warpFactor  = 1
      isValid()
    }
  }

  void testRepositionTransitGalaxy() {
    logger.info 'testRepositionTransitGalaxy...'
    if ( notYetImplemented() ) return

    [0,1].permutations().eachCombination { courseIncrements ->
      logger.info "Transit with [row,col] offsets of $courseIncrements"
      transit( *courseIncrements )
    }
    logger.info 'testRepositionTransitGalaxy -- OK'
  }

  private def getCourseFrom( expectedRowOffset, expectedColOffset ) {

    final def course = [
      [0,1]: 1,   // "East"
      [1,0]: 7,   // "South"
      [1,1]: 7.5  // "South-East"
    ]

    def rv = course[ expectedRowOffset, expectedColOffset ]
    logger.info "Expected course is $rv"
    rv
  }
}
