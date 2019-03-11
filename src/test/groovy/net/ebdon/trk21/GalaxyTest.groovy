package net.ebdon.trk21;
/**
 * @file
 * @author      Terry Ebdon
 * @date        March 2019
 * @copyright (c) Terry Ebdon
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

final class GalaxyTest extends GroovyTestCase {

  Galaxy galaxy;

  @Override void setUp() {
    galaxy = new Galaxy();
  }

  void testOutsideScan() {
    final def badCoords = [-1,0,9]
    for ( row in badCoords ) {
      for ( col in badCoords ) {
        assertEquals "Quadrant outside galaxy should be 000",
            '000', galaxy.scan( new Coords2d( row, col ) )
      }
    }
  }

  void testInsideScan() {
    for (scanValue in 0..999) {
      galaxy[4,4] = scanValue
      assertEquals "$scanValue".padLeft(3,'0'),
          galaxy.scan( new Coords2d( 4,4) )
    }
  }
}
