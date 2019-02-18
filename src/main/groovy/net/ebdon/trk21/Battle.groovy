package net.ebdon.trk21;

import groovy.transform.*;
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
@ToString(includePackage=false,includeNames=true)
@Canonical
@groovy.util.logging.Log4j2
final class Battle {
  EnemyFleet      enemyFleet;
  FederationShip  ship;
  DamageControl   dc;
  Closure         pcReporter;
  Closure         fleetAttackReporter;

  private int nextTargetIndex = 1;

  def getNextTarget() {
    log.info "There are ${enemyFleet.numKlingonBatCrInQuad} enemy ships here."
    log.info "Getting target $nextTargetIndex"
    assert nextTargetIndex >= 0
    assert nextTargetIndex <= 1 + enemyFleet.maxKlingonBCinQuad
    def rv = null
    if ( enemyFleet.numKlingonBatCrInQuad > 0 ) {
      if ( nextTargetIndex <= enemyFleet.maxKlingonBCinQuad ) {
        final def enemyShip = enemyFleet.klingons[ nextTargetIndex ]
        if ( enemyShip[3] > 0 ) {
          log.info "Creating target expando from $enemyShip"
          rv = new Expando(
            name: "Enemy ship No. $nextTargetIndex",
            energy: enemyShip[3],
            sector: new Coords2d( *enemyShip[1..2] )  /// @ todo fix this.
          )
        } else {
          log.info "Enemy ship No. $nextTargetIndex is dead, or never existed... recursing..."
          ++nextTargetIndex
          rv = getNextTarget()
        }
        ++nextTargetIndex
      }
    } else {
      log.info "Nothing to fire at; no enemy ships in this quadrant."
    }
    rv
  }

  def phaserAttackFleet( energy ) {
    assert energy > 0
    assert enemyFleet && ship && dc && pcReporter && fleetAttackReporter
    new PhaserControl( dc, pcReporter, ship, this ).fire( energy )
    enemyFleet.with {
      if ( canAttack() ) {
        attack( ship.position.sector, fleetAttackReporter )
      }
    }
  }
}
