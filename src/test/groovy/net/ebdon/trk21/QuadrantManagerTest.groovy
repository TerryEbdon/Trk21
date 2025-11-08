package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
import groovy.test.GroovyTestCase
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

@Newify( [MockFor,Coords2d] )
final class QuadrantManagerTest extends GroovyTestCase {

  private MockFor   quadMock;

  @Override void setUp() {
    super.setUp()
    quadMock = MockFor( Quadrant )
  }

  void testPositionGamePieces() {
    MockFor fleetMock = MockFor( EnemyFleet ) // pass-through, no demands.
    MockFor qsMock = MockFor( QuadrantSetup )
    MockFor qvMock = MockFor( QuadrantValue )

    final int enemyCount = 9
    final int baseCount  = 1
    final int starCount  = 8

    qsMock.demand.positionEnemy { int num -> assert num == enemyCount }
    qsMock.demand.positionBases { int num -> assert num == baseCount }
    qsMock.demand.positionStars { int num -> assert num == starCount }

    qvMock.demand.with {
      calcNumEnemy { enemyCount }
      calcNumBases { baseCount }
      calcNumStars { starCount }
      getEnemy { enemyCount }
      getBases { baseCount }
      getStars { starCount }
    }

    qvMock.use {
      qsMock.use {
        quadMock.use {
          fleetMock.use {
            new QuadrantManager( new Quadrant() ).
              positionGamePieces 918, new EnemyFleet()
          }
        }
      }
    }
  }

  void testPositionShip() {
    final Coords2d shipCoords = [4, 4]
    quadMock.demand.with {
      clear { }
      getRandomCoords { shipCoords.clone() }
      putAt { Coords2d sector, Quadrant.Thing thing ->
        assert sector == shipCoords
        assert thing == Quadrant.Thing.ship
      }
    }
    Coords2d shipSector

    quadMock.use {
      shipSector = new QuadrantManager( new Quadrant() ).positionShip()
    }

    assert shipCoords == shipSector
  }
}
