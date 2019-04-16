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
  private final int lrScanValue = 123;
  private final int currentQuadRow = 1;
  private final int currentQuadCol = 2;

  private MockFor shipMock;
  private MockFor galaxyMock;

  @Newify(MockFor)
  @Override void setUp() {
    super.setUp()
    shipMock   = MockFor( FederationShip )
    galaxyMock = MockFor( Galaxy )
  }

  @Newify([Coords2d,Position])
  private void shipSetup() {
    ship.demand.getPosition(1) {
      logger.debug 'Mocked ship.getPosition() called'
      Position( Coords2d(currentQuadRow,currentQuadCol), Coords2d(3,4) )
    }
  }

  private void galaxySetup() {
    galaxy.demand.getAt(1) { Coords2d c2d ->
      logger.debug "Mock Galaxy.getAt called with [$c2d.row,$c2d.col]"
      assert c2d.row == currentQuadRow && c2d.col == currentQuadCol
      lrScanValue
    }
  }

  @Newify([MockFor,Coords2d,Position])
  void testSetupQuadrant() {
    MockFor quadMock   = MockFor( Quadrant )
    MockFor qmMock     = MockFor( QuadrantManager ) // No demands.
    MockFor fleetMock  = MockFor( EnemyFleet ) // No demands.

    final Coords2d shipQuad = [currentQuadRow, currentQuadCol]
    qmMock.demand.positionShip { Coords2d(3, 4) }
    qmMock.demand.positionGamePieces { int val, EnemyFleet fleet -> }

    galaxyMock.demand.getAt { Coords2d quadCoords ->
      assert quadCoords == shipQuad
      123
    }

    shipMock.demand.getPosition(2) {
      Position( shipQuad, Coords2d(1, 1) )
    }

    fleetMock.use {
      quadMock.use {
        galaxyMock.use {
          shipMock.use {
            qmMock.use {
              trek = new Trek()
              trek.enemyFleet = new EnemyFleet()
              trek.galaxy     = new Galaxy()
              trek.ship       = new FederationShip()
              trek.setupQuadrant()
            }
          }
        }
      }
    }
  }
}
