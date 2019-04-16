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

@groovy.util.logging.Log4j2
@groovy.transform.TupleConstructor
final class QuadrantManager {
  Quadrant quadrant;

  @groovy.transform.TypeChecked
  void positionGamePieces( final int value, final EnemyFleet enemyFleet ) {
    final QuadrantValue qVal = new QuadrantValue( value,  )
    QuadrantSetup quadrantSetup = new QuadrantSetup( quadrant, enemyFleet )
    quadrantSetup.positionEnemy qVal.enemy
    quadrantSetup.positionBases qVal.bases
    quadrantSetup.positionStars qVal.stars
  }

  /// The ship has a arrived in a new quadrant, and already knows this.
  /// Now it needs to be allocated a sector in it's new quadrant.
  /// @todo A random sector is allocated. Should the sector instead be
  /// based on the staring sector and course vector?
  /// @todo Quadrant.randomCoords() should return a Coords2d, not a list. This
  /// would allow positionShipIn() to be type-checked.
  Coords2d positionShip() {
    quadrant.clear()
    final Coords2d shipSector = quadrant.randomCoords
    quadrant[shipSector] = Thing.ship
    log.debug 'Ship positioned at sector {}', shipSector
    shipSector
  }
}
