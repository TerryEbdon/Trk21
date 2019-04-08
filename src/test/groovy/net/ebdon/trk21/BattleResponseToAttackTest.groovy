package net.ebdon.trk21;

import groovy.mock.interceptor.StubFor;
import groovy.mock.interceptor.MockFor;
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

class BattleResponseToAttackTest extends GroovyTestCase {
  private Battle battle;
  private EnemyFleet enemyFleet;
  private FederationShip ship;
  private DamageControl dc;

  private MockFor fleetMock;
  private StubFor dcStub;
  private MockFor shipMock;
  private List<String> attackReporterMessages;
  private TestUi ui;

  @Override void setUp() {
    super.setUp()
    attackReporterMessages = [];
    dcStub = new StubFor( DamageControl )
    dcStub.use { dc = new DamageControl() }

    shipMock = new MockFor( FederationShip )
    shipMock.use { ship = new FederationShip() }

    fleetMock = new MockFor( EnemyFleet )
    fleetMock.use { enemyFleet = new EnemyFleet() }
    ui = new TestUi()
    battle = new Battle( enemyFleet, ship, dc, ui )
  }

  void testEnemyCanNotRespondToAttack() {
    fleetMock.demand.with {
      regroup { }
      canAttack { false }
    }

    fleetMock.use {
      battle.enemyFleet = new EnemyFleet()
      battle.enemyRespondsToAttack()
    }

    assert ui.empty
  }

  void testEnemyRespondsToAttack() {
    fleetMock.demand.with {
      regroup { }
      canAttack { true }
      attack { Coords2d sector, Closure fleetAttackReporter -> }
    }

    shipMock.demand.isProtectedByStarBase { false }
    shipMock.demand.getPosition { new Position() }

    fleetMock.use {
      battle.enemyFleet = new EnemyFleet()
      shipMock.use {
        battle.ship = new FederationShip()
        battle.enemyRespondsToAttack()
      }
    }

    assert ui.empty
  }

  void testResponseWhenShipDocked() {
    fleetMock.demand.with {
      regroup { }
      canAttack { true }
    }

    shipMock.demand.isProtectedByStarBase { true }

    fleetMock.use {
      battle.enemyFleet = new EnemyFleet()
      shipMock.use {
        battle.ship = new FederationShip()
        battle.enemyRespondsToAttack()
      }
    }

    assert ui.localMsgLog == ['battle.shieldedByBase']
  }
}
