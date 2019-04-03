package net.ebdon.trk21;

import static ShipDevice.DeviceType;

import groovy.mock.interceptor.StubFor;
import groovy.mock.interceptor.MockFor;
import groovy.transform.TypeChecked;

import org.codehaus.groovy.runtime.powerassert.PowerAssertionError;

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
@Newify( MockFor )
final class TrekLongRangeScanTest extends TrekTestBase {

  private StubFor shipStub;
  private Coords2d c2d = [row:1, col:1]
  private Position shipPos = [c2d, c2d]

  @TypeChecked
  @Override void setUp() {
    super.setUp()
    trek = new Trek( ui )
  }

  // void testLongRangeScanEmptyGalaxy() {
  //   logger.info 'testLongRangeScanEmptyGalaxy'
  //
  //   resetShip 1
  //   MockFor galaxyMock = MockFor( Galaxy )        // Pass-through, no demands
  //   MockFor lrScanMock = MockFor( LongRangeScan )
  //
  //   lrScanMock.demand.scanAround { Coords2d quadPos ->
  //     assert quadPos == shipPos.quadrant
  //   }
  //
  //   galaxyMock.use {
  //     lrScanMock.use {
  //       shouldFail( PowerAssertionError ) {
  //         trek.longRangeScan() // Fail: scanning an empty galaxy
  //       }
  //     }
  //   }
  //
  //   assert ui.empty
  //   shipStub.verify()
  //   logger.info 'testLongRangeScanEmptyGalaxy -- OK'
  // }

  void testLongRangeScan() {
    logger.info 'testLongRangeScan'

    resetShip 10
    scanGoodGalaxy()
    assert ui.empty

    // logger.debug ui
    // assert ui.localMsgLog == ['sensors.longRange.scanQuadrant']
    // assert ui.argsLog == [ [c2d.row, c2d.col] ]
    // assert ui.msgLog == ['  000  000  000', '  000  111  121', '  000  211  221']

    logger.info 'testLongRangeScan -- OK'
  }

  private void resetShip( final int expectedPositionCalls ) {
    MockFor galaxyMock = MockFor( Galaxy )        // Pass-through, no demands
    shipStub = new StubFor( FederationShip )
    shipStub.demand.getPosition( expectedPositionCalls ) { shipPos }

    galaxyMock.use {
      trek.galaxy = new Galaxy()
      shipStub.use {
        trek.ship = new FederationShip()
      }
    }
  }

  @TypeChecked
  private void setupGalaxy() {
    trek.galaxy.board = [ [1,1]: 111, [1,2]: 121, [2,1]: 211, [2,2]: 221 ]
  }

  private void scanGoodGalaxy() {
    // setupGalaxy()
    MockFor galaxyMock = MockFor( Galaxy )        // Pass-through, no demands
    MockFor lrScanMock = MockFor( LongRangeScan )
    lrScanMock.demand.scanAround { Coords2d quadPos ->
      assert quadPos == shipPos.quadrant
    }
    resetShip 1

    galaxyMock.use {
      trek.galaxy = new Galaxy()
      lrScanMock.use {
        trek.longRangeScan()
      }
    }
  }

  // void testScanDamaged() {
  //   logger.info 'testScanDamaged'
  //   resetShip 10  // Only needed if test fails & scans when it shouldn't.
  //   setupGalaxy()  // Only needed if test fails & scans when it shouldn't.
  //   trek.with {
  //     damageControl.inflictDamage( 3, 9 ) // 9 units of damage to LR sensor.
  //     assert damageControl.isDamaged( DeviceType.lrSensor )
  //     trek.longRangeScan()
  //   }
  //   logger.debug ui
  //   assert ui.msgLog.empty
  //   assert ui.localMsgLog == ['sensors.longRange.offline']
  //
  //   logger.info 'testScanDamaged -- OK'
  // }
}
