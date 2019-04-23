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

@groovy.transform.TypeChecked
class GameState {
  private final EnemyFleet     enemyFleet;
  private final FederationShip ship;
  private final TrekCalendar   game;

  GameState( EnemyFleet ef, FederationShip fs, TrekCalendar tc ) {
      enemyFleet = ef
      ship       = fs
      game       = tc
  }

  void tick() {
    game.tick()
  }

  boolean continues() {
    !won() && !lost()
  }

  boolean won() {
    enemyFleet.defeated
  }

  boolean lost() {
    ship.deadInSpace() || game.outOfTime()
  }
}
