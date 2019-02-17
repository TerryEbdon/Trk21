package net.ebdon.trk21;

import static GameSpace.*;
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
/// @note the \@grab is required to work-around bug
/// [GROOVY-5574](https://issues.apache.org/jira/browse/GROOVY-5574)
/// Yes, I kow it claims to have been fixed in groovy 2.0.1 but I'm
/// seeing it in Groovy version 2.5.5
/// todo Lots of `1` and `8` instances are hard-coded in this class.
final class QuadrantTest extends GroovyTestCase {

  void testThingEnum() {
    logger.info 'testThingEnum - Running...'
    Quadrant gs = new Quadrant()
    assertEquals null, gs[1,1]
    gs.clear()
    // assertEquals 0, gs[1,1]
    assertEquals Thing.emptySpace, gs[1,1]
    assertFalse "Sector [1,1] should be unoccupied", gs.isOccupied(1,1)
    gs[1,1] = Thing.ship
    gs[1,2] = Thing.base
    gs[4,4] = Thing.star
    gs[5,5] = Thing.enemy
    gs.dump()
    [[1,1],[1,2],[4,4],[5,5]].each {
      assertTrue "Sector $it should be occupied", gs.isOccupied( *it )
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
    assertFalse "Sparse board should not be valid", quad.isValid()
  }
}
