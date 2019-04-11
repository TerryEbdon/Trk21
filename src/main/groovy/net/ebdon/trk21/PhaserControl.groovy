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
  UiBase ui;
  FederationShip ship;  /// @todo Break the dependency on FederationShip
  Battle battle;

  PhaserControl( final DamageControl aDc, final UiBase reporter, aShip, aBattle ) {
    damageControl = aDc
    ui = reporter
    ship = aShip
    battle = aBattle
  }

  private void phasersDisabled() {
    log.info 'Phaser control is disabled.'
    ui.localMsg 'phaserControl.disabled'
  }

  private void phasersOnTarget() {
    log.info "Phaser control is enabled. Energy available $ship.energyNow"
    ui.fmtMsg 'phaserControl.onTarget', [ship.energyNow]
  }

  @SuppressWarnings('InsecureRandom')
  private def phaserVariance() {
    2 + new Random().nextFloat()
  }

  private float rangeTo( final Expando target ) {
    log.info "Fire from ${ship.position.sector} at $target"
    distanceBetween( ship.position.sector, target.sector )
  }

  private int targetDamageAmount( final int fired, final float distance ) {
    [fired, fired / distance * phaserVariance()].min()
  }

  private void fireAt( final int energyAmount, final Expando target ) {
    log.debug 'in fireAt()'
    assert ship.position.valid
    assert target.sector.valid
    assert energyAmount > 0

    final float distance = rangeTo( target )
    log.debug sprintf(
      'Calculating hit on %s with %d units, range %+1.3f sectors',
      target.name, energyAmount, distance )
    assert distance > 0
    final int energyHit = targetDamageAmount( energyAmount, distance )

    log.info '{} hit with {} of the {} units fired at it.',
        target.name, energyHit, energyAmount

    battle.hitOnFleetShip target, energyHit
  }

  void fire( final int energyAmount ) {
    log.info "Firing phasers with $energyAmount units."
    assert damageControl
    assert energyAmount in 1..ship.energyNow // Trek.firePhasers() should ensure this.

    if ( damageControl.isDamaged( DeviceType.phasers ) ) {
        phasersDisabled()
    } else {
      phasersOnTarget()
      ship.energyReducedByPhaserUse energyAmount
      Expando target
      while ( target = battle.getNextTarget() ) {
        fireAt energyAmount, target
      }
    }
    log.info 'Firing phasers -- complete'
  }
}
