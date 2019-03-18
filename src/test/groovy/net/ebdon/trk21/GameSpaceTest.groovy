package net.ebdon.trk21;

import static GameSpace.*;
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
/// todo Lots of `1` and `8` instances are hard-coded in this class.
final class GameSpaceTest extends GroovyTestCase {

  void testSectorIsInsideQuadrantGood() {
    logger.info 'testSectorIsInsideQuadrantGood'

    minCoord.upto(maxCoord) {
      minCoord.upto(maxCoord) { jt ->
        assert sectorIsInsideQuadrant( [ it, jt ] ) // Good coords
      }
    }
  }

  void testSectorIsInsideQuadrantBad() {
    logger.info 'testSectorIsInsideQuadrantBad'

    minCoord.upto(maxCoord) {
      minCoord.upto(maxCoord) { jt ->
        assert !sectorIsInsideQuadrant( [-it,-jt ] ) // fail
        assert !sectorIsInsideQuadrant( [-it, jt ] ) // fail
        assert !sectorIsInsideQuadrant( [ it,-jt ] ) // fail
      }

      [-it,0].permutations {
        assert !sectorIsInsideQuadrant( it )
      }
    }

    [-9,0].permutations().eachCombination {
      assert !sectorIsInsideQuadrant( it )
    }
  }

  void testDistance() {
    Coords2d posFrom  = new Coords2d( row: 1, col: 2 )
    Coords2d posTo    = new Coords2d( row: 8, col: 8 )
    logger.info "Calc distance from $posFrom to $posTo"
    final float distance = distanceBetween( posFrom, posTo )
    logger.info sprintf('Distance is: %+1.6f', distance )
  }
}
