package net.ebdon.trk21;

import static GameSpace.*;
import static Quadrant.*;
import static ShipDevice.*;

import java.text.MessageFormat;
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager;
import groovy.transform.TypeChecked;
import net.ebdon.trk21.battle_management.AfterSkirmish;
import net.ebdon.trk21.arms_man.TorpedoManager;
import net.ebdon.trk21.arms_man.PhaserManager;
import net.ebdon.trk21.course_man.ShipCourseManager;
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
    log.info "Enterprise is in quadrant ${ship.position.quadrant.row}"
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
  void setCourse() {
    final Coords2d oldQuadrant = ship.position.quadrant.clone()
    final GameState gs = new GameState( enemyFleet, ship, game )
    final ShipCourseManager scm = new ShipCourseManager( ui, damageControl, ship, enemyFleet, gs )

    if ( scm.setOffFrom( quadrant ) ) {
      repopulateSector oldQuadrant
      shortRangeScan()
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

  /// @todo Test needed for getShipCourse()
  Tuple2<Boolean,ShipVector> getShipCourse() {
    boolean rejected = false
    float course        = 0
    float warpFactor    = 0
    ShipVector sv = new ShipVector()

    log.trace '''Getting ship's course'''

    course = ui.requestCourse()
    if ( ShipVector.isValidCourse( course ) ) {
      sv.course = course
      warpFactor = ui.getFloatInput( 'input.speed' ) // W1
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
  void updateQuadrantAfterSkirmish( final Thing thingDestroyed = Thing.emptySpace ) {
    new AfterSkirmish( quadrant, galaxy, ship.position.quadrant ).
      updateQuadrant( thingDestroyed, enemyFleet )
  }

  final void fireTorpedo() {
    TorpedoManager tm = new TorpedoManager( ui, enemyFleet, ship, damageControl )
    if ( tm.fire( quadrant ) ) {
      updateQuadrantAfterSkirmish tm.thingDestroyed
    }
  }

  @TypeChecked
  final void firePhasers() {
    PhaserManager pm = new PhaserManager( ui, enemyFleet, ship, damageControl)
    if ( pm.fire() ) {
      updateQuadrantAfterSkirmish()
    }
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
    new GameState( enemyFleet, ship, game ).continues()
  }

  @TypeChecked
  boolean gameWon() {
    new GameState( enemyFleet, ship, game ).won()
  }

  @TypeChecked
  boolean gameLost() {
     new GameState( enemyFleet, ship, game ).lost()
  }
}
