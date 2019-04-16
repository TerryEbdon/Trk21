package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError;

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
@Newify(MockFor)
final class TrekTest extends GroovyTestCase {

  private Trek trek;

  @Override
  void setUp() {
    super.setUp()
    logger.info 'setUp'
    Trek.config = true
    trek = new Trek()
  }

  @SuppressWarnings('ExplicitCallToGetAtMethod')
  @Newify([Position,Coords2d])
  void testUpdateQuadrantAfterSkirmish() {
    MockFor shipMock      = MockFor( FederationShip )
    MockFor galaxy        = MockFor( Galaxy )
    MockFor quadrantSetup = MockFor( QuadrantSetup )
    MockFor enemyFleet    = MockFor( EnemyFleet )

    // Start of demand required for updateNumEnemyShipsInQuad
    shipMock.demand.getPosition { Position( Coords2d(1,2), Coords2d(3,4) ) }
    enemyFleet.demand.getNumKlingonBatCrInQuad { 3 }
    galaxy.demand.with {
      getAt { Coords2d c2d ->
        assert c2d == Coords2d(1,2)
        919
      }

      getAt { Coords2d c2d -> assert c2d == Coords2d(1,2); 919 } // enemy @ start
      putAt { Coords2d c2d, int newVal -> assert newVal ==  19 } // remove them
      getAt { Coords2d c2d -> assert c2d == Coords2d(1,2);  19 }
      putAt { Coords2d c2d, int newVal -> assert newVal == 319 } // sync with fleet
    }
    // end of demand required for updateNumEnemyShipsInQuad

    quadrantSetup.demand.updateAfterSkirmish { }

    shipMock.use {
      trek.ship = new FederationShip()
      galaxy.use {
        trek.galaxy = new Galaxy()
        enemyFleet.use {
          trek.enemyFleet = new EnemyFleet()
          quadrantSetup.use {
            trek.updateQuadrantAfterSkirmish()
          }
        }
      }
    }
  }

  void testNavComp() {
    MockFor navComp       = MockFor( NavComp )
    MockFor damageControl = MockFor( DamageControl )
    MockFor shipMock      = MockFor( FederationShip )
    MockFor quadMock      = MockFor( Quadrant )

    damageControl.demand.isDamaged { ShipDevice.DeviceType dt ->
      assert dt == ShipDevice.DeviceType.srSensor
      false
    }

    shipMock.demand.getPosition { new Position() }
    navComp.demand.run { }

    damageControl.use {
      trek.damageControl = new DamageControl()
      shipMock.use {
        trek.ship = new FederationShip()
        quadMock.use {
          trek.quadrant = new Quadrant()
          navComp.use {
            trek.navComp()
          }
        }
      }
    }
  }

  @SuppressWarnings('JUnitTestMethodWithoutAssert')
  void testReportDamage() {
    MockFor damageControl = MockFor( DamageControl )
    damageControl.demand.report { }

    damageControl.use {
      trek.damageControl = new DamageControl()
      trek.reportDamage()
    }
  }

  void testStartGame() {
    UiBase ui = new TestUi()
    MockFor damageControl = MockFor( DamageControl )
    damageControl.demand.isDamaged { true }

    damageControl.use {
      trek.damageControl = new DamageControl()
      trek.ui = ui
      trek.startGame()
    }

    assert ui.localMsgLog == ['sensors.shortRange.offline']
  }

  void testVictory() {
    final int solarYear = 12345
    final int numEnemyDestroyed = 20
    final int timePlayed = 10
    final int rating = numEnemyDestroyed / timePlayed * 1000

    UiBase ui = new TestUi()
    MockFor gameMock = MockFor( TrekCalendar )
    MockFor fleetMock = MockFor( EnemyFleet )
    gameMock.demand.getCurrentSolarYear { solarYear }
    gameMock.demand.elapsed(3) { timePlayed }
    fleetMock.demand.getNumKlingonBatCrTotal(2) { numEnemyDestroyed }

    trek.ui = ui
    gameMock.use {
      trek.game = new TrekCalendar()
      fleetMock.use {
        trek.enemyFleet = new EnemyFleet()
        trek.victoryDance()
      }
    }

    assert ui.localMsgLog == ['trek.victoryDance']
    assert ui.argsLog.first() == [solarYear, numEnemyDestroyed, timePlayed, rating]
  }

  void testFuneral() {
    final int solarYear = 12345
    final int numEnemyNotDestroyed = 20
    final int timePlayed = 10

    UiBase ui         = new TestUi()
    MockFor gameMock  = MockFor( TrekCalendar )
    MockFor fleetMock = MockFor( EnemyFleet )
    gameMock.demand.getCurrentSolarYear { solarYear }
    gameMock.demand.elapsed { timePlayed }
    fleetMock.demand.getNumKlingonBatCrRemain { numEnemyNotDestroyed }

    trek.ui = ui
    gameMock.use {
      trek.game = new TrekCalendar()
      fleetMock.use {
        trek.enemyFleet = new EnemyFleet()
        trek.shipDestroyed()
      }
    }

    assert ui.localMsgLog == ['trek.funeral']
    assert ui.argsLog.first() == [solarYear, timePlayed, numEnemyNotDestroyed]
  }
}
