package net.ebdon.trk21;

import static GameSpace.*;
import static Quadrant.*;
import static ShipDevice.*;

import java.text.MessageFormat;
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager;
import groovy.transform.TypeChecked;

/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
 * @copyright   Terry Ebdon, 2019
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

/**
@brief A Groovy version of the 1973 BASIC-PLUS program TREK.BAS
@author Terry Ebdon
@date JAN-2019
*/
@groovy.util.logging.Log4j2
final class Trek extends LoggingBase {
  static String logPositionPieces = 'Positioning game pieces {} in quadrant {}'

  UiBase ui;
  PropertyResourceBundle rb;
  MessageFormat formatter;

  /// @todo Rename Trek.game, it's a misleading name for TrekCalendar
  TrekCalendar game;

  Galaxy    galaxy;
  Quadrant  quadrant  = new Quadrant();

  DamageControl   damageControl;
  EnemyFleet      enemyFleet;
  FederationShip  ship;

  @TypeChecked
  boolean isValid() {
    ship?.valid && game?.valid && enemyFleet?.valid
  }

  String toString() {
    "Calendar   : $game\n" +
    "Ship       : $ship\n" +
    "EnemyFleet : $enemyFleet\n" +
    "Quadrant   : $ship.position.quadrant\n" +
    "Sector     : $ship.position.sector"
  }

  Trek( UiBase theUi = null ) {
    ui = theUi
    formatter = new MessageFormat('')
    formatter.locale = Locale.default

    ClasspathResourceManager resourceManager = new ClasspathResourceManager()
    InputStream inStream = resourceManager.getInputStream('Language.properties')

    if ( inStream ) {
      rb = new PropertyResourceBundle( inStream )
      damageControl = new DamageControl()
    } else {
        log.fatal 'Could not load Language.poperties'
        assert inStream
    }
  }

  @TypeChecked
  void setupGalaxy() {
    galaxy = new Galaxy()
    setEntStartPosition()
    new GalaxySetup( galaxy, enemyFleet ).addGamePieces()
    dumpGalaxy()
    setupQuadrant()
  }

  @SuppressWarnings('InsecureRandom')
  void setEntStartPosition() {
    ship.position.quadrant = new Coords2d( *(galaxy.randomCoords) )
  }

  @TypeChecked
  void setupQuadrant() {
    QuadrantManager qm = new QuadrantManager( quadrant )
    ship.position.sector = qm.positionShip()
    qm.positionGamePieces(
      galaxy[ship.position.quadrant],
      enemyFleet
    )
  }

  @TypeChecked
  void dumpGalaxy() {
    galaxy.dump()
    reportEntPosition()
  }

  void reportEntPosition() {
    log.info "Enterprise is in quadrant ${currentQuadrant()}"
  }

  @TypeChecked
  String currentQuadrant() {
    "${ship.position.quadrant.col} - ${ship.position.quadrant.row}"
  }

  @TypeChecked
  void setupGame() {
    // try {
    logException {
      enemyFleet = new EnemyFleet()
      ship = new FederationShip( id: 'Ship' )
      game = new TrekCalendar()
      setupGalaxy()
    }
    // } catch ( Exception ex ) {
    //   log.fatal 'Unexpected exception while setting up the game.'
    //   log.error ex.message
    //   throw ex
    // }
  }

  @TypeChecked
  void startGame() {
    shortRangeScan()
  }

  /// @deprecated Not needed with new font config.
  @TypeChecked
  String btnText( final String text ) {
    // "<html><font size=+3>$text</font></html>"
    text
  }

  @TypeChecked
  void reportDamage() {
    damageControl.report( ui )
  }

  @TypeChecked
  void klingonAttack() {
    if ( !ship.protectedByStarBase ) {
      enemyFleet.attack ship.position.sector, ui.&fmtMsg
    } else {
      ui.localMsg 'starbase.shields'
    }
  }

  @TypeChecked
  void enemyAttacksBeforeShipCanMove() {
    if ( enemyFleet.canAttack() ) {
      log.info 'Klingons attack before the ship can move away.'
      klingonAttack()
    }
  }

  @TypeChecked
  boolean tooFastForDamagedEngine( final ShipVector sv ) {
    log.trace "tooFastForDamagedEngine called with $sv"
    sv.warpFactor > 0.2F && damageControl.isDamaged( ShipDevice.DeviceType.engine )
  }

  @TypeChecked
  @SuppressWarnings('InsecureRandom')
  void setCourse() {
    final Tuple2<Boolean, ShipVector> rv = getShipCourse()
    final ShipVector vector = rv.second
    final boolean rejected = rv.first

    if ( vector?.valid ) {
      log.info "Got a good vector: $vector"

      if ( tooFastForDamagedEngine( vector ) ) {
        ui.localMsg 'engine.damaged'
        ui.localMsg 'engine.damaged.max'
      } else {
        enemyAttacksBeforeShipCanMove()
        damageControl.repair( ui.&fmtMsg )
        /// @todo Is repair() called at the correct point?

        if ( new Random().nextFloat() <= 0.20 ) { // 1750
          DeviceStatusLottery.run( damageControl, ui.&localMsg )
        }
        game.tick() // Line 1830
        final Coords2d oldQuadrant = ship.position.quadrant.clone()
        ship.move( vector ) // set course, start engines...

        if ( gameContinues() ) {
          log.trace 'Ship has moved.'
          // Continue from line 1840...

          Repositioner rp = new ShipRepositioner(
            ship:     ship,
            ui:       ui,
            quadrant: quadrant
          )

          rp.repositionShip vector
          repopulateSector oldQuadrant
          shortRangeScan()
        }
      }
    } else {
      log.info "vector is not so good: $vector"
      if ( rejected ) { // User didn't hit cancel
        ui.localMsg 'input.vector.bad'
      }
    }
  }

  @TypeChecked
  private void repopulateSector( final Coords2d oldQuadrant ) {
    log.info "Quadrant was $oldQuadrant"
    log.info "Quadrant now $ship.position.quadrant"
    if ( ship.position.quadrant != oldQuadrant ) {
      log.info "Ship jumped to new quadrant $ship.position.quadrant"
      log.info "Setup in new quadrant at $ship.position"
      setupQuadrant()
    } else {
      log.info "Ship didn't jump quadrants."
    }
  }

  private float requestCourse() {
    ui.getFloatInput( rb.getString( 'input.course' ) ) // C1
  }

  /// @todo Test needed for getShipCourse()
  Tuple2<Boolean,ShipVector> getShipCourse() {
    boolean rejected = false
    float course        = 0
    float warpFactor    = 0
    ShipVector sv = new ShipVector()

    log.trace '''Getting ship's course'''

    course = requestCourse()
    if ( ShipVector.isValidCourse( course ) ) {
      sv.course = course
      warpFactor = ui.getFloatInput( rb.getString( 'input.speed' ) ) // W1
      if ( ShipVector.isValidWarpFactor( warpFactor ) ) {
        sv.warpFactor = warpFactor
      } else {
        rejected = warpFactor
        log.info "Warp factor $warpFactor is outside of expected range."
      }
    } else {
      log.info "Course value $course is outside of expected range."
      rejected = course
    }
    [rejected,sv]
  }

  /// Perform a @ref TrekLongRangeSensors "long-range sensor scan"
  @TypeChecked
  void longRangeScan() {
    new LongRangeScan( ui, damageControl, galaxy ).
        scanAround( ship.position.quadrant)
  }

  @TypeChecked
  void navComp() {
    final boolean srSensorDamaged = damageControl.isDamaged( DeviceType.srSensor )
    new NavComp( ui, srSensorDamaged, ship.position, quadrant ).run()
  }

  /// Perform a @ref TrekShortRangeSensors "short-range sensor scan"
  @TypeChecked
  void shortRangeScan() {
    logException {
      if ( damageControl.isDamaged( ShipDevice.DeviceType.srSensor ) ) {
        ui.localMsg 'sensors.shortRange.offline'
      } else {
        new ShortRangeScan(
          ship, quadrant, galaxy, ui, game.currentSolarYear,
          enemyFleet.numKlingonBatCrRemain ).scan()
      }
      log.info game.toString()
    }
  }

  @TypeChecked
  private void attackFleetWithPhasers( final int energy ) {
    new Battle(
      enemyFleet, ship, damageControl, ui
    ).phaserAttackFleet( energy )
  }

  @TypeChecked
  void updateQuadrantAfterSkirmish( final Thing thingDestroyed = Thing.emptySpace ) {
    log.debug 'updateQuadrantAfterSkirmish BEGIN'
    final GalaxyManager gm = new GalaxyManager( ship.position.quadrant, galaxy )

    if ( thingDestroyed != Thing.emptySpace ) {
      gm.updateNumInQuad thingDestroyed
    } else {
      log.debug 'Destroyed {}, nothing to do. )', thingDestroyed
    }
    gm.updateNumEnemyShips enemyFleet.numKlingonBatCrInQuad
    new QuadrantSetup( quadrant, enemyFleet ).updateAfterSkirmish()
    log.debug 'updateQuadrantAfterSkirmish END'
  }

  final void fireTorpedo() {
    log.info "Fire torpedo - available: ${ship.numTorpedoes}"

    float course = requestCourse()
    if ( course ) {
      if ( ship.numTorpedoes ) {
        Battle battle = new Battle(
          enemyFleet, ship, damageControl, ui
        )
        battle.fireTorpedo course, quadrant
        updateQuadrantAfterSkirmish battle.thingDestroyed
      } else {
        ui.fmtMsg 'torpedo.unavailable'
      }
    }
    log.info "Fire torpedo completed - available: ${ship.numTorpedoes}"
  }

  final int uiIntegerInput( final String propertyKey ) {
    ui.getFloatInput( rb.getString( propertyKey ) ).toInteger()
  }

  @TypeChecked
  final void firePhasers() {
    log.info "Fire phasers - available energy: ${ship.energyNow}"

    final int energy = uiIntegerInput( 'input.phaserEnergy' )

    if ( energy > 0 ) {
      if ( energy <= ship.energyNow ) {
        attackFleetWithPhasers energy
        updateQuadrantAfterSkirmish()
      } else {
        log.info 'Phaser command refused; user tried to fire too many units.'
        ui.localMsg 'phaser.refused.badEnergy'
      }
    } else {
      log.info 'Command cancelled by user.'
    }
    log.info "Fire phasers completed - available energy: ${ship.energyNow}"
  }

  @TypeChecked
  void victoryDance() {
    ui.fmtMsg 'trek.victoryDance', [ game.currentSolarYear,
      enemyFleet.numKlingonBatCrTotal,
      game.elapsed(),
      rating() ]
  }

  @TypeChecked
  private int rating() {
    assert game.elapsed() > 0
    (enemyFleet.numKlingonBatCrTotal / game.elapsed() * 1000).toInteger()
  }

  @TypeChecked
  void shipDestroyed() {
    ui.fmtMsg 'trek.funeral', [ game.currentSolarYear,
      game.elapsed(), enemyFleet.numKlingonBatCrRemain ]
  }

  @TypeChecked
  boolean gameContinues() {
    !gameWon() && !gameLost()
  }

  @TypeChecked
  boolean gameWon() {
    enemyFleet.defeated
  }

  @TypeChecked
  boolean gameLost() {
    ship.deadInSpace() || game.outOfTime()
  }
}
