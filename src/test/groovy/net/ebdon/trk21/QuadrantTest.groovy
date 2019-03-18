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

  void testThingEnum() {
    logger.info 'testThingEnum - Running...'
    Quadrant gs = new Quadrant()
    assert gs[1,1] == null
    gs.clear()
    assert gs[1,1] == Thing.emptySpace
    assert !gs.isOccupied(1,1)
    gs[1,1] = Thing.ship
    gs[1,2] = Thing.base
    gs[4,4] = Thing.star
    gs[5,5] = Thing.enemy
    gs.dump()
    [[1,1],[1,2],[4,4],[5,5]].each {
      assert gs.isOccupied( *it )
    }
    gs.dump()
    logger.info '... testThingEnum OK'
  }

  void testSparse() {
    Quadrant quad = new Quadrant()
    quad.clear()
    quad[99,99] = Thing.star
    quad.board.with {
        remove keySet().first()
    }
    assert !quad.valid
  }
}
