package net.ebdon.trk21;

import groovy.mock.interceptor.StubFor;
import groovy.transform.TypeChecked;

import static ShipDevice.DeviceType;

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
  private Coords2d c2d = [row:1, col:1]
  private Position shipPos = [c2d, c2d]

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
    scanGoodGalaxy()
    logger.debug ui

    assert ui.localMsgLog == ['sensors.longRange.scanQuadrant']
    assert ui.argsLog == [ [c2d.row, c2d.col] ]
    assert ui.msgLog == ['  000  000  000', '  000  111  121', '  000  211  221']

    logger.info 'testLongRangeScan -- OK'
  }

  private void resetShip( final int expectedPositionCalls ) {
    shipStub = new StubFor( FederationShip )
    shipStub.demand.getPosition( expectedPositionCalls ) { shipPos }

    shipStub.use {
      trek.ship = new FederationShip()
    }
  }

  @TypeChecked
  private void setupGalaxy() {
    trek.galaxy.board = [ [1,1]: 111, [1,2]: 121, [2,1]: 211, [2,2]: 221 ]
  }

  @TypeChecked
  private void scanGoodGalaxy() {
    setupGalaxy()
    trek.longRangeScan()
  }

  void testScanDamaged() {
    logger.info 'testScanDamaged'
    resetShip 10  // Only needed if test fails & scans when it shouldn't.
    setupGalaxy()  // Only needed if test fails & scans when it shouldn't.
    trek.with {
      damageControl.inflictDamage( 3, 9 ) // 9 units of damage to LR sensor.
      assert damageControl.isDamaged( DeviceType.lrSensor )
      trek.longRangeScan()
    }
    logger.debug ui
    assert ui.msgLog.empty
    assert ui.localMsgLog == ['sensors.longRange.offline']

    logger.info 'testScanDamaged -- OK'
  }
}
