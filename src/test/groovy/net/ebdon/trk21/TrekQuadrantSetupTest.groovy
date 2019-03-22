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

  private MockFor ship;
  private MockFor galaxy;

  @Newify(MockFor)
  @Override void setUp() {
    super.setUp()
    ship       = MockFor( FederationShip )
    galaxy     = MockFor( Galaxy )
  }

  @Newify([Coords2d,Position])
  private void shipSetup() {
    ship.demand.getPosition(7) {
      logger.debug 'Mocked ship.getPosition() called'
      Position( Coords2d(currentQuadRow,currentQuadCol), Coords2d(3,4) )
    }
  }

  private void galaxySetup() {
    galaxy.demand.getAt(7) { Coords2d c2d ->
      logger.debug "Mock Galaxy.getAt called with [$c2d.row,$c2d.col]"
      assert c2d.row == currentQuadRow && c2d.col == currentQuadCol
      lrScanValue
    }
  }

  void testPositionGamePieces() {
    logger.debug 'testPositionGamePieces'
    MockFor quadrantSetup = new MockFor( QuadrantSetup )
    quadrantSetup.demand.positionEnemy { int numThings -> }
    quadrantSetup.demand.positionBases { int numThings -> }
    quadrantSetup.demand.positionStars { int numThings -> }

    shipSetup()
    galaxySetup()

    quadrantSetup.use {
      trek = new Trek()
      ship.use {
        trek.ship = new FederationShip()
        galaxy.use {
          trek.galaxy = new Galaxy()
          trek.positionGamePieces()
        }
      }
    }
    logger.debug 'testPositionGamePieces -- OK'
  }
}
