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
final class TorpedoManager extends WeaponsManager {
  private Quadrant.Thing thingDestroyed;

  TorpedoManager( UiBase uib, EnemyFleet ef, FederationShip fs, DamageControl dc ) {
    super( uib, ef, fs, dc )
  }

  boolean fire( Quadrant quadrant = null ) {
    log.info "Launch - available: ${ship.numTorpedoes}"
    assert quadrant != null
    boolean fired = false

    float course = ui.requestCourse()
    if ( course ) {
      if ( ship.numTorpedoes ) {
        Battle battle = new Battle(
          enemyFleet, ship, damageControl, ui
        )
        battle.fireTorpedo course, quadrant
        thingDestroyed = battle.thingDestroyed
        fired = true
      } else {
        ui.localMsg 'torpedo.unavailable'
      }
    }
    log.info "Launch completed - available: ${ship.numTorpedoes}"
    fired
  }

  Quadrant.Thing getThingDestroyed() {
    this.thingDestroyed
  }
}
