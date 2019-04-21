package net.ebdon.trk21.battle_management;

import static net.ebdon.trk21.Quadrant.Thing;
import static net.ebdon.trk21.ShipDevice.*;
import net.ebdon.trk21.Coords2d;
import net.ebdon.trk21.Quadrant;
import net.ebdon.trk21.QuadrantSetup;
import net.ebdon.trk21.Galaxy;
import net.ebdon.trk21.GalaxyManager;
import net.ebdon.trk21.EnemyFleet;
/**
 * @file
 * @author      Terry Ebdon
 * @date        April 2019
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
class AfterSkirmish {
  private final Galaxy galaxy;
  private final Quadrant quadrant;
  private final Coords2d shipQuad;

  AfterSkirmish( final Quadrant qnt, Galaxy gal, final Coords2d shipQd ) {
    quadrant = qnt
    galaxy = gal
    shipQuad  = shipQd
  }

  void updateQuadrant( final Thing thingDestroyed, EnemyFleet enemyFleet ) {
    log.debug 'updateQuadrantAfterSkirmish BEGIN'
    final GalaxyManager gm = new GalaxyManager( shipQuad, galaxy )

    if ( thingDestroyed != Thing.emptySpace ) {
      gm.updateNumInQuad thingDestroyed
    } else {
      log.debug 'Destroyed {}, nothing to do. )', thingDestroyed
    }
    gm.updateNumEnemyShips enemyFleet.numKlingonBatCrInQuad
    new QuadrantSetup( quadrant, enemyFleet ).updateAfterSkirmish()
    log.debug 'updateQuadrantAfterSkirmish END'
  }

}
