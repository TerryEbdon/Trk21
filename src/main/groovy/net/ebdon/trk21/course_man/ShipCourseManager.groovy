package net.ebdon.trk21.course_man;

import static net.ebdon.trk21.GameSpace.*;
import static net.ebdon.trk21.Quadrant.*;
import static net.ebdon.trk21.ShipDevice.DeviceType;
import net.ebdon.trk21.ShipDevice.*;
import net.ebdon.trk21.ShipVector;
import net.ebdon.trk21.DamageControl;
import net.ebdon.trk21.EnemyFleet;
import net.ebdon.trk21.ShipRepositioner;
import net.ebdon.trk21.Coords2d;
import net.ebdon.trk21.FederationShip;
import net.ebdon.trk21.UiBase;
import net.ebdon.trk21.DeviceStatusLottery;
import net.ebdon.trk21.GameState;

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

@groovy.util.logging.Log4j2
@groovy.transform.TypeChecked
final class ShipCourseManager {
  private final UiBase ui;
  private final DamageControl damageControl;
  private final FederationShip ship;
  private final EnemyFleet enemyFleet;
  private final GameState gameState;

  ShipCourseManager( UiBase uib, DamageControl dc, FederationShip fs, EnemyFleet ef, GameState gs ) {
    ui            = uib
    damageControl = dc
    ship          = fs
    enemyFleet    = ef
    gameState     = gs
  }

  private boolean tooFastForDamagedEngine( final ShipVector sv ) {
    log.trace "tooFastForDamagedEngine called with $sv"
    sv.warpFactor > 0.2F && damageControl.isDamaged( DeviceType.engine )
  }

  /// @todo Test needed for getShipCourse()
  private Tuple2<Boolean,ShipVector> getShipCourse() {
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
    new Tuple2(rejected,sv)
  }

  private void enemyAttacksBeforeShipCanMove() {
    if ( enemyFleet.canAttack() ) {
      log.info 'Klingons attack before the ship can move away.'
      klingonAttack()
    }
  }

  private void klingonAttack() {
    if ( !ship.protectedByStarBase ) {
      enemyFleet.attack ship.position.sector, ui.&fmtMsg
    } else {
      ui.localMsg 'starbase.shields'
    }
  }

  @SuppressWarnings('InsecureRandom')
  boolean setOffFrom( final net.ebdon.trk21.Quadrant quadrant ) {
    final Tuple2<Boolean, ShipVector> rv = getShipCourse()
    final ShipVector vector = rv.second
    final boolean rejected = rv.first
    boolean shipMoved = false

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
        gameState.tick() // Line 1830
        // final Coords2d oldQuadrant = ship.position.quadrant.clone()
        ship.move( vector ) // set course, start engines...

        if ( gameState.continues() ) {
          log.trace 'Ship has moved.'
          shipMoved = true
          // Continue from line 1840...

          ShipRepositioner rp = new ShipRepositioner(
            ship:     ship,
            ui:       ui,
            quadrant: quadrant
          )

          rp.repositionShip vector
        }
      }
    } else {
      log.info "vector is not so good: $vector"
      if ( rejected ) { // User didn't hit cancel
        ui.localMsg 'input.vector.bad'
      }
    }
    shipMoved
  }
}
