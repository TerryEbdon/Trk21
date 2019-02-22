package net.ebdon.trk21;

import java.text.MessageFormat;
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager;

import static GameSpace.*;
import static Quadrant.*;
import static ShipDevice.*;
/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
 * @copyright
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
  UiBase ui;
  PropertyResourceBundle rb;
  MessageFormat formatter;

  /// @todo Rename Trek.game, it's a misleading name for TrekCalendar
  TrekCalendar game = new TrekCalendar();

    Galaxy    galaxy    = new Galaxy();
    Quadrant  quadrant  = new Quadrant();

    Repositioner repositioner;
    DamageControl damageControl;

    EnemyFleet enemyFleet = new EnemyFleet();
    FederationShip ship;
    int numStarBasesTotal = 0; ///< b9% in TREK.BAS

  int getEntQuadX() {
    ship.position.quadrant.row
  }

  def setEntQuadX( newPos ) {
    ship.position.quadrant.row = newPos
  }

  int getEntSectX() {
    ship.position.sector.row
  }

  def setEntSectX( newPos ) {
    ship.position.sector.row = newPos
  }

  int getEntQuadY() {
    ship.position.quadrant.col
  }

  def setEntQuadY( newPos ) {
    ship.position.quadrant.col = newPos
  }

  int getEntSectY() {
    ship.position.sector.col
  }

  def setEntSectY( newPos ) {
    ship.position.sector.col = newPos
  }

  def damage = [
    1: new ShipDevice('device.WARP.ENGINES'), 2: new ShipDevice('device.S.R..SENSORS'),
    3: new ShipDevice('device.L.R..SENSORS'), 4: new ShipDevice('device.PHASER.CNTRL'),
    5: new ShipDevice('device.PHOTON.TUBES'), 6: new ShipDevice('device.DAMAGE.CNTRL')
  ]; ///< D%[] and D$[] in TREK.BAS.
     ///< @todo Move damage[] into FederationShip.
     ///< @note elements [n][0] are keys to the Language resource bundle, via #rb.

  boolean isValid() {
    ship != null && ship.isValid() &&
      // position != null && position.isValid() &&
      game != null && game.isValid() &&
      enemyFleet != null && enemyFleet.isValid()
  }

  String toString() {
    "Calendar   : $game\n" +
    "Ship       : $ship\n" +
    "EnemyFleet : $enemyFleet\n" +
    "Quadrant   : [$entQuadX, $entQuadY]\n" +
    "Sector     : [$entSectX, $entSectY]"
  }

  Trek( theUi = null ) {
    ui = theUi
    formatter = new MessageFormat("");
    formatter.setLocale( Locale.getDefault() );

    ClasspathResourceManager resourceManager = new ClasspathResourceManager()
    InputStream is = resourceManager.getInputStream('Language.properties')

    if ( is ) {
      rb = new PropertyResourceBundle( is )
      repositioner = new Repositioner( this )
      damageControl = new DamageControl( damage )
    } else {
        log.fatal "Can't load Language.poperties"
        system.exit(1)
    }
  }

  def setupGalaxy() {
    setEntStartPosition()
    distributeKlingons()
    dumpGalaxy()
    setupQuadrant()
  }

  def setEntStartPosition() {
    ship.position.quadrant = new Coords2d( *(galaxy.randomCoords) )
  }

  def setupQuadrant() {
    enemyFleet.numKlingonBatCrInQuad = 0
    quadrant.clear()
    positionShipInQuadrant()
    positionKlingons()
  }

  def positionShipInQuadrant() {
    log.info "Position ship within quadrant ${ship.position.quadrant}"
    assert ship.position.quadrant.isValid()
    ship.position.sector = new Coords2d( *(quadrant.randomCoords) )
    quadrant[ship.position.sector] = Thing.ship
    log.debug "Ship positioned at sector ${ship.position.sector}"
    assert quadrant.isValid()
    quadrant.dump()
  }

  def positionKlingons() {
    final int x                   = galaxy[entQuadX,entQuadY]
    enemyFleet.numKlingonBatCrInQuad   = x / 100
    final int numBasesInQuad      = x / 10 - 10 * enemyFleet.numKlingonBatCrInQuad
    final int numStarsInQuad      = x - enemyFleet.numKlingonBatCrInQuad * 100 - numBasesInQuad * 10

    log.info "x: ${x}"
    log.info "numKlingonBatCrInQuad: ${enemyFleet.numKlingonBatCrInQuad}"
    log.info "numBasesInQuad: ${numBasesInQuad}"
    log.info "numStarsInQuad: ${numStarsInQuad}"

    positionEnemy()
    positionBases numBasesInQuad
    positionStars numStarsInQuad
  }

  def positionEnemy() {
    log.info 'positionEnemy()'
    assert quadrant.isValid()
    enemyFleet.resetQuadrant()
    log.info "Positioning $enemyFleet.numKlingonBatCrInQuad Klingons in quadrant ${currentQuadrant()}."
    for ( int klingonShipNo = 1; klingonShipNo <= enemyFleet.numKlingonBatCrInQuad; ++klingonShipNo ) {
      def klingonPosition = getEmptySector()
      quadrant[klingonPosition] = Thing.enemy

      enemyFleet.positionInSector klingonShipNo, klingonPosition
    }

    log.info 'v'*16
    log.info "quad with Klingons"
    quadrant.displayOn( {log.info it} )
    log.info "quad with Klingons"
    log.info '^'*16
  }

  def positionStars( numStarsInQuad ) {
    log.info "Positioning $numStarsInQuad stars."
    for ( int star = 1; star <= numStarsInQuad; ++star ) {
      def starPos = getEmptySector()
      log.trace "... star $star is at sector ${starPos}"
      quadrant[starPos[0],starPos[1]] = Thing.star
      log.trace "V*: ${quadrant[starPos[0],starPos[1]]}"
    }
  }

  def positionBases( numBasesInQuad ) {
    log.info "Positioning $numBasesInQuad bases."
    for ( int base = 1; base <= numBasesInQuad; ++base ) {
      def basePos = getEmptySector()
      log.trace "... base $base is at sector ${basePos}"
      quadrant[basePos] = Thing.base
    }

  }

  /// @deprecated Acts as a facade for the Quadrant
  def getEmptySector() {
    quadrant.getEmptySector()
  }

  int rand1to9() { // fnr%() -- Used a lot, as there are most 9 bases / Klingons / stars in each quadrant.
     new java.util.Random().nextFloat() * 8 + 1
  }

  def distributeKlingons() {
    int totalStars = 0
    int starsInQuad = 0
    // int numBasesInQuad = 0 //b3%

    log.info "Distributing Klingon battle cruisers."
    minCoord.upto(maxCoord) { i->
      minCoord.upto(maxCoord) { j->
        enemyFleet.numKlingonBatCrInQuad = 0 // k3%
        // int b9 = 0 //b9%
        def c1 = new java.util.Random().nextFloat()*64 //rnd * 64

        1.upto( enemyFleet.maxKlingonBCinQuad ) {
          if ( c1 < enemyFleet.softProbs[ it ] ) {
            ++enemyFleet.numKlingonBatCrTotal   // line 1200
            ++enemyFleet.numKlingonBatCrRemain  // line 1180
            ++enemyFleet.numKlingonBatCrInQuad  // line 1180
            log.trace "Enemy craft added to quadrant ${i} - ${j}, " +
                      "there's now ${enemyFleet.numKlingonBatCrInQuad} in this quadrant."
          }
        }

        final int numBasesInQuad = new java.util.Random().nextFloat() > 0.9 ? 1 : 0 // b3%
        numStarBasesTotal += numBasesInQuad // 1210 B9%=B9%+B3%
        starsInQuad = rand1to9()
        totalStars += starsInQuad
        galaxy[i,j] =
          enemyFleet.numKlingonBatCrInQuad * 100 +
          numBasesInQuad * 10 +
          starsInQuad //G%(I%,J%)=K3%*100%+B3%*10%+FNR%
        log.trace sprintf('galaxy[%d,%d] = %03d', i, j, galaxy[i,j] )
      }
    }
    log.info "Total number of Klingon BC: ${enemyFleet.numKlingonBatCrRemain.toString().padLeft(3)}"
    log.info "Total number of star bases: ${numStarBasesTotal.toString().padLeft(3)}"
    log.info "Total number stars:         ${totalStars.toString().padLeft(3)}"
    log.info enemyFleet.toString()
  }

  def dumpGalaxy() {
    galaxy.dump()
    reportEntPosition()
  }

  def reportEntPosition() {
    log.info "Enterprise is in quadrant ${currentQuadrant()}"
  }

  String currentQuadrant() {
    "${entQuadY} - ${entQuadX}"
  }

  void setupGame() {
    assert damage

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

  void startGame() {
    shortRangeScan()
  }

  /// @deprecated Not needed with new font config.
  String btnText( final String text ) {
    // "<html><font size=+3>$text</font></html>"
    text
  }

  def msgBox( msg, boolean logIt = true ) {
    ui.outln "$msg"
    if ( logIt ) {
      log.info msg
    }
  }

  /// @deprecated Switch the code to use the UI version.
  Float getFloatInput( final String prompt ) {
    ui. getFloatInput( prompt )
  }

  def reportDamage() {
    damageControl.report( rb, this.&msgBox, formatter )
  }

  def damageRepair() {
    log.info "Repairing damage"
    damageControl.repair( this.&msgBox )
  }

  def attackReporter( damageAmount, message ) {
    log.info "Ship under attack, $damageAmount units of damage sustained."
    msgBox message
    //:E% -= damageAmount // Line 2410
  }

  ///@ todo: Localise klingonAttack() messages.
  def klingonAttack() {
    if ( !ship.isProtectedByStarBase() ) {
      // def attackReporter = { damageAmount, message ->
      //   log.info "Ship under attack, $damageAmount units of damage sustained."
      //   msgBox message
      //   //:E% -= damageAmount // Line 2410
      // }
      enemyFleet.attack( [entSectX, entSectY], this.&attackReporter )
    } else {
      msgBox "Star Base shields protect Enterprise";
    }
  }

  ///@ todo: Localise spaceStorm() messages.
  def spaceStorm() {
    final int systemToDamage      = new Random().nextInt( damage.size() ) + 1
    final int damageAmount        = new Random().nextInt(5) + 1
    damage[systemToDamage].state -= damageAmount
    log.info "Space storm has damaged device No. $systemToDamage"
    log.info "   damage of $damageAmount units"
    log.info "   new status: ${damage[systemToDamage].state} units"
    msgBox( "*** Space Storm, ${damage[systemToDamage].name} damaged ***")
  }

  // def randomlyRepairDamage( final deviceKey ) {
  //   assert deviceKey
  //   damage[devicekey].state -=
  //     Math.floor( new Random().nextFloat() *
  //     damage[deviceKey].state -1 )
  //       // inflict damage on device -- see line 1810
  // }

  void deviceStatusLottery() {
    assert damage
    log.debug 'deviceStatusLottery()'
    if ( new Random().nextFloat() <= 0.5 ) { // 1760
      spaceStorm()
    } else { // 1790 - Not a space storm
      log.info "MORE TO DO... see lines 1790 onwards..."
      /// @todo MORE TO DO... see lines 1790 onwards...

      // def dc = new DamageControl( damage )
      final def firstDamagedDeviceKey = damageControl.findDamagedDeviceKey()
      if ( firstDamagedDeviceKey ) {
          damageControl.randomlyRepair( firstDamagedDeviceKey )
        // randomlyRepairDamage( firstDamagedDevice.key )
        final def damagedDeviceId = damage[firstDamagedDeviceKey].id

        Object[] msgArgs = [ "device.DAMAGE.$damagedDeviceId" ]
        formatter.applyPattern( rb.getString( 'truce' ) );
        msgBox formatter.format( msgArgs );
        // msgBox( "*** TRUCE ${damagedDeviceName} state of repair improved ***")
      }
    }
  }

  void enemyAttacksBeforeShipCanMove() {
    if ( enemyFleet.canAttack() ) {
      log.info 'Klingons attack before the ship can move away.'
      klingonAttack()
    }
  }

  /// @todo Localise setCourse()
  def setCourse() {
    ShipVector vector = getCourse()
    if ( vector && vector.isValid() ) {
      log.info "Got a good vector: $vector"

      if ( vector.warpFactor > 0.2 && damage[1].isDamaged() ) {
        msgBox( "Warp engines are damaged.\nMaximum speed is .2")
      } else {
        enemyAttacksBeforeShipCanMove()
        damageRepair() /// @todo Is damageRepair() called at the correct point?

        if ( new Random().nextFloat() <= 0.20 ) { // 1750
          deviceStatusLottery()
        }
        game.tick() // Line 1830
        final Coords2d oldQuadrant = ship.position.quadrant.clone()
        ship.move( vector ) // set course, start engines...

        if ( ship.deadInSpace() || game.outOfTime() ) {
          shipDestroyed() /// @todo How do I "end" the game?
        } else {
          log.trace "Ship has moved, but where is it?"
          // Continue from line 1840...

          repositioner.repositionShip vector
          repopulateSector oldQuadrant
          shortRangeScan()
        }
      }
    } else {
      log.info "vector is not so good: $vector"
      if (vector.course * vector.warpFactor != 0 ) { // User didn't hit cancel
        msgBox "That's not a valid course / warp factor. Command refused."
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

  /// @todo localise blockedAtSector( row, column )
  void blockedAtSector( row, column ) {
    msgBox "Ship blocked by ${quadrant[row,column]} at sector ${logFmtCoords( row, column )}"
  }

  def logFmtCoords( x, y ) {
    "${[x,y]} == $y - $x"
  }
  /// @todo Reverse the coordinates? i.e. i,j or j,i?
  /// @deprecated
  boolean sectorIsOccupied( i, j ) {
    quadrant.isOccupied(i,j)
  }

  /// @todo Localise shipDestroyed()
  void shipDestroyed() {
    msgBox "It is stardate ${game.currentSolarYear}\n"
    msgBox "Your ship has been destroyed."
    msgBox "Your civilisation will be conquered."
    msgBox "There are still ${enemyFleet.numKlingonBatCrRemain} enemy ships."
    msgBox "You are dead."
  }

  /// @todo Test needed for getCourse()
  ShipVector getCourse() {
    float course        = 0
    float warpFactor    = 0
    ShipVector sv = new ShipVector()

    log.trace 'Getting course'

    course = getFloatInput( rb.getString( 'input.course' ) ) // C1
    if ( ShipVector.isValidCourse( course ) ) {
      sv.course = course
      warpFactor = getFloatInput( rb.getString( 'input.speed' ) ) // W1
      if ( ShipVector.isValidWarpFactor( warpFactor ) ) {
        sv.warpFactor = warpFactor
      } else {
        log.info "Warp factor $warpFactor is outside of expected range."
      }
    } else {
      log.info "Course value $course is outside of expected range."
    }
    sv
  }

  /// Perform a @ref TrekLongRangeSensors "long-range sensor scan"
  def longRangeScan() {
    if ( damage[3].isDamaged() ) {
      msgBox rb.getString( 'sensors.longRange.offline' )
    } else {
      Object[] msgArgs = [ currentQuadrant() ]
      formatter.applyPattern( rb.getString( 'sensors.longRange.scanQuadrant') );
      msgBox formatter.format( msgArgs );

      // msgBox "Long range sensor scan for quadrant ${currentQuadrant()}\n"
      ( entQuadX - 1 ).upto( entQuadX + 1 ) { i ->    // q1% -1 to q1% + 1
        String lrStatusLine = ''
        ( entQuadY - 1 ).upto( entQuadY + 1 ) { j ->  // q2% -1 to q2% + 1
          lrStatusLine += '  '
          if ( !insideGalaxy( i, j ) ) {
            lrStatusLine += '000'
          } else {
            lrStatusLine += quadrantScan( i, j )
          }
        }
        msgBox lrStatusLine
      }
    }
  }

  String quadrantScan( x, y ) {
    galaxy[ x, y ].toString().padLeft(3,'0')
  }

  def showCondition() {
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
  def shortRangeScan() {
    logException {
      ship.shortRangeScan( galaxy )
      ship.attemptDocking( quadrant )
      // @todo Is this methode code-complete?
      // GOSUB 2370 UNLESS A%
      // 1570 IF D%(2%) THEN &"SHORT RANGE SENSORS ARE INOPERABLE":GOTO1650

      msgBox( '---------------', false )
      quadrant.displayOn( {msgBox it} )

      msgBox( '---------------', false )
      showCondition()
      msgBox( sprintf("%8s: %5d  %15s: %s", rb.getString('starDate'), game.currentSolarYear, rb.getString('condition'),ship.condition ) )
      msgBox( sprintf('%8s: %5s  %15s: %d - %d', rb.getString('quadrant'),currentQuadrant(), rb.getString('sector'),entSectY, entSectX ) )
      msgBox( sprintf('%8s: %5d  %15s: %2d', rb.getString('energy'),ship.energyNow, rb.getString('missiles'),ship.numTorpedoes ) )
      msgBox( sprintf('%8s: %5d', rb.getString('enemy'), enemyFleet.numKlingonBatCrRemain ) )

      log.info game.toString()
    }
  }

  final void firePhasers() {
    log.info 'Fire phasers'

    int energy = getFloatInput( rb.getString( 'input.phaserEnergy' ) )
    if ( energy > 0 ) {
      if ( energy <= ship.energyNow ) {
        new Battle( enemyFleet, ship, damageControl, this.&msgBox, this.&attackReporter ).
            phaserAttackFleet( energy )
      } else {
        log.info  "Phaser command refused; user tried to fire too many units."
        msgBox    rb.getString('phaser.refused.badEnergy')
      }
    } else {
      log.info "Command cancelled by user."
    }
   }
}
