package net.ebdon.trk21;

import java.text.MessageFormat;
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager;
import groovy.transform.TypeChecked;

import static GameSpace.*;
import static Quadrant.*;
import static ShipDevice.*;
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
  // def ui;
  UiBase ui;
  PropertyResourceBundle rb;
  MessageFormat formatter;

  /// @todo Rename Trek.game, it's a misleading name for TrekCalendar
  TrekCalendar game = new TrekCalendar();

    static String logPositionPieces = 'Positioning game pieces {} in quadrant {}'

    Galaxy    galaxy    = new Galaxy();
    Quadrant  quadrant  = new Quadrant();

    Repositioner repositioner;
    DamageControl damageControl;

    EnemyFleet enemyFleet = new EnemyFleet();
    FederationShip ship;
    int numStarBasesTotal = 0; ///< b9% in TREK.BAS

  @TypeChecked
  int getEntQuadX() {
    ship.position.quadrant.row
  }

  @TypeChecked
  void setEntQuadX( final int newPos ) {
    ship.position.quadrant.row = newPos
  }

  @TypeChecked
  int getEntSectX() {
    ship.position.sector.row
  }

  @TypeChecked
  void setEntSectX( final int newPos ) {
    ship.position.sector.row = newPos
  }

  @TypeChecked
  int getEntQuadY() {
    ship.position.quadrant.col
  }

  @TypeChecked
  void setEntQuadY( final int newPos ) {
    ship.position.quadrant.col = newPos
  }

  @TypeChecked
  int getEntSectY() {
    ship.position.sector.col
  }

  @TypeChecked
  void setEntSectY( final int newPos ) {
    ship.position.sector.col = newPos
  }

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

  Trek( theUi = null ) {
    ui = theUi
    formatter = new MessageFormat('')
    formatter.locale = Locale.default

    ClasspathResourceManager resourceManager = new ClasspathResourceManager()
    InputStream inStream = resourceManager.getInputStream('Language.properties')

    if ( inStream ) {
      rb = new PropertyResourceBundle( inStream )
      repositioner = new Repositioner( this )
      damageControl = new DamageControl()
    } else {
        log.fatal 'Could not load Language.poperties'
        assert inStream
    }
  }

  @TypeChecked
  void setupGalaxy() {
    setEntStartPosition()
    distributeKlingons()
    dumpGalaxy()
    setupQuadrant()
  }

  void setEntStartPosition() {
    ship.position.quadrant = new Coords2d( *(galaxy.randomCoords) )
  }

  @TypeChecked
  void setupQuadrant() {
    enemyFleet.numKlingonBatCrInQuad = 0
    quadrant.clear()
    positionShipInQuadrant()
    positionGamePieces()
  }

  /// The ship has a arrived in a new quadrant, and already knows this.
  /// Now it needs to be allocated a sector in it's new quadrant.
  /// @todo A random sector is allocated. Should the sector instead be
  /// based on the staring sector and course vector?
  /// @todo Quadrant.randomCoords() should return a Coords2d, not a list. This
  /// would remove the need for the spread operator, and allow the
  /// positionShipInQuadrant() method to be type-checked.
  void positionShipInQuadrant() {
    log.info 'Position ship within quadrant {}', ship.position.quadrant
    assert ship.position.quadrant.valid
    final Coords2d shipSector = new Coords2d( *(quadrant.randomCoords) )
    ship.position.sector = shipSector
    quadrant[shipSector] = Thing.ship
    log.debug 'Ship positioned at sector {}', shipSector
    assert quadrant.valid
    quadrant.dump()
  }

  @TypeChecked
  private void updateNumEnemyShipsInQuad() {
    final Coords2d shipQuadrant = ship.position.quadrant
    final QuadrantValue qv = new QuadrantValue( galaxy[shipQuadrant] )
    galaxy[shipQuadrant] -= 100 * qv.enemy
    galaxy[shipQuadrant] += 100 * enemyFleet.numKlingonBatCrInQuad
  }

  @TypeChecked
  @Newify(QuadrantValue)
  void positionGamePieces() {
    log.trace( logPositionPieces,
        galaxy.scan(ship.position.quadrant), ship.position.quadrant)

    final QuadrantValue qVal = QuadrantValue( galaxy[ship.position.quadrant] )
    QuadrantSetup quadrantSetup = new QuadrantSetup( quadrant, enemyFleet )
    quadrantSetup.positionEnemy qVal.enemy
    quadrantSetup.positionBases qVal.bases
    quadrantSetup.positionStars qVal.stars
  }

  @TypeChecked
  private int numberOfStarsToBirth() { // fnr%()
    new Random().nextInt(8) + 1
  }

  void distributeKlingons() {
    int totalStars = 0
    int starsInQuad = 0
    // int numBasesInQuad = 0 //b3%

    log.info 'Distributing Klingon battle cruisers.'
    minCoord.upto(maxCoord) { i ->
      minCoord.upto(maxCoord) { j ->
        enemyFleet.numKlingonBatCrInQuad = 0 // k3%
        // int b9 = 0 //b9%
        final float c1 = new java.util.Random().nextFloat() * 64

        1.upto( enemyFleet.maxKlingonBCinQuad ) {
          if ( c1 < enemyFleet.softProbs[ it ] ) {
            ++enemyFleet.numKlingonBatCrTotal   // line 1200
            ++enemyFleet.numKlingonBatCrRemain  // line 1180
            ++enemyFleet.numKlingonBatCrInQuad  // line 1180
            log.trace "Enemy craft added to quadrant ${i} - ${j}, " +
                      "there's now ${enemyFleet.numKlingonBatCrInQuad} in this quadrant."
          }
        }

        final int numBasesInQuad = new Random().nextFloat() > 0.9 ? 1 : 0 // b3%
        numStarBasesTotal += numBasesInQuad // 1210 B9%=B9%+B3%
        starsInQuad = numberOfStarsToBirth()
        totalStars += starsInQuad
        galaxy[i,j] =
          enemyFleet.numKlingonBatCrInQuad * 100 +
          numBasesInQuad * 10 +
          starsInQuad //G%(I%,J%)=K3%*100%+B3%*10%+FNR%
        log.trace 'galaxy[{},{}] = {}', i, j, galaxy.scan(i,j)
      }
    }
    log.info "Total number of Klingon BC: ${enemyFleet.numKlingonBatCrRemain.toString().padLeft(3)}"
    log.info "Total number of star bases: ${numStarBasesTotal.toString().padLeft(3)}"
    log.info "Total number stars:         ${totalStars.toString().padLeft(3)}"
    log.info enemyFleet.toString()
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
      ship = new FederationShip();
      setupGalaxy()
      // shortRangeScan()
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

  void msgBox( final String msg, boolean logIt = true ) {
    ui.outln msg
    if ( logIt ) {
      log.info msg
    }
  }

  @TypeChecked
  void reportDamage() {
    damageControl.report( rb.&getString, ui.&localMsg )
  }

  @TypeChecked
  void attackReporter( final int damageAmount, final String message ) {
    log.info "Ship under attack, $damageAmount units of damage sustained."
    ui.outln message
    //:E% -= damageAmount // Line 2410
  }

  @TypeChecked
  void klingonAttack() {
    if ( !ship.isProtectedByStarBase() ) {
      enemyFleet.attack( ship.position.sector, this.&attackReporter )
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
          log.trace "Ship has moved, but where is it?"
          // Continue from line 1840...

          repositioner.repositionShip vector
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

  final private void repopulateSector( final oldQuadrant ) {
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

  @TypeChecked
  void blockedAtSector( final int row, final int column ) {
    ui.localMsg 'blockedAtSector',
      [ quadrant[row,column], column, row ] as Object[]
  }

  @TypeChecked
  String logFmtCoords( final int x, final int y ) {
    "${[x,y]} == $y - $x"
  }

  /// @todo Reverse the coordinates? i.e. i,j or j,i?
  /// @deprecated
  @TypeChecked
  boolean sectorIsOccupied( final int i, final int j ) {
    quadrant.isOccupied(i,j)
  }

  float getCourse() {
    ui.getFloatInput( rb.getString( 'input.course' ) ) // C1
  }

  /// @todo Test needed for getShipCourse()
  Tuple2<Boolean,ShipVector> getShipCourse() {
    boolean rejected = false
    float course        = 0
    float warpFactor    = 0
    ShipVector sv = new ShipVector()

    log.trace '''Getting ship's course'''

    course = getCourse()
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

  void showCondition() {
    ui.conditionText = displayableCondition()
  }

  /// @return A localised display string for the ship's condition
  /// @todo Move displayableCondition() into FederationShip
  /// @todo Localise via the #rb resource bundle.
  /// @bug  Code assumes that all condition values, other than "DOCKED",
  ///       are also valid HTML colours. This is currently true for ENGLISH
  ///       locales, but will fail for other languages. Colours and language
  ///       should be orthogonal.
  /// @todo Consider splitting into two methods, to allow use where HTML is
  ///       not appropriate.
  String displayableCondition() {
    /// @bug Should use config insteaf of HTML fonts.
    /// @todo This is incompatible with a CLI based UI.
    def colour = ship.condition != 'DOCKED' ? ship.condition : 'GREEN'
    "<html><font size=+2 color=$colour>${ship.condition}</font></html>"
  }

  /// Perform a @ref TrekShortRangeSensors "short-range sensor scan"
  void shortRangeScan() {
    logException {
      ship.shortRangeScan( galaxy )
      ship.attemptDocking( quadrant )
      // @todo Is this methode code-complete?
      // GOSUB 2370 UNLESS A%
      // 1570 IF D%(2%) THEN &"SHORT RANGE SENSORS ARE INOPERABLE":GOTO1650

      ui.localMsg 'sensors.shortRange.header'
      quadrant.displayOn ui.&outln
      ui.localMsg 'sensors.shortRange.divider'

      showCondition()
      ship.position.sector.with {
        ui.fmtMsg 'sensors.shipStatus.1', [ game.currentSolarYear, ship.condition ]
        ui.fmtMsg 'sensors.shipStatus.2', [ currentQuadrant(), col, row ]
        ui.fmtMsg 'sensors.shipStatus.3', [ ship.energyNow, ship.numTorpedoes ]
        ui.fmtMsg 'sensors.shipStatus.4', [ enemyFleet.numKlingonBatCrRemain ]
      }
      log.info game.toString()
    }
  }

  @TypeChecked
  private void attackFleetWithPhasers( final int energy ) {
    new Battle(
      enemyFleet, ship, damageControl,
      ui.&outln, this.&attackReporter, rb
    ).phaserAttackFleet( energy )
  }

  void updateQuadrantAfterSkirmish() {
    updateNumEnemyShipsInQuad()
    new QuadrantSetup( quadrant, enemyFleet ).updateAfterSkirmish()
  }

  final void fireTorpedo() {
    log.info "Fire torpedo - available: ${ship.numTorpedoes}"

    float course = getCourse()
    if ( course ) {
      new Battle(
        enemyFleet, ship, damageControl,
        ui.&outln, this.&attackReporter, rb
      ).fireTorpedo( course )
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
