package net.ebdon.trk21;
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

final class ShipVectorTest extends GroovyTestCase {
  ShipVector sv;

  @Override void setUp() {
    sv = new ShipVector();
  }

  void testRandomGoodValues() {
    def rnd = new Random()
    for ( float course = 1; course < 9; course += rnd.nextInt( 9 ) / 10  ) {
      for ( float warp = 0.125; warp <= 12; warp += rnd.nextInt( 9 ) / 10  ) {
        sv = new ShipVector( course: course, warpFactor: warp )
        assert sv.valid
      }
    }
  }

  void testStandardWarpFactors() {
    sv.with {
      course = 1
      0.upto(12) { baseWarp ->
        checkGoodBaseWarp baseWarp
        checkGoodWarpSteps()
      }
    }
  }

  private void checkGoodBaseWarp( baseWarp ) {
    sv.warpFactor = baseWarp
    if ( baseWarp ) {
      assert sv.valid
    } else {
      assert !sv.valid
    }
  }

  private void checkGoodWarpSteps() {
    1.upto(7) {
      sv.warpFactor += 1.0 / 8.0
      if (sv.warpFactor < 12) {
        assert sv.valid
      } else {
        assert !sv.valid
      }
    }
  }

  void testBadvalues() {
    assert !sv.valid  // Fail: All zero values
  }
}
