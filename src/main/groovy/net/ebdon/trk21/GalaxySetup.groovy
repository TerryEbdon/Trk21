package net.ebdon.trk21;

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

@SuppressWarnings('InsecureRandom')
@groovy.transform.TypeChecked
@groovy.util.logging.Log4j2
final class GalaxySetup {
  private final Galaxy galaxy;
  private final EnemyFleet enemyFleet;
  private Coords2d currentQuadrant;

  private int enemyInQuad;
  private int basesInQuad;
  private int starsInQuad;

  private final float[] softProbs =
      [ 0, 0.0001, 0.01, 0.03, 0.08, 0.28, 1.28, 3.28, 6.28, 13.28 ];

  GalaxySetup( final Galaxy g, final EnemyFleet ef ) {
    galaxy = g
    enemyFleet = ef
    enemyInQuad = basesInQuad = starsInQuad = 0
  }

  @SuppressWarnings('InsecureRandom')
  private int numStarsToBirth() {
    new Random().nextInt(8) + 1
  }

  @SuppressWarnings('InsecureRandom')
  private int numBasesToBirth() {
    new Random().nextFloat() > 0.9F ? 1 : 0
  }

  void addGamePieces() {
    int starsInQuad = 0

    galaxy.eachCoords2d { Coords2d c2d ->
      currentQuadrant = c2d
      addEnemy()
      addBases()
      addStars()
      QuadrantValue qv = new QuadrantValue( enemyInQuad, basesInQuad, starsInQuad )
      galaxy[ c2d ] = qv.value
    }
  }

  private void addEnemy() {
    log.debug 'Adding Enemy to {}', currentQuadrant
    enemyInQuad = 0
    final float c1 = new Random().nextFloat() * galaxy.boardSize
    enemyFleet.eachShipNo { int shipNo ->
      addShip shipNo, c1
    }
  }

  private void addShip( final int shipNo, final float c1 ) {
    log.debug 'addShip() called with args {}', [shipNo, c1]
    if ( c1 < softProbs[ shipNo ] ) {
      ++enemyFleet.numKlingonBatCrTotal
      ++enemyFleet.numKlingonBatCrRemain
      ++enemyInQuad
      log.debug 'Enemy craft added to quadrant {}, now {} in this quadrant.',
          currentQuadrant, enemyInQuad
    }
  }

  private void addBases() {
    log.debug 'Adding Bases to {}', currentQuadrant
    basesInQuad = numBasesToBirth()
    log.debug 'Bases in {}: {}.', currentQuadrant, basesInQuad
  }

  private void addStars() {
    log.debug 'Adding Stars to {}', currentQuadrant
    starsInQuad = numStarsToBirth()
  }
}
