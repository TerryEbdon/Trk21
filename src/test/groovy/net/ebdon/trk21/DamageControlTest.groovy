package net.ebdon.trk21;

import static ShipDevice.*;

import org.codehaus.groovy.runtime.powerassert.PowerAssertionError;
import groovy.mock.interceptor.MockFor;
import groovy.transform.TypeChecked;
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

@groovy.util.logging.Log4j2('logger')
final class DamageControlTest extends GroovyTestCase {

  private DamageControl dc;

  @Override void setUp() {
    super.setUp()
    dc = new DamageControl()
    dc.devices[2].state = -3
  }

  @TypeChecked
  void testDamageControl() {
    dc.with {
      final ShipDevice damageDevice = dc.devices[ findDamagedDeviceKey() ]
      assert damageDevice?.id   == 'device.S.R..SENSORS'
      assert damageDevice.state == -3
      assert damageDevice.isDamaged()
    }
  }

  @TypeChecked
  void testFindDamagedWithNoDamage() {
    dc.devices[2].state = 0
    assert dc.findDamagedDeviceKey() == 0
  }

  /// Test for Issue #36:
  /// Space storm damage to device 6, damage control, stack dumps.
  @TypeChecked
  private void checkInflictDamage( final int deviceNo ) {
    final Integer damageAmount    = 2
    final Integer oldState    = dc.devices[deviceNo]?.state

    dc.inflictDamage( deviceNo, damageAmount )

    assert dc.devices[deviceNo].state == oldState - damageAmount
  }

  /// Test for Issue #36:
  /// Space storm damage to device 6, damage control, stack dumps.
  @TypeChecked
  void testInflictDamage() {
    final int highestDeviceNo = dc.devices.size()

    checkInflictDamage highestDeviceNo

    shouldFail(PowerAssertionError) {
      checkInflictDamage highestDeviceNo + 1
    }
  }

  @TypeChecked
  void testRandomlyRepair() {
    // final int oldState = damage[2].state
    final int oldState = dc.devices[2].state

    dc.randomlyRepair 2

    assert dc.devices[2].state > oldState
  }

  @TypeChecked
  void testDamageControlRepair() {
    TestUi ui = new TestUi()
    final int oldState = dc.devices[2].state

    dc.repair ui.&fmtMsg

    assert ui.msgLog.empty
    assert ui.localMsgLog?.size() == 1
    assert ui.localMsgLog.first() == 'damage.control.repair'
    assert ui.argsLog?.size() == 1
    assert ui.argsLog == [ [dc.devices[2].name, dc.devices[2].state] ]
    assert dc.devices[2].state == oldState + 1
    assert dc.devices[1].state == 0
  }

  void testDamageControlReport() {
    final String localisedDeviceName = 'neutron blaster'
    TestUi ui = new TestUi()

    MockFor damageReporterMock = new MockFor( DamageReporter )
    damageReporterMock.demand.with {
      report { Map devices, Closure rbStringCl, Closure localMsgCl ->
        logger.debug 'in mock report()'
        assert devices.isEmpty() == false
        final String msg = rbStringCl( 'wibble' )
        assert msg == localisedDeviceName
        localMsgCl msg
        logger.debug 'About to exit from DamageReporter.report()'
      }
    }

    Closure localDeviceName = { String key ->
      logger.debug 'localDeviceName closure called with key {}', key
      localisedDeviceName
    }

    damageReporterMock.use {
      dc.report( localDeviceName, ui.&localMsg )
    }

    assert ui.localMsgLog.size() == 1
    assert ui.localMsgLog.first() == localisedDeviceName
  }
}
