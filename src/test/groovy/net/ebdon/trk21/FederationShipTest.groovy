package net.ebdon.trk21;

import groovy.mock.interceptor.StubFor;
import static Quadrant.Thing;
/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
 * @copyright
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

    FederationShip ship = new FederationShip();
    final int galaxyLength = 8;
    def galaxy;
    StubFor galaxyStub;

    void setUp() {
      logger.info "setUp"
      galaxyStub = new StubFor( Galaxy )
      galaxyStub.use { galaxy = new Galaxy() }
      logger.info "setUp -- OK"
    }

    void testDeadInSpace() {
      ship.with {
        assertFalse "Ship should NOT be dead:\n$ship", deadInSpace()

        energyNow = 0
        assertTrue  "Ship SHOULD be dead:\n$ship", deadInSpace()
      }
    }

    void testShipConstruction() {
      logger.info 'testShipConstruction'
      ship.with {
        assertFalse(  "$ship", isValid() )  // Position is invalid.
        positionShip()
        assertTrue(   "$ship", isValid() )  // Position is invalid.
        assertEquals( "$ship", energyAtStart, energyNow )
        assertEquals( "$ship", maxTorpedoes, numTorpedoes )
        assertEquals( "$ship", 'GREEN', condition )
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
      logger.info "testBadConditionChanges"
      ship.with {
        condition = allowedConditions[0]
        assertEquals( "$ship", allowedConditions[0], condition )

        shouldFail( org.codehaus.groovy.runtime.powerassert.PowerAssertionError ) {
          condition = "flooded"
        }
        assertEquals( "$ship", allowedConditions[0], condition )

        allowedConditions.each { newCond ->
          shouldFail( org.codehaus.groovy.runtime.powerassert.PowerAssertionError ) {
            condition = newCond.toLowerCase()
          }
          assertEquals( "$ship", allowedConditions[0], condition )
        }
      }
      logger.info "testBadConditionChanges -- OK"
    }

    void testGoodConditionChanges() {
      ship.with {
      logger.info "testGoodConditionChanges"
        positionShip()
        assertEquals( "$ship", true, isValid() )
        allowedConditions.each { newCond ->
          condition = newCond
          assertEquals( "$ship", newCond, condition )
          assertEquals( "$ship", true, isValid() )
        }
      }
      logger.info "testGoodConditionChanges -- OK"
    }

    void testProtectedBystarBase() {
      logger.info 'testProtectedBystarBase'
      ship.with {
        assertEquals 'GREEN', condition
        assertFalse "$ship", isProtectedByStarBase()
        condition = 'DOCKED'
        assertTrue "$ship", isProtectedByStarBase()
      }
      logger.info 'testProtectedBystarBase -- OK'
    }

    void testHitFromEnemy() {
      logger.info 'testHitFromEnemy'
      ship.with {
        shouldFail( org.codehaus.groovy.runtime.powerassert.PowerAssertionError ) {
          hitFromEnemy energyAtStart // fail, not in condition red.
        }

        condition = 'RED'
        hitFromEnemy energyAtStart // Succeed: Ship is in condition red.
        assertEquals "$ship", 0, energyNow
      }
      logger.info 'testHitFromEnemy -- OK'
    }

    void testHitWhileprotectedByStarBase() {
      logger.info 'testHitWhileprotectedByStarBase'
      shouldFail {
        ship.with {
          condition = 'DOCKED'
          hitFromEnemy energyAtStart // Fail: protected by SB
        }
      }
      logger.info 'testHitWhileprotectedByStarBase -- OK'
    }

    void testShortRangeScanBadEverything() {
      logger.info "testShortRangeScanBadEverything"
      // galaxy    = [:];

      galaxyStub.demand.size {0}
      galaxyStub.use {
        ship.with {
          position.quadrant = new Coords2d( row: 0, col: 0 )
          shouldFail {
            shortRangeScan( galaxy ) // Everything's invalid.
            // shortRangeScan( galaxy, 0, 0 ) // Everything's invalid.
          }
        }
      }
      logger.info "testShortRangeScanBadEverything -- OK"
    }

    void testShortRangeScanBad() {
      logger.info "testShortRangeScanBad"
      // int quadX, quadY    = 0; ///< Q1% and Q2% in TREK.BAS
      ship.with {
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
          // quadX = 1
          // quadY = 1
          galaxy.remove([1,1]) // galaxy is no longer square
          shortRangeScan( galaxy ) // fail: galaxy smaller than expected
        }
      }
      logger.info "testShortRangeScanBad -- OK"
    }

    void testShortRangeScanGoodGreen() {
      logger.info "testShortRangeScanGoodGreen"

      // galaxy.keySet().each {
      galaxyStub.demand.getAt(1..548) {
        // println 'returning 000 @ 178'
        000
        /// @todo 64 should be more than enough... but isn't
      }
      getGalaxyKeySet().each {
        scan it, "GREEN"
      }
      logger.info "testShortRangeScanGoodGreen -- OK"
    }

    private def getGalaxyKeySet() {
      def ks = []
      1.upto( 8 ) { i ->
        1.upto(8) { j ->
          ks << [i,j]
        }
      }

      ks
    }
    void testShortRangeScanYellowLowEnergy() {
      logger.info "testShortRangeScanYellowLowEnergy"
      ship.with {
        assertEquals 'GREEN', condition
        assertEquals energyAtStart, energyNow
        energyNow = lowEnergyThreshold
        galaxyStub.demand.getAt { 4 } // 4 stars
        scan new Coords2d( row: 1, col: 1 ), "YELLOW"
      }
      logger.info "testShortRangeScanYellowLowEnergy -- OK"
    }

    void testShortRangeScanGoodYellow() {
      logger.info "testShortRangeScanGoodYellow"

      // getGalaxyKeySet().each {
        ship.position = new Position().tap {
          quadrant = new Coords2d(row: 1, col: 1)
          sector   = new Coords2d(row: 1, col: 1)
        }
        makeYellow()
        scan ship.position.quadrant, "YELLOW"
      // }
      logger.info "testShortRangeScanGoodYellow -- OK"
    }

    void testShortRangeScanGoodRed() {
      logger.info "testShortRangeScanGoodRed"

      makeRed()
      getGalaxyKeySet().each {
          // makeRed it
        scan it, "RED"
      }
      logger.info "testShortRangeScanGoodRed -- OK"
    }

    void testDockingBad() {
      logger.info 'testDockingBad'
      ship.with {
        assertEquals "$ship", 'GREEN', condition
        def quadrant = new Quadrant() //galaxy

        (-1).upto(0) { i->
          (-1).upto(0) { j->
            shouldFail {
              logger.debug "Testing $i - $j against empty quadrant."
              attemptDocking( quadrant, i, -j ) // fail, invalid coords
            }
          }
        }
        shouldFail {
          logger.debug "Try to dock while in same sector as a star base."
          quadrant[4,4] = 3  /// @todo testDockingBad() hard-codes 3 for starbase
          attemptDocking( quadrant, 4, 4 ) // fail, in same sector as @ref StarBase
        }
      }
      logger.info 'testDockingBad -- OK'
    }

    void testDockingGood() {
      logger.info 'testDockingGood'
      ship.with {
        assertEquals "$ship", 'GREEN', condition
        def quadrant = new Quadrant()

        quadrant[4,4] = Thing.base

        3.upto(5) { i ->
          3.upto(5) { j ->
            if ( [4,4] != [i,j] ) { // can't be in same sector as a @ref StarBase
              position.sector = new Coords2d( row: i, col: j )
              attemptDocking quadrant
              // attemptDocking( quadrant, i, j )
              assertEquals "$ship", 'DOCKED', condition
            }
          }
        }
      }
      logger.info 'testDockingGood -- OK'
    }

    void testMove() {
      logger.info 'testMove'
      // if (notYetImplemented()) return

      ShipVector sv = new ShipVector()
      //
      // galaxyStub.demand.with {
      //   insideGalaxy(1..99) { x -> println 'insideGalaxy @ 279'; true }
      //   insideGalaxy(1..99) { x,y -> println 'insideGalaxy @ 280'; true }
      //   getAt { 4 } // 4 stars
      // }

      galaxyStub.use {
        ship.with {
          shouldFail( org.codehaus.groovy.runtime.powerassert.PowerAssertionError ) {
            move( sv ) // Fail: invalid course.
          }
          sv.course = 1
          sv.warpFactor = 1
          move( sv )
          assertFalse deadInSpace() // Should succeed.

          energyNow = 1
          sv.course = 1
          sv.warpFactor = 1
          move( sv )
          assertTrue deadInSpace()
        }
      }
      logger.info 'testMove -- OK'
    }

    private String scan( final ourPosition, final expectCondition ) {
      galaxyStub.demand.with {
        size {/*println 'returning size=64 @ 306';*/ 64}
        // insideGalaxy(1..99) { posQuad -> println "insideGalaxy[$posQuad]"; true }
        // insideGalaxy(1..99) { x,y -> println "insideGalaxy[$x,$y]"; true }
        insideGalaxy { /*println 'insideGalaxy[???] @ 309';*/ true }
      }

      ship.with {
          position.quadrant = ourPosition
          galaxyStub.use {
            shortRangeScan galaxy
          }
          // shortRangeScan( galaxy, *ourPosition )
          assertEquals "$ship", expectCondition, condition
      }
    }

    private makeRed() { //( final ourPosition ) { /// @todo Move ourPosition() into a MockGalaxy class.
      // galaxy[ ourPosition ] = 900
      galaxyStub.demand.getAt(1..64) {900}
  }

    private makeYellow() { /// @todo Move makeYellow() into a MockGalaxy class.
      // getGalaxyKeySet().each { pos ->
        // galaxy[ pos ] =
      galaxyStub.demand.with {
        getAt { final Coords2d x ->
          // println "Called with $x, returning 000"
          000
        }
        getAt(1..4) { final ArrayList x ->
          final int rv = ( x == [1,1] ? 000 : 900 )
          // println "Called with ${x.class.name} = $x, returning $rv"
          rv
        }
      }
    }

    /// @deprecated
    // private def getValidGalaxy() { /// @todo Move getValidGalaxy() into a MockGalaxy class.
    //     def galaxy = [:]
    //     1.upto( galaxyLength) { i->
    //         1.upto( galaxyLength ) { j->
    //             galaxy[i,j] = 0
    //         }
    //     }
    //     galaxy
    // }
}
