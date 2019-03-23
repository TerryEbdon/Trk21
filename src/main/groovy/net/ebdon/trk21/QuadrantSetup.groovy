package net.ebdon.trk21;

/**
 * @file
 * @author      Terry Ebdon
 * @date        March 2019
 * @copyright   Terry Ebdon, 2019
 *
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
@groovy.transform.ToString
@groovy.transform.TupleConstructor
class QuadrantSetup {
  Quadrant   quadrant;
  EnemyFleet enemyFleet;

  private void positionThings( final int numThings, final Quadrant.Thing thing ) {
    log.debug "1. Positioning $numThings game pieces of type $thing"
    positionThings( numThings, thing ) { int i, List<Integer> pos ->
      // default empty closure.
    }
  }

  private void positionThings(
      final int numThings, final Quadrant.Thing thing, Closure closure ) {
    log.debug "2. Positioning $numThings game pieces of type $thing"
    assert quadrant?.valid
    for ( int thingNo = 1; thingNo <= numThings; ++thingNo ) {
      final List<Integer> thingPos = quadrant.emptySector
      log.trace "... Game piece $thingNo is at sector $thingPos"
      quadrant[thingPos] = thing
      closure thingNo, thingPos
    }
  }

  void positionEnemy( final int numEnemyShips ) {
    log.info 'positioning {} enemy ships.', numEnemyShips
    assert (0..9).contains( numEnemyShips )
    assert enemyFleet?.valid
    enemyFleet.numKlingonBatCrInQuad = numEnemyShips
    enemyFleet.resetQuadrant()

    positionThings( numEnemyShips, Quadrant.Thing.enemy ) { int shipNo, List<Integer> pos ->
      enemyFleet.positionInSector shipNo, pos
    }
  }

  void positionBases( final int numBases ) {
    log.debug "Positioning $numBases bases."
    assert (0..1).contains( numBases )
    positionThings numBases, Quadrant.Thing.base
    log.info "Positioned $numBases bases."
  }

  void positionStars( final int numStars ) {
    log.debug "Positioning $numStars stars."
    assert (0..9).contains( numStars )
    positionThings numStars, Quadrant.Thing.star
    log.trace "Positioned $numStars stars."
  }

  void updateAfterSkirmish() {
    assert enemyFleet.valid
    Map<List<Integer>,Quadrant.Thing> enemiesAtStart = quadrant.findEnemies()
    log.debug "enemiesAtStart: $enemiesAtStart"
    log.debug "Found {} possibly dead enemies.",
      enemiesAtStart ? enemiesAtStart.size() : 'no'

    enemiesAtStart.each { List<Integer>sectorCoords, Quadrant.Thing thing ->
      if ( !enemyFleet.isShipAt( sectorCoords ) ) {
        assert thing == Quadrant.Thing.enemy
        assert sectorCoords.size() == 2
        log.info "Removing vanquished $thing from sector $sectorCoords"
        quadrant.removeEnemy( sectorCoords )
      }
    }
  }
}
