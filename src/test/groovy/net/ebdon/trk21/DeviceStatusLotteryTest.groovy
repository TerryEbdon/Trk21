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

/// @todo Consider renaming this to DeviceStatusLotteryTest
@groovy.util.logging.Log4j2('logger')
final class DeviceStatusLotteryTest extends TrekTestBase {

  private MockFor damageControl;

  @TypeChecked
  @Override void setUp() {
    super.setUp()
    setupDamageControl()
    // damageControl.use {
      trek = new Trek( ui )
    // }
  }

  private void setupDamageControl() {
    damageControl = new MockFor( DamageControl )
    // dcMock.demand...
  }

  @TypeChecked
  void testSpaceStorm() {
    assert !devicesAreDamaged
    trek.spaceStorm()
    assert ui.msgLog.size() == 1
    assert ui.msgLog.first().contains( '*** Space Storm, ' )
    assert devicesAreDamaged
  }

  @TypeChecked
  private boolean getDevicesAreDamaged() {
    trek.damageControl.findDamagedDeviceKey()
  }

  @TypeChecked
  void testRandomDeviceRepairNoDamage() {
    trek.randomDeviceRepair()
    assert ui.msgLog.size() == 0
  }

  @TypeChecked
  void testRandomDeviceRepairWithDamage() {
    trek.damageControl.devices[1].state = -1
    trek.randomDeviceRepair()
    assert trek.damageControl.devices[1].state == 0
    assert ui.msgLog.size() >= 1
    assert ui.msgLog.last().contains( 'state of repair improved' )
  }
}
