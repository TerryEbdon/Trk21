package net.ebdon.trk21;

import static Quadrant.Thing;
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

@groovy.transform.TypeChecked
@groovy.transform.TupleConstructor
@groovy.util.logging.Log4j2
final class GalaxyManager {
  final Coords2d quadCoords;
  final Galaxy galaxy;

  void updateNumEnemyShips( final int fleetSize ) {
    final QuadrantValue qv = new QuadrantValue( galaxy[quadCoords] )
    galaxy[quadCoords] -= Thing.enemy.multiplier * qv.enemy
    galaxy[quadCoords] += Thing.enemy.multiplier * fleetSize
  }

  void updateNumInQuad( final Thing thingHit ) {
    log.debug 'updateNumInQuad( {} ) BEGIN', thingHit
    if ( thingHit != Thing.enemy ) {
      final QuadrantValue qv = new QuadrantValue( galaxy[quadCoords] )
      int numInQuad = qv.num( thingHit )
      log.debug '{} {}{} before update', numInQuad, thingHit, numInQuad == 1 ? '' : 's'
      galaxy[quadCoords] -= thingHit.multiplier * numInQuad--
      galaxy[quadCoords] += thingHit.multiplier * numInQuad
      log.debug '{} {}{} after update', numInQuad, thingHit, numInQuad == 1 ? '' : 's'
    }
    log.debug 'updateNumInQuad( {} ) END', thingHit
  }
}
