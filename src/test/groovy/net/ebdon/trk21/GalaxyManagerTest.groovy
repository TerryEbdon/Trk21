package net.ebdon.trk21;

import static Quadrant.Thing;
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

@Newify( [MockFor,Coords2d] )
final class GalaxyManagerTest extends GroovyTestCase {
  private MockFor       galaxyMock;
  private MockFor       qvMock;
  private GalaxyManager manager;
  private Coords2d      quadCoords;
  private int           fleetSize;

  @Override void setUp() {
    super.setUp()
    quadCoords = [1,1]
    fleetSize  = 7
    galaxyMock = MockFor( Galaxy )
    qvMock     = MockFor( QuadrantValue )
  }

  void testUpdateEnemy() {
    galaxyMock.demand.with {
      getAt(2) { Coords2d c2d -> assert c2d == quadCoords; 917 }
      putAt(1) { Coords2d c2d, int val -> assert c2d == quadCoords; assert val == 17 }
      getAt(1) { Coords2d c2d -> assert c2d == quadCoords; 17 }
      putAt(1) { Coords2d c2d, int val -> assert c2d == quadCoords; assert val == 717 }
    }
    qvMock.demand.getEnemy { 9 }

    qvMock.use {
      galaxyMock.use {
        manager = new GalaxyManager( quadCoords, new Galaxy() )
        manager.updateNumEnemyShips fleetSize
      }
    }
  }

  void testUpdateNumStars() {
    qvMock.demand.num { Thing thingHit ->
      assert thingHit == Thing.star
      7
    }
    galaxyMock.demand.getAt(2) { Coords2d c2d ->
      assert c2d == quadCoords
      917
    }
    galaxyMock.demand.putAt(1) { Coords2d c2d, int newVal ->
      assert c2d == quadCoords
      assert newVal == 910
    }
    galaxyMock.demand.getAt(1) { Coords2d c2d ->
      assert c2d == quadCoords
      910
    }
    galaxyMock.demand.putAt(1) { Coords2d c2d, int newVal ->
      assert c2d == quadCoords
      assert newVal == 916
    }

    qvMock.use {
      galaxyMock.use {
        manager = new GalaxyManager( quadCoords, new Galaxy() )
        manager.updateNumInQuad Thing.star
      }
    }
  }

  @SuppressWarnings('JUnitTestMethodWithoutAssert')
  void testUpdateNumEnemy() {
    qvMock.use { // No demands, should not be used
      galaxyMock.use { // No demands, should not be used
        manager = new GalaxyManager( quadCoords, new Galaxy() )
        manager.updateNumInQuad Thing.enemy
      }
    }
  }
}
