package net.ebdon.trk21;

import groovy.test.GroovyTestCase

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

@groovy.transform.TypeChecked
final class ShipVectorTest extends GroovyTestCase {
  private ShipVector sv;

  @Override void setUp() {
    super.setUp()
    sv = new ShipVector();
  }

  @groovy.transform.TypeChecked
  void testRandomGoodValues() {
    Random rnd = new Random()
    for ( float course = 1F; course < 9F; course += rnd.nextInt( 9 ) / 10  ) {
      for ( float warp = 0.125F; warp <= 12F; warp += rnd.nextInt( 9 ) / 10  ) {
        sv = new ShipVector( course: course, warpFactor: warp )
        assert sv.valid
      }
    }
  }

  @SuppressWarnings('JUnitTestMethodWithoutAssert')
  void testStandardWarpFactors() {
    sv.with {
      course = 1F
      for ( int baseWarp in 0..12 ) {
        checkGoodBaseWarp baseWarp
        checkGoodWarpSteps()
      }
    }
  }

  private void checkGoodBaseWarp( final float baseWarp ) {
    sv.warpFactor = baseWarp
    if ( baseWarp ) {
      assert sv.valid
    } else {
      assert !sv.valid
    }
  }

  private void checkGoodWarpSteps() {
    7.times {
      sv.warpFactor += (1.0F / 8.0F).toFloat()
      if (sv.warpFactor < 12F) {
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
