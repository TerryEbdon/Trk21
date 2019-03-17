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
final class TrekSetCourseTest extends TrekTestBase {

  private MockFor shipMock; /// @todo Duplicated in TrekLongRangeScanTest

  @TypeChecked
  @Override void setUp() {
    super.setUp()
    trek = new Trek( ui )
  }

  void testSetBadCourseWithDamagedEngines() {
    ui.inputValues = [1,1]
    trek.damage[1].state = -1
    trek.setCourse()
    assert ui.inputValues.size() == 0
    assert ui.msgLog.size() == 1
    assert ui.msgLog.last().contains( 'Warp engines are damaged.\nMaximum speed is .2')
  }

  void testGoodBadCourseWithDamagedEngines() {
    if ( notYetImplemented() ) return

    ui.inputValues = [1, 0.2]
    trek.damage[1].state = -1

    MockFor fleetMock = new MockFor( EnemyFleet )
    fleetMock.demand.canAttack { false }
    fleetMock.use {
      trek.enemyFleet = fleetMock
    }

    MockFor dcMock = new MockFor( DamageControl )
    dcMock.demand.repair( { String -> } )
    dcMock.use { trek.damageControl = new DamageControl() }

    trek.setCourse()

    assert ui.inputValues.size() == 0
    assert ui.msgLog.size() == 1
    assert ui.msgLog.last().contains( 'Warp engines are damaged.\nMaximum speed is .2')
  }
}
