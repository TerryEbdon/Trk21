package net.ebdon.trk21;

import static ShipDevice.*;
import static GameSpace.*;
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

@groovy.util.logging.Log4j2
final class PhaserControl {

  final DamageControl damageControl;
  Closure report;
  FederationShip ship;  /// @todo Break the dependency on FederationShip
  Battle battle;

  PhaserControl( final DamageControl aDc, final Closure reporter, aShip, aBattle ) {
    damageControl = aDc
    report = reporter
    ship = aShip
    battle = aBattle
  }

  private void phasersDisabled() {
    log.info "Phaser control is disabled."
    report "Phaser control is disabled."
  }

  private void phasersOnTarget() {
    log.info "Phaser control is enabled. Energy available $ship.energyNow"
    report "Phasers locked in on target. Energy available $ship.energyNow"
  }

  private void commandRefused() {
    log.warn "Command refused; insufficient energy available."
    report   "Command refused; insufficient energy available."
  }

  private def phaserVariance() {
    2 + new Random().nextFloat()
  }

  private float rangeTo( target ) {
    log.info "Fire from ${ship.position.sector} at $target"
    distanceBetween( ship.position.sector, target.sector )
  }

  private void fireAt( energyAmount, target ) {
    log.debug 'in fireAt()'
    assert ship.position.isValid()
    assert target.sector.isValid()
    assert energyAmount > 0

    final float distance = rangeTo( target )
    log.debug sprintf(
      "Calculating hit on %s with %d units, range %+1.6f sectors",
      target.name, energyAmount, distance )
    assert distance > 0
    final float energyHit = energyAmount / distance * phaserVariance()

    log.info sprintf( "Target %s hit with %1.6f of the %d units fired at it.",
        target, energyHit, energyAmount )

    log.error "** Code incomplete **" /// @todo More phaser stuff here.

    // K%( I%,3% ) = K%( I%,3% ) - H%  !!! Decrement the target Klingon's energy.
  }

  void fire( energyAmount ) {
    log.info "Firing phasers..."
    log.info "Firing phasers with $energyAmount units."
    // assert ship
    assert damageControl && report
    assert energyAmount > 0

    if ( damageControl.isDamaged( DeviceType.phasers ) ) {
        phasersDisabled()
    } else {
      phasersOnTarget()
      if (energyAmount > ship.energyNow) {
        commandRefused()
      } else {
        log.debug "Subtracting $energyAmount from ship's energy of ${ship.energyNow}"
        ship.energyNow -= energyAmount
        log.debug "Ship energy reduced to ${ship.energyNow}"
        def target
        while ( target = battle.getNextTarget() ) {
          fireAt energyAmount, target //klingon
        }
        log.info "*** Last *** target: $target"
        // def klingons = []
        // klingons.each { klingon ->
        //   if ( !klingon.dead() ) {
            // fireAt energyAmount, null //klingon
        //   }
        // }
      }
    }
    log.info 'Firing phasers -- complete'
  }
}