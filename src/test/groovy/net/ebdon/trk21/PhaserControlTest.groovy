package net.ebdon.trk21;

import groovy.util.logging.Log4j;
import static GameSpace.*;
import static ShipDevice.*;
import groovy.mock.interceptor.StubFor;
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
/// todo Lots of `1` and `8` instances are hard-coded in this class.
final class PhaserControlTest extends GroovyTestCase {

  private PhaserControl pc;
  private DamageControl dc;
  private boolean damageReported;
  private def ship;
  private def battle;
  private StubFor shipStub;
  private StubFor battleStub;
  private String reportedMsg = "";
  private newEnergy;
  private int energyAtStart;

  @Override void setUp() {
    logger.info "setUp()"
    dc = new DamageControl()
    shipStub = new StubFor( FederationShip )
    shipStub.demand.deadInSpace {true}
    shipStub.use {
      ship = new FederationShip()
    }

    battleStub = new StubFor( Battle )
    battleStub.use {
      battle = new Battle()
    }
    pc = new PhaserControl( dc, this.&reporter, ship, battle )
    damageReported = false
    newEnergy = 0
    reportedMsg = ''
  }

  private def getLog() { assert false }

  private void reporter( msg ) {
    reportedMsg = msg
    logger.info msg
    damageReported = true
  }

  void testEnergyValue() {
    logger.info 'testEnergyValue'
    [0,-1].each { energyValue ->
      shouldFail( org.codehaus.groovy.runtime.powerassert.PowerAssertionError ) {
        pc.fire energyValue
      }
    }
    shipStub.demand.getEnergyNow(1..99) { 0 }
    shipStub.use {
      pc.fire 1 + ship.energyNow
    }
    assert damageReported
    assert reportedMsg.contains( 'Command refused' )
    logger.info 'testEnergyValue -- OK'
  }

  void testCantFire() {
    logger.info 'testCantFire'
    if ( notYetImplemented() ) return
    // damageControl[phasers.ordinal()]
    pc.fire( 100 )
    assert false
    logger.info 'testCantFire -- OK'
  }

  private void newEnergyReporter( newVal) {
    energyReporterCalled = true
    newEnergy = newVal
  }

  private void fireAtTargets( Closure populateTargets, fireAmount=100 ) {
    logger.info 'fireAtTargets'
    setupShipForFiring()
    populateTargets()
    battleStub.demand.hitOnFleetShip(1..99) { xp, amount ->
      logger.trace "battleStub.hitOnFleetShip xp:$xp amount:$amount"
    }
    shipStub.demand.energyReducedByPhaserUse { amount ->
      assert amount == fireAmount
      newEnergy = energyAtStart - amount
    }
    shipStub.use {
      pc.fire( fireAmount )
    }

    assert newEnergy == energyAtStart - fireAmount
    logger.info 'fireAtTargets -- OK'
  }

  void testFireAtGoodTargets() {
    logger.info 'testFireAtGoodTargets'
    final int fireAmount = 100
    fireAtTargets this.&setupGoodTargets
    logger.info 'testFireAtGoodTargets -- OK'
  }

  void testFireAtBadTarget() {
    logger.info 'testFireAtBadtarget'
    shouldFail( org.codehaus.groovy.runtime.powerassert.PowerAssertionError ) {
      fireAtTargets this.&setupBadTargets
    }
    logger.info 'testFireAtBadtarget -- OK' /// @todo finish this.
  }

  private void setupShipForFiring() {
    final Closure energyReporter = this.&newEnergyReporter
    final shipQuadrant = new Coords2d( row: 1, col: 1 )
    energyAtStart = 3000

    shipStub.demand.with {
      getEnergyNow(1..99) { energyAtStart }

      getPosition(1..99) {
        new Position().tap {
          quadrant  = shipQuadrant
          sector    = new Coords2d( row: 1, col: 1 )
        }
      }
    }
  }

  private void setupTargetsIn( final Coords2d targetSector ) {
    for ( int enemyNo = 9; enemyNo > 0; enemyNo-- ) {
      logger.info "Setting up target $enemyNo"
      final def xp = new Expando(
        name: sprintf("Enemy ship No. %d", enemyNo ),
        sector: targetSector.clone(),
        enemyNum: enemyNo
      )

      battleStub.demand.getNextTarget { xp }
    }
    battleStub.demand.getNextTarget { null }
  }

  private void setupBadTargets() {
    setupTargetsIn new Coords2d( row: 0, col: 0 )
  }

  private void setupGoodTargets() {
    setupTargetsIn new Coords2d( row: 8, col: 8 )
  }
  // private void setupTargetsIn( final Coords2d shipQuadCoords ) {
  //   battleStub.demand.with {
  //     for ( int enemyNo = 9; enemyNo > 0; enemyNo-- ) {
  //       getNextTarget {
  //         new Expando(
  //           name: "Enemy ship No. $enemyNo",
  //           sector: new Coords2d( row:8, col:8 )
  //         )
  //       }
  //     }
  //     getNextTarget { null }
  //   }
  // }
}
