package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
import groovy.test.GroovyTestCase
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
final class DeviceStatusLotteryTest extends TrekTestBase {

  private MockFor damageControl;

  void testSpaceStorm() {
    damageControl = new MockFor( DamageControl ).tap {
      demand.getRandomDeviceKey { 1 }
      demand.inflictDamage { int systemToDamage, int damageAmount ->
        assert systemToDamage == 1
        assert (1..6).contains( damageAmount )
      }
      demand.deviceName { key ->
        assert key == 1
        'Some.device.name'
      }
    }
    damageControl.use {
      DeviceStatusLottery lottery = new DeviceStatusLottery()
      lottery.damageControl = new DamageControl()
      lottery.localMsg = ui.&localMsg

      lottery.spaceStorm()

      assert ui.msgLog.empty
      assert ui.localMsgLog.size() == 1
      assert ui.localMsgLog.first() == 'deviceStatusLottery.spaceStorm'
    }
  }

  void testRandomDeviceRepairNoDamage() {
    damageControl = new MockFor( DamageControl ).tap {
      demand.findDamagedDeviceKey { 0 }
    }
    damageControl.use {
      DeviceStatusLottery lottery = new DeviceStatusLottery()
      lottery.damageControl = new DamageControl()
      lottery.localMsg = ui.&localMsg

      lottery.randomDeviceRepair()

      assert ui.msgLog.size() == 0
      assert ui.localMsgLog.size() == 0
    }
  }

  void testRandomDeviceRepairWithDamage() {
    damageControl = new MockFor( DamageControl ).tap {
      demand.findDamagedDeviceKey { 1 }
      demand.randomlyRepair { int firstDamagedDeviceKey ->
        assert firstDamagedDeviceKey == 1
      }
      demand.deviceId { int deviceKey ->
        assert deviceKey == 1
        'Some device ID'
      }
    }

    damageControl.use {
      DeviceStatusLottery lottery = new DeviceStatusLottery()
      lottery.damageControl = new DamageControl()
      lottery.localMsg = ui.&localMsg

      lottery.randomDeviceRepair()

      assert ui.msgLog.size() == 0
      assert ui.localMsgLog.size() == 1
      assert ui.localMsgLog.first() == 'truce'
    }
  }
}
