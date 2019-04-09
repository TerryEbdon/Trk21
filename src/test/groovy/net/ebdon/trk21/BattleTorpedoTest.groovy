package net.ebdon.trk21;

import static GameSpace.*;
import static ShipDevice.*;
import static Quadrant.Thing;

import groovy.mock.interceptor.MockFor;
import groovy.transform.TypeChecked;

/**
 * @file
 * @author      Terry Ebdon
 * @date        April 2019
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
@Newify([MockFor,Position,Coords2d])
class BattleTorpedoTest extends GroovyTestCase {
  private Battle battle;
  private TestUi ui;

  private MockFor quadMock;
  private MockFor trepMock;
  private MockFor shipMock;
  private MockFor fleetMock;
  private MockFor torpMock;

  private Position shipPos;
  private Position targetPos;

  @Override void setUp() {
    super.setUp()
    ui = new TestUi()
    quadMock  = MockFor( Quadrant )
    trepMock  = MockFor( TorpedoRepositioner )
    shipMock  = MockFor( FederationShip )
    fleetMock = MockFor( EnemyFleet )
    torpMock  = MockFor( Torpedo )

    shipPos  = Position( Coords2d(1, 1), Coords2d(4, 4) )
    targetPos = Position( Coords2d(1, 1), Coords2d(4, 5) )

    initDemands()
  }

  private void initDemands() {
    shipMock.demand.with {
      getTorpedo  { new Torpedo() }
      getPosition { shipPos }
    }

    quadMock.demand.with {
      putAt { Coords2d sector, Thing thing ->
        logger.debug 'put {} at sector {}', thing, sector
        assert thing == Thing.torpedo // Swap the ship out for a torpedo.
        assert sector == shipPos.sector
      }
      putAt { Coords2d sector, Thing thing ->
        logger.debug 'put {} at sector {}', thing, sector
        assert thing == Thing.ship  // Put the ship back where it belongs.
        assert sector == shipPos.sector
      }
    }
  }

  private void torpedoDemand( final Thing thingHit ) {
    torpMock.demand.with {
      getPosition { shipPos }
      if ( thingHit != Thing.emptySpace ) {
        getPosition { targetPos }
      }
    }
  }

  private void fleetDemand( final Thing thingHit ) {
    fleetMock.demand.with {
      if ( thingHit == Thing.enemy ) {
        shipHitByTorpedo { Coords2d sector -> assert sector == targetPos.sector }
      }
      regroup   { logger.debug 'regroup() called.' }
      canAttack { logger.debug 'canAttack() called'; false }
    }
  }

  private void trepDemand( final Thing thingHit ) {
    trepMock.demand.with {
      repositionShip { logger.debug 'repositionShip() called' }
      getThingHit { thingHit }
    }
  }

  private void runTheTest() {
    fleetMock.use {
      torpMock.use {
        shipMock.use {
          battle = new Battle(
            enemyFleet: new EnemyFleet(),
            ship      : new FederationShip(),
            ui        : ui
          )

          trepMock.use {
            torpMock.use {
              quadMock.use {
                Quadrant quad = new Quadrant()
                battle.fireTorpedo( 1F, quad )
                logger.debug 'fireTorpedo succeeded'
              }
            }
          }
        }
      }
    }
  }

  @TypeChecked
  private void demandsForHitOn( final Thing thingHit ) {
    trepDemand thingHit
    fleetDemand thingHit
    torpedoDemand thingHit
  }

  @TypeChecked
  private void uiChecks( final List locMsgLog, final List msgLog ) {
    assert ui.localMsgLog == locMsgLog
    assert ui.msgLog == msgLog
  }

  @TypeChecked
  private void runTestAndCheckUi( final Thing thingHit, final List locMsgLog, final List msgLog ) {
    demandsForHitOn thingHit
    runTheTest()
    uiChecks locMsgLog, msgLog
  }

  @SuppressWarnings('JUnitTestMethodWithoutAssert')
  void testTorpedoHitOnStar() {
    logger.info 'testTorpedoHitOnStar -- BEGIN'
    quadMock.demand.putAt { Coords2d sector, Thing thing ->
      assert thing == Thing.emptySpace
      assert sector == targetPos.sector
    }

    runTestAndCheckUi Thing.star, [], []
    logger.info 'testTorpedoHitOnStar -- END'
  }

  @TypeChecked
  @SuppressWarnings('JUnitTestMethodWithoutAssert')
  void testTorpedoHitOnEnemy() {
    logger.info 'testTorpedoHitOnEnemy -- BEGIN'
    runTestAndCheckUi Thing.enemy, ['battle.enemy.destroyed'], []
    logger.info 'testTorpedoHitOnEnemy -- END'
  }

  @TypeChecked
  @SuppressWarnings('JUnitTestMethodWithoutAssert')
  void testTorpedoHitNothing() {
    logger.info 'testTorpedoHitNothing -- BEGIN'
    runTestAndCheckUi Thing.emptySpace, [], []
    logger.info 'testTorpedoHitNothing -- END'
  }
}
