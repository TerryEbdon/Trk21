package net.ebdon.trk21;

import groovy.mock.interceptor.StubFor;
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError;
import groovy.test.GroovyTestCase
/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
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

@groovy.util.logging.Log4j2('logger')
/// @warning 'logger' not 'log' as the latter conflicts with the base class
final class FederationShipTest extends GroovyTestCase {

    private FederationShip ship;
    private Galaxy galaxy;
    private StubFor galaxyStub;

    @Override void setUp() {
      super.setUp()
      logger.info 'setUp'
      ship = new FederationShip()
      galaxyStub = new StubFor( Galaxy )
      galaxyStub.use { galaxy = new Galaxy() }
      logger.info 'setUp -- OK'
    }

    void testDeadInSpace() {
      ship.with {
        id = 'testDeadInSpace'
        assert !deadInSpace()

        energyNow = 0
        assert deadInSpace()
      }
    }

    void testShipConstruction() {
      logger.info 'testShipConstruction'
      ship.with {
        id = 'testShipConstruction'
        assert !valid   // Position is invalid.
        positionShip()
        assert valid    // Position is valid.
        assert energyNow == energyAtStart
        assert numTorpedoes == maxTorpedoes
        assert condition == 'GREEN'
      }
      logger.info 'testShipConstruction -- OK'
    }

    private void positionShip() {
      ship.position = new Position().tap {
        quadrant = new Coords2d(row: 4, col: 5)
        sector   = new Coords2d(row: 6, col: 7)
      }
    }

    void testBadConditionChanges() {
      logger.info 'testBadConditionChanges'
      ship.with {
        id = 'testBadConditionChanges'
        condition = allowedConditions[0]
        assert condition == allowedConditions[0]

        shouldFail( PowerAssertionError ) {
          condition = 'flooded'
        }
        assert condition == allowedConditions[0]

        allowedConditions.each { newCond ->
          shouldFail( PowerAssertionError ) {
            condition = newCond.toLowerCase()
          }
          assert condition == allowedConditions[0]
        }
      }
      logger.info 'testBadConditionChanges -- OK'
    }

    void testGoodConditionChanges() {
      logger.info 'testGoodConditionChanges'
      ship.with {
        id = 'testGoodConditionChanges'
        positionShip()
        assert valid
        allowedConditions.each { newCond ->
          condition = newCond
          assert newCond, condition
          assert valid
        }
      }
      logger.info 'testGoodConditionChanges -- OK'
    }

    @groovy.transform.TypeChecked
    void testProtectedByStarBase() {
      logger.info 'testProtectedByStarBase'
      ship.with {
        id = 'testProtectedByStarBase'
        assert condition == 'GREEN'
        assert !isProtectedByStarBase()
        condition = 'DOCKED'
        condV2 = FederationShip.Condition.docked
        assert isProtectedByStarBase()
      }
      logger.info 'testProtectedByStarBase -- OK'
    }

    void testHitFromEnemy() {
      logger.info 'testHitFromEnemy'
      ship.with {
        id = 'testHitFromEnemy'
        shouldFail( PowerAssertionError ) {
          hitFromEnemy energyAtStart // fail, not in condition red.
        }

        condition = 'RED'
        condV2 = FederationShip.Condition.red
        hitFromEnemy energyAtStart // Succeed: Ship is in condition red.
        assert energyNow == 0
      }
      logger.info 'testHitFromEnemy -- OK'
    }

    void testHitWhileprotectedByStarBase() {
      logger.info 'testHitWhileprotectedByStarBase'
      shouldFail {
        ship.with {
          id = 'testHitWhileprotectedByStarBase'
          condition = 'DOCKED'
          hitFromEnemy energyAtStart // Fail: protected by SB
        }
      }
      logger.info 'testHitWhileprotectedByStarBase -- OK'
    }

    void testShortRangeScanBadEverything() {
      logger.info 'testShortRangeScanBadEverything'

      galaxyStub.demand.getBoardSize { 0 }
      galaxyStub.use {
        ship.with {
          id = 'testShortRangeScanBadEverything'
          position.quadrant = new Coords2d( row: 0, col: 0 )
          shouldFail {
            shortRangeScan( galaxy ) // Everything's invalid.
          }
        }
      }
      logger.info 'testShortRangeScanBadEverything -- OK'
    }

    void testShortRangeScanBad() {
      logger.info 'testShortRangeScanBad'
      ship.with {
        id = 'testShortRangeScanBad'
        position.quadrant = new Coords2d( row: 0, col: 0 )
        shouldFail {
          shortRangeScan( galaxy ) // valid galaxy, bad coords.
        }
        position.quadrant = new Coords2d( row: 99, col: 99 )
        shouldFail {
          shortRangeScan( galaxy ) // valid galaxy, bad coords.
        }
        position.quadrant = new Coords2d( row: 1, col: 1 )
        shouldFail {
          galaxy.remove([1,1]) // galaxy is no longer square
          shortRangeScan( galaxy ) // fail: galaxy smaller than expected
        }
      }
      logger.info 'testShortRangeScanBad -- OK'
    }

    @SuppressWarnings('JUnitTestMethodWithoutAssert') // asserts in scan()
    void testShortRangeScanGoodGreen() {
      logger.info 'testShortRangeScanGoodGreen'

      // galaxy.keySet().each {
      galaxyStub.demand.getAt(1..548) {
        000 /// @todo 64 should be more than enough... but isn't
      }
      galaxyKeySet.each {
        scan it, 'GREEN'
      }
      logger.info 'testShortRangeScanGoodGreen -- OK'
    }

    private List getGalaxyKeySet() {
      List ks = []
      1.upto( 8 ) { i ->
        1.upto(8) { j ->
          ks << [i,j]
        }
      }
      ks
    }

    void testShortRangeScanYellowLowEnergy() {
      logger.info 'testShortRangeScanYellowLowEnergy'
      ship.with {
        id = 'testShortRangeScanYellowLowEnergy'
        assert condition == 'GREEN'
        assert energyNow == energyAtStart
        energyNow = lowEnergyThreshold
        galaxyStub.demand.getAt { 4 } // 4 stars
        scan new Coords2d( row: 1, col: 1 ), 'YELLOW'
      }
      logger.info 'testShortRangeScanYellowLowEnergy -- OK'
    }

    @SuppressWarnings('JUnitTestMethodWithoutAssert') // asserts in scan()
    void testShortRangeScanGoodYellow() {
      logger.info 'testShortRangeScanGoodYellow'
      ship.id = 'testShortRangeScanGoodYellow'
      ship.position = new Position().tap {
        quadrant = new Coords2d(row: 1, col: 1)
        sector   = new Coords2d(row: 1, col: 1)
      }
      makeYellow()
      scan ship.position.quadrant, 'YELLOW'
      logger.info 'testShortRangeScanGoodYellow -- OK'
    }

    @SuppressWarnings('JUnitTestMethodWithoutAssert') // asserts in scan()
    void testShortRangeScanGoodRed() {
      logger.info 'testShortRangeScanGoodRed'

      makeRed()
      galaxyKeySet.each {
        scan it, 'RED'
      }
      logger.info 'testShortRangeScanGoodRed -- OK'
    }

    void testDockingBad() {
      logger.info 'testDockingBad'
      ship.with {
        id = 'testDockingBad'
        assert condition == 'GREEN'
        Quadrant quadrant = new Quadrant() //galaxy

        (-1).upto(0) { i ->
          (-1).upto(0) { j ->
            shouldFail {
              logger.debug "Testing $i - $j against empty quadrant."
              attemptDocking( quadrant, i, -j ) // fail, invalid coords
            }
          }
        }
        shouldFail {
          logger.debug 'Try to dock while in same sector as a star base.'
          quadrant[4,4] = 3  /// @todo testDockingBad() hard-codes 3 for starbase
          attemptDocking( quadrant, 4, 4 ) // fail, in same sector as @ref StarBase
        }
      }
      logger.info 'testDockingBad -- OK'
    }

    void testDockingGood() {
      logger.info 'testDockingGood'
      ship.with {
        id = 'testDockingGood'
        assert condition == 'GREEN'
        Quadrant quadrant = new Quadrant()

        quadrant[4,4] = Quadrant.Thing.base

        3.upto(5) { i ->
          3.upto(5) { j ->
            if ( [4,4] != [i,j] ) { // can't be in same sector as a @ref StarBase
              position.sector = new Coords2d( row: i, col: j )
              attemptDocking quadrant
              assert condV2 == FederationShip.Condition.docked
              assert condition == 'DOCKED'
            }
          }
        }
      }
      logger.info 'testDockingGood -- OK'
    }

    void testMove() {
      logger.info 'testMove'

      ShipVector sv = new ShipVector()

      galaxyStub.use {
        ship.with {
          id = 'testMove'
          shouldFail( PowerAssertionError ) {
            move( sv ) // Fail: invalid course.
          }
          sv.course = 1
          sv.warpFactor = 1
          move( sv )
          assert !deadInSpace() // Should succeed.

          energyNow = 1
          sv.course = 1
          sv.warpFactor = 1
          move( sv )
          assert deadInSpace()
        }
      }
      logger.info 'testMove -- OK'
    }

    private String scan( final ourPosition, final String expectCondition ) {
      galaxyStub.demand.with {
        getBoardSize { 64 }
        insideGalaxy { true }
      }

      ship.with {
          position.quadrant = ourPosition
          galaxyStub.use {
            shortRangeScan galaxy
          }
          assert condition == expectCondition
      }
    }

    private void makeRed() {
      galaxyStub.demand.getAt(1..64) { 900 }
    }

    private void makeYellow() {
      galaxyStub.demand.with {
        getAt { final Coords2d x ->
          000
        }
        getAt(1..4) { final List<Integer> x ->
          final int rv = ( x == [1,1] ? 000 : 900 )
          rv
        }
      }
    }
}
