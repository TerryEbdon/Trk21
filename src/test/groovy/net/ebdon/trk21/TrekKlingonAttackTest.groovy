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
final class TrekKlingonAttackTest extends TrekTestBase {

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
      isProtectedByStarBase { true }
    }
  }

  void testKlingonAttackProtectedByStarBase() {
    resetShip 0
    shipMock.use {
      trek.ship = new FederationShip()
      trek.klingonAttack()
    }

    assert ui.msgLog.size() == 1
    assert ui.msgLog.first() == 'Star Base shields protect the Enterprise.'
  }
}
