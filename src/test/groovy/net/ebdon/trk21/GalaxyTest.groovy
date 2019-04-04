package net.ebdon.trk21;

/**
 * @file
 * @author      Terry Ebdon
 * @date        March 2019
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

 @groovy.transform.TypeChecked
final class GalaxyTest extends GroovyTestCase {

  private Galaxy galaxy;

  @Override void setUp() {
    super.setUp()
    galaxy = new Galaxy();
  }

  void testScanBadGalaxy() {
    shouldFail( org.codehaus.groovy.runtime.powerassert.PowerAssertionError ) {
      galaxy.scan( new Coords2d(4, 4) ) // should fail, empty galaxy
    }
  }

  void testOutsideScan() {
    final List<Integer> badCoords = [-1,0,9]
    for ( int row in badCoords ) {
      for ( int col in badCoords ) {
        assert galaxy.scan( new Coords2d( row, col ) ) == '000'
      }
    }
  }

  void testInsideScan() {
    for (int scanValue in 0..999) {
      galaxy[4,4] = scanValue
      assert galaxy.scan( new Coords2d( 4,4 ) ) == "$scanValue".padLeft(3,'0')
    }
  }

  void testGalaxyvalidafterUpdate() {
    galaxy.with {
      Coords2d coords = new Coords2d(1,1)
      clear()
      assert valid && galaxy[coords] == 0
      galaxy[coords] = 321
      assert valid && galaxy[coords] == 321
      galaxy[coords] -= 100
      assert valid && galaxy[coords] == 221
      galaxy[coords] += 200
      assert valid && galaxy[coords] == 421
    }
  }
}
