package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
import groovy.transform.TypeChecked;

import static ShipDevice.DeviceType;
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
final class TrekSetCourseTest extends TrekTestBase {

  private MockFor shipMock; /// @todo Duplicated in TrekLongRangeScanTest

  @TypeChecked
  @Override void setUp() {
    super.setUp()
    trek = new Trek( ui )
  }

  void testSetBadCourseWithDamagedEngines() {
    logger.info 'testSetBadCourseWithDamagedEngines'
    ui.inputValues = [1,1]

    MockFor dcMock = new MockFor( DamageControl )
    dcMock.demand.isDamaged { DeviceType dt ->
      assert dt == DeviceType.engine
      logger.trace "Called with correct DeviceType: $dt"
      true
    }
    resetShip(0).use {
      trek.ship = new FederationShip()

      dcMock.use {
        trek.damageControl = new DamageControl()
        trek.setCourse()
      }
    }

    logger.info ui
    assert ui.inputValues.size() == 0
    assert ui.msgLog.size()   == 2
    assert ui.msgLog.first()  == 'Warp engines are damaged.'
    assert ui.msgLog.last()   == 'Maximum speed is .2'
    logger.info 'testSetBadCourseWithDamagedEngines -- OK'
  }

  private MockFor resetShip( final int expectedPositionCalls ) {
    shipMock = new MockFor( FederationShip )
    shipMock.demand.getPosition( expectedPositionCalls ) {
      Coords2d c2d = [row:1, col:1]
      new Position( c2d, c2d )
    }
    shipMock
  }

  void testGoodCourseWithDamagedEngines() {
    logger.info '-----'
    logger.info 'testGoodCourseWithDamagedEngines'
    ui.inputValues = [1F, 0.2F]

    MockFor shipMock = resetShip(1)
    shipMock.demand.move { ShipVector vector ->
      assert vector.course == 1
      assert vector.warpFactor == 0.2F
    }

    MockFor fleetMock = new MockFor( EnemyFleet )
    fleetMock.demand.canAttack { false }
    fleetMock.demand.getDefeated { true }

    MockFor dcMock = new MockFor( DamageControl )
    dcMock.demand.isDamaged(0) { DeviceType dt -> // Note: 0 expected calls
      assert dt == DeviceType.engine
      logger.info "Called with correct DeviceType, but should NOT be called at all!"
      assert false
      true
    }

    dcMock.demand.repair {  Closure localMsg ->
      localMsg 'damage.control.repair', ['hello','world'] as Object[]
    }

    MockFor rnd = new MockFor( java.util.Random )
    rnd.demand.nextFloat { ->
      logger.info 'Bypassings the device status lottery.'
      1F
    }

    fleetMock.use {
      trek.enemyFleet = new EnemyFleet()

      dcMock.use {
        trek.damageControl = new DamageControl()

        shipMock.use {
          trek.ship = new FederationShip()
          rnd.use {
            trek.setCourse()
          }
        }
      }
    }

    assert ui.inputValues.empty
    assert ui.msgLog.size() == 1
    assert ui.msgLog.first().contains( 'hello' )
    assert ui.msgLog.first().contains( 'world' )

    logger.info 'testGoodCourseWithDamagedEngines -- OK'
  }
}
