package net.ebdon.trk21.arms_man;

import net.ebdon.trk21.Quadrant;
import net.ebdon.trk21.EnemyFleet;
import net.ebdon.trk21.FederationShip;
import net.ebdon.trk21.DamageControl;
import net.ebdon.trk21.UiBase;
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

@groovy.transform.TypeChecked
@groovy.transform.TupleConstructor
abstract class WeaponsManager {
  final UiBase         ui;
  final EnemyFleet     enemyFleet;
  final FederationShip ship;
  final DamageControl  damageControl;

  abstract boolean fire( Quadrant quadrant = null )

  WeaponsManager( UiBase uib, EnemyFleet ef, FederationShip fs, DamageControl dc ) {
    ui            = uib
    enemyFleet    = ef
    ship          = fs
    damageControl = dc
  }
}
