package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
import groovy.transform.TypeChecked;
/**
 * @file
 * @author      Terry Ebdon
 * @date        March 2019
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
final class TrekShortRangeScanTest extends TrekTestBase {

  private MockFor shipMock; /// @todo Duplicated in TrekLongRangeScanTest

  @TypeChecked
  @Override void setUp() {
    super.setUp()
    trek = new Trek( ui )
  }

  /// @todo Duplicated in TrekLongRangeScanTest
  private void resetShip( final int expectedPositionCalls ) {
    shipMock = new MockFor( FederationShip )

    shipMock.demand.with {
      shortRangeScan { galaxy -> }
      attemptDocking { quadrant -> }
      getCondition(4) { 'flummoxed' }

      getPosition( expectedPositionCalls ) {
        Coords2d c2d = [row:1, col:1]
        new Position( c2d, c2d )
      }
      getEnergyNow { 12345 }
      getNumTorpedoes { 99 }
    }
  }

  @TypeChecked
  void testShortRangeScan() {
    resetShip 4
    trek.quadrant.clear()
    shipMock.use {
      trek.ship = new FederationShip()
      trek.shortRangeScan()
    }
    checkScanOutput()
  }

  @TypeChecked
  private void checkScanOutput() {
    final String dividerLine = '---------------'

    ui.with {
      assert msgLog.size() == 14
      assert msgLog.first().contains( dividerLine )
      assert msgLog[9].contains( dividerLine )
      assert msgLog[10].contains( 'STARDATE' )
      assert msgLog[10].contains( 'CONDITION: flummoxed' )
      assert msgLog[11].contains( 'QUADRANT:' )
      assert msgLog[11].contains( 'SECTOR: 1 - 1' )
      assert msgLog[12].contains( 'ENERGY: 12345' )
      assert msgLog[12].contains( 'PHOTON TORPEDOS: 99' )
      assert msgLog[13].contains( 'KLINGONS:     0' )
    }
  }
}
