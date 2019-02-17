package net.ebdon.trk21;

@groovy.util.logging.Log4j2('logger') /// @warning 'logger' not 'log' as the latter conflicts with the base class
final class EnemyFleetTest extends GroovyTestCase {
    EnemyFleet enemyFleet = new EnemyFleet();

    void testFleet() {
      logger.info 'testFleet'
      enemyFleet.with {
        assertTrue( "$enemyFleet", isValid())
      }
    }

    void testFleetCanAttack() {
      logger.info 'testFleetCanAttack'
      enemyFleet.with {
        assertFalse "$enemyFleet", canAttack()
        numKlingonBatCrTotal  = 1
        numKlingonBatCrRemain = 1
        numKlingonBatCrInQuad = 0
        assertFalse "$enemyFleet", canAttack()
        numKlingonBatCrInQuad = 1
        assertTrue "$enemyFleet", canAttack()
      }
    }

    void testAttack() {
      def reporterNotCalled = { attackWithEnergyAmt, msg ->
        assert false // Should never get here
      }

      enemyFleet.with {
        shouldFail {
          attack reporter // Fail: No Klingons in fleet.
        }

        numKlingonBatCrTotal  = 1;
        numKlingonBatCrRemain = 1;
        numKlingonBatCrInQuad = 1;

        shouldFail {
          attack reporter // Fail: 1 ship but it's not positioned.
        }

        // def reporterMustbeCalled = { attackWithEnergyAmt, msg ->
        //   static called = false;
        // }

        positionInSector( 1, [1,1] )
        attack( [2,2], ReporterMock.reporterMustbeCalled )
        assertTrue ReporterMock.reporterCalled
        assertTrue ReporterMock.attackAmount > 0
      }
    }

    //@NotYetImplemented
    void testEnergyHittingTarget() {
      logger.info "testEnergyHittingTarget"

      // Test every variation of these 2 invalid arguments.
      enemyFleet.with {
        [-1,0].permutations().eachCombination{
          shouldFail { // Fail: invalid arguments.
            energyHittingTarget(
              it[0],  // energyReleased
              it[1]   // distanceToTarget
            )
          }
        }
      }

      // Test every variation of 1 valid argument and 1 invalid argument.
    }

    void testGoodFleetSize() {
        logger.info 'testGoodFleetSize'
        enemyFleet.with {
            log.info 'testGoodFleetSize'
            1.upto( maxPossibleKlingonShips ) {
                numKlingonBatCrTotal = it
                numKlingonBatCrRemain = it
            }
            numKlingonBatCrTotal = 9
            numKlingonBatCrRemain = 9
            assertEquals( "$enemyFleet", true, isValid() )
            0.upto(9) {
                numKlingonBatCrInQuad = it
                assertEquals( "$enemyFleet", it, numKlingonBatCrInQuad )
                assertTrue( "$enemyFleet", isValid() )
            }
        }
    }

    void testBadFleetSize() {
      logger.info 'testBadFleetSize'
      enemyFleet = new EnemyFleet()
      enemyFleet.with {
        log.info 'testBadFleetSize'
        shouldFail {
            numKlingonBatCrRemain = 9 // Fail: 9 remain out of zero total.
        }
        assertEquals( "$enemyFleet", true, isValid() )
        [ -1, 10 ].each {
            shouldFail {
                numKlingonBatCrInQuad = it
            }
        }
        assertTrue( "$enemyFleet", isValid() )
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
        assertEquals root2, distanceToTarget( 1, [2,2] )
      }
    }

    void testFleetPosition() {
        logger.info 'testFleetPosition'
        enemyFleet = new EnemyFleet()
        enemyFleet.with {
            log.info 'testFleetPosition'
            shouldFail {
                positionInSector( 1, [1,1] ) // Positioning non-existent assets
            }

            numKlingonBatCrTotal    = maxKlingonBCinQuad
            numKlingonBatCrRemain   = maxKlingonBCinQuad
            numKlingonBatCrInQuad   = maxKlingonBCinQuad

            positionInSector 1, [1,1]
            assertEquals maxKlingonEnergy, klingons[1][3]

            1.upto( numKlingonBatCrInQuad ) {
                shouldFail { // sector is already occupied.
                    positionInSector it, [1,1]
                }
            }

            2.upto( numKlingonBatCrInQuad ) {
                positionInSector it, [2,it - 1]
                assertEquals maxKlingonEnergy, klingons[it][3]
            }

            assertEquals(
                "$enemyFleet",
                numKlingonBatCrInQuad,
                klingons.count { it[1] !=0 && it[2] !=0 }
            )

            assertEquals(
                "$enemyFleet",
                maxKlingonEnergy * numKlingonBatCrInQuad,
                klingons.sum { it[3] }
            )
        }
    }
}

@groovy.util.logging.Log4j2
class ReporterMock {
  static boolean reporterCalled = false;
  static int attackAmount = 0;
  static def reporterMustbeCalled = { attackWithEnergyAmt, msg ->
    log.info "reporterMustbeCalled( attackWithEnergyAmt: $attackWithEnergyAmt, msg: $msg )"
    reporterCalled = true;
    attackAmount =  attackWithEnergyAmt
  }
}
