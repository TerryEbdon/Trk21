package net.ebdon.trk21;
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

final class ShipVectorTest extends GroovyTestCase {
  ShipVector sv = new ShipVector();

  void testGoodValues() {
    def rnd = new Random()
    for ( float course = 1; course < 8; course += rnd.nextInt( 9 ) / 10  ) {
      1.upto(12) { warp ->
        sv = new ShipVector( course: course, warpFactor: warp )
        assertEquals(
          "$sv",
          true,
          sv.isValid()
        )
      }
    }
  }

  void testBadvalues() {
    sv.with {
      assertFalse "$sv", isValid()  // Fail: All zero values
      course = 1
      [ 0.25,   // WF 0.25 is used to move 2 sectors.
        0.5,    // WF 0.25 is used to move 4 sectors.
        1       // WF 0.25 is used to move 1 quadrant (8 sectors).
      ].each { wf ->
        warpFactor = wf
        assertTrue "$sv", isValid()
      }
    }
  }
}
