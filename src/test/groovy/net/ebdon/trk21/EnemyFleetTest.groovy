package net.ebdon.trk21;

import org.codehaus.groovy.runtime.powerassert.PowerAssertionError;
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

@groovy.util.logging.Log4j2('logger') /// @warning 'logger' not 'log' as the latter conflicts with the base class
final class EnemyFleetTest extends GroovyTestCase {
    private EnemyFleet enemyFleet = new EnemyFleet();

    void testFleet() {
      logger.info 'testFleet'
      assert enemyFleet.valid
    }

    void testFleetCanAttack() {
      logger.info 'testFleetCanAttack'
      enemyFleet.with {
        assert !canAttack()
        numKlingonBatCrTotal  = 1
        numKlingonBatCrRemain = 1
        numKlingonBatCrInQuad = 0
        assert !canAttack()
        numKlingonBatCrInQuad = 1
        assert canAttack()
      }
    }

    @groovy.transform.TypeChecked
    void testAttack() {
      Closure reporterNotCalled = { int attackWithEnergyAmt, String msg ->
        assert false // Should never get here
      }

      enemyFleet.with {
        shouldFail(PowerAssertionError) {
          attack new Coords2d(2,2), reporterNotCalled // Fail: No Klingons in fleet.
        }

        numKlingonBatCrTotal  = 1;
        numKlingonBatCrRemain = 1;
        numKlingonBatCrInQuad = 1;

        shouldFail(PowerAssertionError) {
          attack new Coords2d(2,2), reporterNotCalled // Fail: 1 ship but it's not positioned.
        }

        // def reporterMustbeCalled = { attackWithEnergyAmt, msg ->
        //   static called = false;
        // }

        positionInSector( 1, [1,1] )
        attack( new Coords2d(2,2), ReporterMock.reporterMustbeCalled )
        assert ReporterMock.reporterCalled
        assert ReporterMock.attackAmount > 0
      }
    }

    //@NotYetImplemented
    void testEnergyHittingTarget() {
      logger.info 'testEnergyHittingTarget'

      // Test every variation of these 2 invalid arguments.
      enemyFleet.with {
        [-1,0].permutations().eachCombination { params ->
          shouldFail(PowerAssertionError) { // Fail: invalid arguments.
            energyHittingTarget(
              params[0],  // energyReleased
              params[1]   // distanceToTarget
            )
          }
        }
      }

      // Test every variation of 1 valid argument and 1 invalid argument.
    }

    @groovy.transform.TypeChecked
    void testGoodFleetSize() {
        logger.info 'testGoodFleetSize'
        enemyFleet.with {
            log.info 'testGoodFleetSize'
            for ( int numEnemyShips in 1..maxPossibleKlingonShips ) {
                numKlingonBatCrTotal = numEnemyShips
                numKlingonBatCrRemain = numEnemyShips
            }
            numKlingonBatCrTotal = maxKlingonBCinQuad
            numKlingonBatCrRemain = maxKlingonBCinQuad
            assert valid
            for ( int numEnemyShips in 0..maxKlingonBCinQuad ) {
                numKlingonBatCrInQuad = numEnemyShips
                assert numEnemyShips == numKlingonBatCrInQuad
                assert valid
            }
        }
    }

    void testBadFleetSize() {
      logger.info 'testBadFleetSize'
      enemyFleet = new EnemyFleet()
      enemyFleet.with {
        log.info 'testBadFleetSize'
        shouldFail(PowerAssertionError) {
            numKlingonBatCrRemain = 9 // Fail: 9 remain out of zero total.
        }
        assert valid
        [ -1, 10 ].each { int numShips ->
            shouldFail(PowerAssertionError) {
                numKlingonBatCrInQuad = numShips
            }
        }
        assert valid
      }
    }

    void testDistance() {
      logger.info 'testDistance'

      enemyFleet.with {
        numKlingonBatCrTotal    = 1
        numKlingonBatCrRemain   = 1
        numKlingonBatCrInQuad   = 1

        // From Pythagaras: distance from [1,1] to [2,2] is sqrt(1*^2 + 1^2)
        positionInSector( 1, [1, 1] )
        final Float root2 = Math.sqrt(2)
        assert root2 == distanceToTarget( 1, new Coords2d(2,2) )
      }
    }

    void testFleetPosition() {
        logger.info 'testFleetPosition'
        enemyFleet = new EnemyFleet()
        enemyFleet.with {
            log.info 'testFleetPosition'
            shouldFail(PowerAssertionError) {
                positionInSector( 1, [1,1] ) // Positioning non-existent assets
            }

            numKlingonBatCrTotal    = maxKlingonBCinQuad
            numKlingonBatCrRemain   = maxKlingonBCinQuad
            numKlingonBatCrInQuad   = maxKlingonBCinQuad

            positionInSector 1, [1,1]
            assert maxKlingonEnergy == klingons[1][3]

            for ( int shipNum in 1..numKlingonBatCrInQuad ) {
                shouldFail(PowerAssertionError) { // sector is already occupied.
                    positionInSector shipNum, [1,1]
                }
            }

            2.upto( numKlingonBatCrInQuad ) {
              positionInSector it, [2,it - 1]
              assert maxKlingonEnergy == klingons[it][3]
            }

            assert numKlingonBatCrInQuad == klingons.count {
              it[1] != 0 && it[2] != 0
            }

            assert maxKlingonEnergy * numKlingonBatCrInQuad == klingons.sum {
              it[3]
            }
        }
    }
}

@groovy.util.logging.Log4j2
class ReporterMock {
  static boolean reporterCalled = false;
  static int attackAmount = 0;
  static Closure reporterMustbeCalled = { attackWithEnergyAmt, msg ->
    log.info "reporterMustbeCalled( attackWithEnergyAmt: $attackWithEnergyAmt, msg: $msg )"
    reporterCalled = true;
    attackAmount =  attackWithEnergyAmt
  }
}
