package net.ebdon.trk21;

import groovy.mock.interceptor.StubFor;
import groovy.transform.TypeChecked;

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

@groovy.util.logging.Log4j2('logger')
final class TrekLongRangeScanTest extends TrekTestBase {

  private StubFor shipStub;

  @TypeChecked
  @Override void setUp() {
    super.setUp()
    trek = new Trek( ui )
  }

  void testLongRangeScanEmptyGalaxy() {
    logger.info 'testLongRangeScanEmptyGalaxy'

    resetShip 8

    trek.with {
      shouldFail( org.codehaus.groovy.runtime.powerassert.PowerAssertionError ) {
        longRangeScan() // Fail: scanning an empty galaxy
      }
    }
    logger.debug ui
    logger.info 'testLongRangeScanEmptyGalaxy -- OK'
  }

  void testLongRangeScan() {
    logger.info 'testLongRangeScan'

    resetShip 10
    scanGoodBoard()
    logger.debug ui

    assert ui.msgLog.size() == 4
    assert ui.msgLog.first().contains( 'Long range sensor scan for quadrant 1 - 1\n' )

    logger.info 'testLongRangeScan -- OK'
  }

  private void resetShip( final int expectedPositionCalls ) {
    shipStub = new StubFor( FederationShip )
    // int positionCalledTimes = 0
    shipStub.demand.getPosition( expectedPositionCalls ) {
      // positionCalledTimes++
      Coords2d c2d = [row:1, col:1]
      new Position( c2d, c2d )
    }

    shipStub.use {
      trek.ship = new FederationShip()
    }
  }

  @TypeChecked
  private void scanGoodBoard() {
    trek.with {
      galaxy.board = [ [1,1]: 111, [1,2]: 121, [2,1]: 211, [2,2]: 221 ]
      longRangeScan()
    }
  }

  void testScanDamaged() {
    logger.info 'testScanDamaged'
    trek.with {
      damage[3].state = -9 // 9 units of damage
      trek.longRangeScan()
    }
    logger.debug ui
    assert ui.msgLog.size() == 1
    assert ui.msgLog.contains( 'Long range sensors are inoperable.' )

    logger.info 'testScanDamaged -- OK'
  }
}
