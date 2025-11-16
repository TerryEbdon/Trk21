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

  PhaserControl( final DamageControl aDc, final UiBase reporter, FederationShip aShip, Battle aBattle ) {
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
  private float phaserVariance() {
    2f + new Random().nextFloat()
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

  /**
   * Fire phasers at the current battle targets.
   * <p>
   * <h4>Implementation notes</h4>
   * {@code AssignmentInConditional} suppressed due to CodeNarc false positive.
   * See <a href="https://github.com/TerryEbdon/Trk21/issues/158">Trk21 issue
   * &#35;158</a> and <a href="https://github.com/CodeNarc/CodeNarc/issues/447">
   * CodeNarc issue &#35;447</a>
   * <p>
   * <h4>Behaviour</h4>
   * <ul style="list-style-type:disc;">
   *   <li>&diams; If the phasers device is damaged, notifies the user and does
   *   not fire.
   *   <li>&diams; Otherwise notifies the user, reduces the ship's available
       energy by energyAmount, then iterates over battle.nextTarget calling
       fireAt(...) for each target.
   * </ul>
   * <h4>Side effects:</h4>
   * <ul>
   *   <li>&diams; Reduces ship energy by energyAmount.
   *   <li>&diams; May call Battle.hitOnFleetShip via fireAt().
   *   <li>&diams; Emits UI messages and log entries.
   * </ul>
   * @param energyAmount  number of energy units to fire; must be >0 and <=
   * ship.energyNow
   * @throws AssertionError if damageControl is null or energyAmount is out of
   * range
   */
  @SuppressWarnings('AssignmentInConditional')
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
      while ( target = battle.nextTarget ) {
        fireAt energyAmount, target
      }
    }
    log.info 'Firing phasers -- complete'
  }
}
