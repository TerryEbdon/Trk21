package net.ebdon.trk21;

import groovy.test.GroovyTestCase
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

  private MockFor shipMock;
  private MockFor fleetMock;

  @TypeChecked
  @Override void setUp() {
    super.setUp()
    fleetMock = new MockFor( EnemyFleet )
    trek = new Trek( ui )
  }

  private void resetShip( final boolean shielded ) {
    shipMock = new MockFor( FederationShip )
    shipMock.demand.getProtectedByStarBase { shielded }
  }

  @Newify(Coords2d)
  void testKlingonAttack() {
    resetShip false
    Coords2d shipSector = [3,4]
    shipMock.demand.getPosition { new Position(Coords2d(1,2), shipSector.clone() ) }
    fleetMock.demand.attack { Coords2d sector, Closure closure ->
      assert sector == shipSector
      closure 'enemyFleet.hitOnFedShip', shipSector.toList()
    }

    shipMock.use {
      trek.ship = new FederationShip()
      fleetMock.use {
        trek.enemyFleet = new EnemyFleet()
        trek.klingonAttack()
      }
    }

    assert ui.msgLog.empty
    assert ui.localMsgLog == ['enemyFleet.hitOnFedShip']
    assert ui.argsLog ==  [ shipSector.toList() ]
  }

  void testKlingonAttackProtectedByStarBase() {
    resetShip true
    shipMock.use {
      trek.ship = new FederationShip()
      trek.klingonAttack()
    }

    assert ui.msgLog.empty
    assert ui.localMsgLog == ['starbase.shields']
  }
}
