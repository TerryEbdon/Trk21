package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
import groovy.transform.TypeChecked;

import static ShipDevice.*;
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

final class DamageControlTest extends DeviceTestBase {

  private DamageControl dc;

  @Override void setUp() {
    super.setUp()
    damage[2].state = -3
    dc = new DamageControl( damage )
  }

  @TypeChecked
  void testDamageControl() {
    dc.with {
      final ShipDevice damaged = damage[ findDamagedDeviceKey() ]
      assert '222' == damaged.id
      assert -3 == damaged.state
      assert damaged.isDamaged()
    }
  }

  @TypeChecked
  void testFindDamagedWithNoDamage() {
    damage[2].state = 0
    assert dc.findDamagedDeviceKey() == 0
  }

  @TypeChecked
  void testRandomlyRepair() {
    final int oldState = damage[2].state
    dc.randomlyRepair 2
    assert damage[2].state > oldState
  }

  @TypeChecked
  void testDamageControlRepair() {
    final int oldState = damage[2].state
    dc.repair DamageControlRepairCallBackMock.&callBack
    assert DamageControlRepairCallBackMock.called
    assert DamageControlRepairCallBackMock.calledWith.size() > 0
    assert damage[2].state > oldState
  }

  void testDamageControlReport() {
    if ( notYetImplemented() ) return

    MockFor damageReporterMock = new MockFor( DamageReporter )
    damageReporterMock.demand.report { formatter -> }

    // damageReporterMock.use {
    //   dc.report( rb, Closure closure, formatter )
    // }
    // dc.report( rb, Closure closure, formatter )
    // MockFor drMock = new MockFor( DamageReporter )
    // drMock.demand.report { rb, Closure closure, formatter ->
    //
    // }
    // drMock.use {
    //   dc.report( {true} )
    // }
    assert false
  }

  class DamageControlRepairCallBackMock {

    static boolean called = false;
    static String calledWith = ''

    static void callBack( msg ) {
      called = true
      calledWith = msg
    }
  }
}
