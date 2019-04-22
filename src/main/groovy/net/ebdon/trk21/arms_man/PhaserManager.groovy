package net.ebdon.trk21.arms_man;

import net.ebdon.trk21.Quadrant;
import net.ebdon.trk21.EnemyFleet;
import net.ebdon.trk21.FederationShip;
import net.ebdon.trk21.DamageControl;
import net.ebdon.trk21.UiBase;
import net.ebdon.trk21.Battle;
/**
 * @file
 * @author      Terry Ebdon
 * @date        April 2019
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
@groovy.transform.TypeChecked
final class PhaserManager extends WeaponsManager {

  PhaserManager( UiBase uib, EnemyFleet ef, FederationShip fs, DamageControl dc ) {
    super( uib, ef, fs, dc )
  }

  private int uiIntegerInput( final String propertyKey ) {
    ui.getFloatInput( propertyKey ).toInteger()
  }

  private void attackFleetWithPhasers( final int energy ) {
    new Battle(
      enemyFleet, ship, damageControl, ui
    ).phaserAttackFleet( energy )
  }

  boolean fire( Quadrant quadrant = null ) {
    log.info "Fire phasers - available energy: ${ship.energyNow}"
    assert quadrant == null
    boolean fired = false
    final int energy = uiIntegerInput( 'input.phaserEnergy' )

    if ( energy > 0 ) {
      if ( energy <= ship.energyNow ) {
        attackFleetWithPhasers energy
        fired = true
      } else {
        log.info 'Phaser command refused; user tried to fire too many units.'
        ui.localMsg 'phaser.refused.badEnergy'
      }
    } else {
      log.info 'Command cancelled by user.'
    }
    log.info "Fire phasers completed - available energy: ${ship.energyNow}"
    fired
  }
}
