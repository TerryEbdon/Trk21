package net.ebdon.trk21;

import static GameSpace.*;
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
/// @todo Lots of `1` and `8` instances are hard-coded in this class.
final class QuadrantTest extends GroovyTestCase {
  private Quadrant quadrant;
  final Coords2d enemyPos = [5, 5]

  @Override void setUp() {
    quadrant = new Quadrant()
  }

  void testThingEnum() {
    logger.info 'testThingEnum - Running...'
    assert quadrant[1,1] == null
    quadrant.clear()
    assert quadrant[1,1] == Thing.emptySpace
    assert !quadrant.isOccupied(1,1)
    quadrant[1,1] = Thing.ship
    quadrant[1,2] = Thing.base
    quadrant[4,4] = Thing.star
    quadrant[5,5] = Thing.enemy
    quadrant.dump()
    [[1,1],[1,2],[4,4],[5,5]].each {
      assert quadrant.isOccupied( *it )
    }
    quadrant.dump()
    logger.info '... testThingEnum OK'
  }

  void testSparse() {
    quadrant.clear()
    quadrant[99,99] = Thing.star
    quadrant.board.with {
      remove keySet().first()
    }
    assert !quadrant.valid
  }

  @groovy.transform.TypeChecked
  void testRemoveEnemy() {
    quadrant.clear()
    quadrant[enemyPos.toList()] = Thing.enemy
    quadrant.removeEnemy enemyPos
    assert quadrant[enemyPos.toList()] == Thing.emptySpace
  }

  @groovy.transform.TypeChecked
  void testFindEnemiesNone() {
    quadrant.clear()
    assert quadrant.findEnemies() == []
  }

  @groovy.transform.TypeChecked
  void testFindEnemies() {
    quadrant.clear()
    quadrant[enemyPos] = Thing.enemy
    assert quadrant.findEnemies() == [enemyPos]
  }
}
