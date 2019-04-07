package net.ebdon.trk21;

import groovy.transform.*;
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
@ToString(includePackage=false,includeNames=true)
@Canonical
@groovy.util.logging.Log4j2
final class Battle {
  EnemyFleet      enemyFleet;
  FederationShip  ship;
  DamageControl   dc;
  UiBase          ui;

  private int nextTargetIndex = 1;

  Expando getNextTarget() {
    log.trace "There are ${enemyFleet.numKlingonBatCrInQuad} enemy ships here."
    log.trace "Getting target $nextTargetIndex"
    assert nextTargetIndex >= 0
    assert nextTargetIndex <= 1 + enemyFleet.maxKlingonBCinQuad
    Expando rv = null
    if ( enemyFleet.numKlingonBatCrInQuad > 0 ) {
      if ( nextTargetIndex <= enemyFleet.maxKlingonBCinQuad ) {
        final int[] enemyShip = enemyFleet.klingons[ nextTargetIndex ]
        if ( enemyShip[3] > 0 ) {
          log.info "Creating target expando from $enemyShip"
          rv = new Expando(
            id:     nextTargetIndex,
            name:   "Enemy ship No. $nextTargetIndex",
            energy: enemyShip[3],
            sector: new Coords2d( *enemyShip[1..2] )  /// @ todo fix this.
          )
        } else {
          log.trace "Enemy ship No. $nextTargetIndex is dead, or never existed... recursing..."
          ++nextTargetIndex
          rv = getNextTarget()
        }
        ++nextTargetIndex
      }
    } else {
      log.info 'Nothing to fire at; no enemy ships in this quadrant.'
    }
    rv
  }

  void hitOnFleetShip( final Expando target, final int hitAmount = EnemyFleet.maxKlingonEnergy ) {
    ui.fmtMsg 'battle.hitOntargetAt',
      [hitAmount, target.name, target.sector.row, target.sector.col]

    enemyFleet.with {
      hitOnShip( target.id, hitAmount )
      if (shipExists(target.id)) {
        ui.fmtMsg 'battle.targetEnergyLeft', [energy(target.id)]
      } else {
        ui.localMsg 'battle.enemy.destroyed'
      }
    }
  }

  void phaserAttackFleet( final int energy ) {
    assert energy > 0
    assert enemyFleet && ship && dc // && fleetAttackReporter
    new PhaserControl( dc, ui, ship, this ).fire( energy )
    enemyRespondsToAttack()
  }

  @TypeChecked
  void enemyRespondsToAttack() {
    enemyFleet.regroup()
    if ( enemyFleet.canAttack() ) {
      if ( ship.isProtectedByStarBase() ) {
        ui.localMsg 'battle.shieldedByBase'
      } else {
        enemyFleet.attack( ship.position.sector, ui.&fmtMsg )
      }
    }
  }

  @TypeChecked
  void fireTorpedo( final float course, Quadrant quadrant ) {
    Torpedo torpedo = ship.torpedo
    quadrant[ torpedo.position.sector ] = Quadrant.Thing.torpedo
    Repositioner rp = new TorpedoRepositioner(
      ship:     torpedo,
      ui:       ui,
      quadrant: quadrant
    )

    rp.repositionShip new ShipVector( course: course )
    quadrant[ ship.position.sector ] = Quadrant.Thing.ship

    if ( rp.thingHit == Quadrant.Thing.enemy ) {
      enemyFleet.with {
        shipHitByTorpedo torpedo.position.sector
        // pcReporter rb.getString( 'battle.enemy.destroyed' )
        ui.localMsg 'battle.enemy.destroyed'
        // quadrant[ torpedo.position.sector ] = Quadrant.Thing.emptySpace
        quadrant[ ship.position.sector ] = Quadrant.Thing.ship
      }
    }
    enemyRespondsToAttack()
  }
}
