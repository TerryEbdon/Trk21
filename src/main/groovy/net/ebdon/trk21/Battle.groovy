package net.ebdon.trk21;

import static Quadrant.Thing;
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
  Thing thingDestroyed = Thing.emptySpace;
  private int nextTargetIndex = 1;
  private int maxEnemyShips = 0;

  Expando getNextTarget() {
    log.debug "There are ${enemyFleet.numKlingonBatCrInQuad} enemy ships here."
    log.trace "Getting target $nextTargetIndex"
    maxEnemyShips = maxEnemyShips ?: enemyFleet.maxKlingonBCinQuad
    assert nextTargetIndex in 0..(1 + maxEnemyShips)
    Expando rv = null
    if ( enemyFleet.numKlingonBatCrInQuad > 0 ) {
      if ( nextTargetIndex <= maxEnemyShips ) {
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

    enemyFleet.hitOnShip( target.id, hitAmount )
    if ( enemyFleet.shipExists(target.id) ) {
      ui.fmtMsg 'battle.targetEnergyLeft', [enemyFleet.energy(target.id)]
    } else {
      ui.localMsg 'battle.enemy.destroyed'
    }
  }

  void phaserAttackFleet( final int energy ) {
    assert energy > 0
    assert enemyFleet && ship && dc
    new PhaserControl( dc, ui, ship, this ).fire( energy )
    enemyRespondsToAttack()
  }

  @TypeChecked
  void enemyRespondsToAttack() {
    log.debug 'enemyRespondsToAttack -- BEGIN'
    enemyFleet.regroup()
    if ( enemyFleet.canAttack() ) {
      log.trace 'enemyFleet can attack'
      if ( ship.protectedByStarBase ) {
        log.trace 'ship *IS* protected by a star base'
        ui.localMsg 'battle.shieldedByBase'
      } else {
        log.trace 'ship is *NOT* protected by a star base'
        enemyFleet.attack( ship.position.sector, ui.&fmtMsg )
      }
    }
    log.debug 'enemyRespondsToAttack -- END'
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

    final Coords2d shipSector    = ship.position.sector
    final Thing thingHit         = rp.thingHit
    final Coords2d torpedoSector = torpedo.position.sector
    final String torpedoId       = torpedo.id

    quadrant[ shipSector ] = Quadrant.Thing.ship

    if ( thingHit == Quadrant.Thing.enemy ) {
      log.info 'Torpedo {} hit on enemy ship at {} from Fed ship at {}.',
        torpedoId, torpedoSector, shipSector
      enemyFleet.shipHitByTorpedo torpedoSector
      quadrant[ torpedoSector ] =
        Quadrant.Thing.enemy // Will be cleaned up later by Quadrant.removeEnemy()
      ui.localMsg 'battle.enemy.destroyed'
    } else {
      if ( thingHit != Quadrant.Thing.emptySpace ) {
        quadrant[ torpedoSector ] = Quadrant.Thing.emptySpace
        thingDestroyed = thingHit
        log.info 'Torpedo {} hit on {} at {} from ship at {}',
            torpedoId, thingDestroyed, torpedoSector, shipSector
      } else {
        log.info 'Torpedo {} missed, out of range at {}', torpedoId, torpedoSector
      }
    }

    enemyRespondsToAttack()
  }
}
