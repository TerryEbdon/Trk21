package net.ebdon.trk21;

import groovy.test.GroovyTestCase
import groovy.mock.interceptor.MockFor;
import net.ebdon.trk21.battle_management.AfterSkirmish
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
    trek.trackLog.clear()
  }

  @Newify([Position,Coords2d])
  void testUpdateQuadrantAfterSkirmish() {
    MockFor shipMock      = MockFor( FederationShip )
    MockFor galaxy        = MockFor( Galaxy )
    MockFor enemyFleet    = MockFor( EnemyFleet )
    MockFor afterSkirmish = MockFor( AfterSkirmish )

    afterSkirmish.demand.
      updateQuadrant { Quadrant.Thing thingDestroyed, EnemyFleet fleet -> }
    shipMock.demand.getPosition { Position( Coords2d(1,2), Coords2d(3,4) ) }

    shipMock.use {
      trek.ship = new FederationShip()
      galaxy.use {
        trek.galaxy = new Galaxy()
        enemyFleet.use {
          trek.enemyFleet = new EnemyFleet()
          afterSkirmish.use {
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

  void testShowHistory() {
    logger.info '> testShowHistory'
    final String lastQuadrantString = 'F - U'
    final String historyFormatId = 'trek.historyEntry'
    final int numQuadrantsToVisit = 6
    final int numQuadrantsPerLine = 5
    final int numQuadrantCoordToStrings = (1..numQuadrantsToVisit).sum()

    char row
    char col

    UiBase ui            = new TestUi()
    MockFor coords2dMock = MockFor( Coords2d )
    coords2dMock.demand.toString(numQuadrantCoordToStrings) {
      String rv = "${row++} - ${col--}"
      logger.info "Coords2dMock.toString() returning $rv"
      rv
    }

    trek.ui = ui
    coords2dMock.use {
      1.upto(numQuadrantsToVisit) { int numQuadrantsVisited ->
        ui.msgLog = [ ]
        ui.localMsgLog = [ ]
        logger.debug sprintf( 'Visit %1d: %2d messages, %1d quadrants.%n',
          numQuadrantsVisited, ui.msgLog.size(), trek.trackLog.size()
        )
        trek.trackLog << new Coords2d()
        row = 'A'
        col = 'Z'
        trek.showHistory()
        assert ui.localMsgLog.size() == numQuadrantsVisited
        assert ui.localMsgLog.last() == historyFormatId
        assert numQuadrantsVisited in 1..numQuadrantsToVisit

        switch (numQuadrantsVisited) {
          case 1.. numQuadrantsPerLine - 1 -> {
            assert ui.msgLog.size() == 0
          }
          case numQuadrantsPerLine -> {
            assert ui.msgLog.size() == 1
            assert ui.msgLog.first() == '\n'
          }
          case numQuadrantsPerLine..numQuadrantsToVisit -> {
            assert ui.msgLog.size() == 1
            assert ui.msgLog.first() == '\n'
          }
        }
      }
    }
    assert ui.localMsgLog.last() == historyFormatId
    assert ui.argsLog.last() == [ lastQuadrantString, ]
    logger.info '< testShowHistory'
  }

  void testStartGame() {
    UiBase ui             = new TestUi()
    MockFor shipMock      = MockFor( FederationShip )
    MockFor coords2dMock  = MockFor( Coords2d )
    MockFor positionMock  = MockFor( Position)
    MockFor damageControl = MockFor( DamageControl )

    damageControl.demand.isDamaged { true }

    coords2dMock.demand.with {
      clone { }
    }

    coords2dMock.use {
      positionMock.demand.getQuadrant { new Coords2d() }

      positionMock.use {
        shipMock.demand.getPosition { new Position() }

        shipMock.use {
          damageControl.use {
            trek.ship = new FederationShip()
            trek.damageControl = new DamageControl()
            trek.ui = ui
            trek.startGame()
          }
        }
      }
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

  @Newify(Coords2d)
  void testUpdateTrackLog() {
    logger.info '> testUpdateTrackLog'

    MockFor shipMock = MockFor(FederationShip)
    final int move1QuadRow = 3
    final int move1QuadCol = 5

    final Coords2d mockQuadrant = Coords2d(move1QuadRow,move1QuadCol)
    final Position mockPosition = new Position(mockQuadrant, Coords2d(2, 4))

    shipMock.demand.getPosition(2) { mockPosition }

    shipMock.use {
      trek.ship = new FederationShip()

      // There will be one entry at game start
      trek.trackLog = [ Coords2d(1, 1) ]
      assert trek.trackLog.size() == 1

      trek.updateTrackLog() // Log move to different quadrant
      assert trek.trackLog.size() == 2

      final Coords2d loggedQuadrant = trek.trackLog.last()
      assert loggedQuadrant.row == move1QuadRow
      assert loggedQuadrant.col == move1QuadCol

      trek.updateTrackLog() // Add duplicate, i.e. ship has not changed quadrant
      assert trek.trackLog.size() == 2 // Duplicate rejected
    }

    logger.info '< testUpdateTrackLog'
  }
}
