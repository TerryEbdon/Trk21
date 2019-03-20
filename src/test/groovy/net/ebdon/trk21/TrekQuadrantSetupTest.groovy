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

@groovy.util.logging.Log4j2('logger')
final class QuadrantSetupTest extends GroovyTestCase {

  private Trek trek;
  private final int numEnemy    = 1;
  private final int numBases    = 2;
  private final int numStars    = 3;
  private final int lrScanValue = numEnemy * 100 + numBases * 10 + numStars;
  private final int starRow     = 4;

  private final int currentQuadRow = 1;
  private final int currentQuadCol = 2;

  private MockFor ship;
  private MockFor galaxy;
  private MockFor quadrant;

  @Newify([MockFor,Coords2d,Position])
  @Override void setUp() {
    super.setUp()
    ship     = MockFor( FederationShip )
    galaxy   = MockFor( Galaxy )
    quadrant = MockFor( Quadrant )

    ship.demand.getPosition(8) {
      logger.debug 'Mocked ship.getPosition() called'
      Position( Coords2d(currentQuadRow,currentQuadCol), Coords2d(3,4) )
    }

    galaxy.demand.getAt(4) { int row, int col ->
      logger.debug "Mock Galaxy.getAt called with [$row,$col]"
      assert row == currentQuadRow && col == currentQuadCol
      lrScanValue
    }
  }

  void testPositionStars() {
    logger.info 'testPositionStars'

    1.upto(numStars) { starNum ->
      quadrant.demand.getEmptySector {
        logger.debug "Getting empty sector for star $starNum"
        [starRow, starNum]
      }
      quadrant.demand.putAt { List<Integer> coords, Quadrant.Thing thing ->
        logger.debug "putAt[$coords] called with star $starNum"
        assert coords == [starRow,starNum]
        assert thing == Quadrant.Thing.star
      }
    }

    galaxy.use {
      trek = new Trek()
      ship.use {
        trek.ship = new FederationShip()
        quadrant.use {
          trek.quadrant = new Quadrant()
          trek.positionStars()
        }
      }
    }

    logger.info 'testPositionStars -- OK'
  }

  @SuppressWarnings(['IfStatementBraces','ConstantAssertExpression'])
  void testPositionBases() {
    logger.debug 'testPositionBases'
    if ( notYetImplemented() ) return
    assert false
    logger.debug 'testPositionBases -- OK'
  }

  @SuppressWarnings(['IfStatementBraces','ConstantAssertExpression'])
  void testPositionEnemy() {
    logger.debug 'testPositionEnemy'
    if ( notYetImplemented() ) return
    assert false
    logger.debug 'testPositionEnemy -- OK'
  }
}
