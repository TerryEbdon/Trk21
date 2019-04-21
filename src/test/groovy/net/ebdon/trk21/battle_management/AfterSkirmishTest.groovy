package net.ebdon.trk21.battle_management;

import static net.ebdon.trk21.Quadrant.Thing;
import static net.ebdon.trk21.ShipDevice.*;
import groovy.mock.interceptor.MockFor;
import net.ebdon.trk21.Coords2d;
import net.ebdon.trk21.Quadrant;
import net.ebdon.trk21.QuadrantSetup;
import net.ebdon.trk21.Galaxy;
import net.ebdon.trk21.GalaxyManager;
import net.ebdon.trk21.EnemyFleet;
import net.ebdon.trk21.Position;
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

@Newify([MockFor,Position,Coords2d])
class AfterSkirmishTest extends GroovyTestCase {
  private MockFor galaxyMock;
  private MockFor quadrantSetup;
  private MockFor fleetMock;
  private MockFor quadMock;
  private MockFor gmMock;
  private final Coords2d shipQuadCoords = Coords2d(1,2)

  @SuppressWarnings('ExplicitCallToGetAtMethod')
  private void initDemands( int get1, int put1, int put2 ) {
    quadrantSetup.demand.updateAfterSkirmish { }
    gmMock.demand.updateNumEnemyShips { }
    // Start of demand required for updateNumEnemyShipsInQuad
    // shipMock.demand.getPosition { Position( Coords2d(1,2), Coords2d(3,4) ) }
    fleetMock.demand.getNumKlingonBatCrInQuad { 3 }
    // gmMock.demand.
    // galaxyMock.demand.with {
    //   getAt { Coords2d c2d ->
    //     assert c2d == Coords2d(1,2)
    //     get1
    //   }
    //
    //   getAt { Coords2d c2d -> assert c2d == Coords2d(1,2); get1 } // enemy @ start
    //   putAt { Coords2d c2d, int newVal -> assert newVal == put1 } // remove them
    //   getAt { Coords2d c2d -> assert c2d == Coords2d(1,2); put1 }
    //   putAt { Coords2d c2d, int newVal -> assert newVal == put2 } // sync with fleet
    // }
    // end of demand required for updateNumEnemyShipsInQuad
  }

  private void runAfterSkirmishTest( Quadrant.Thing thing ) {
    quadMock.use {
      Quadrant quadrant = new Quadrant()
      galaxyMock.use {
        Galaxy galaxy = new Galaxy()
        fleetMock.use {
          EnemyFleet enemyFleet = new EnemyFleet()
          gmMock.use {
            quadrantSetup.use {
              new AfterSkirmish( quadrant, galaxy, shipQuadCoords ).
                updateQuadrant( thing, enemyFleet )
            }
          }
        }
      }
    }
  }

  @Override void setUp() {
    galaxyMock    = MockFor( Galaxy )
    quadrantSetup = MockFor( QuadrantSetup )
    fleetMock     = MockFor( EnemyFleet )
    quadMock      = MockFor( Quadrant )
    gmMock        = MockFor( GalaxyManager )
  }

  @SuppressWarnings('JUnitTestMethodWithoutAssert')
  void testUpdateQuadrantDeadEnemy() {
    // MockFor shipMock      = MockFor( FederationShip )
    // gmMock.demand.updateNumEnemyShips { int n -> }
    initDemands 919, 19, 319
    // quadrantSetup.demand.updateAfterSkirmish { }
    // shipMock.use {
      // trek.ship = new FederationShip()
    runAfterSkirmishTest Quadrant.Thing.emptySpace
  }

  @SuppressWarnings('JUnitTestMethodWithoutAssert')
  @Newify([MockFor,Position,Coords2d])
  void testUpdateQuadrantDeadStar() {
    gmMock.demand.updateNumInQuad { Quadrant.Thing thingDestroyed ->
      assert thingDestroyed == Quadrant.Thing.star
    }
    initDemands 919, 91, 918

    // quadrantSetup.demand.updateAfterSkirmish { }
    final Coords2d shipQuadCoords = Coords2d(1,2)
    // shipMock.use {
      // trek.ship = new FederationShip()
    runAfterSkirmishTest Quadrant.Thing.star
  }
}
