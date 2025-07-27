package net.ebdon.trk21;

import static Quadrant.*;
import org.apache.logging.log4j.Level;
import groovy.mock.interceptor.MockFor;
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
abstract class RepositionerTestBase extends GroovyTestCase {

  abstract protected void transit( final int expectedRowOffset, final int expectedColOffset );

  final String errTransitBadPos    = 'In wrong quadrant %s at q.step %2d with offsets of [%d, %d]';
  private final String msgTransitTestStart = 'Transit in at most %2d steps with ShipVector: %s';
  private final String msgTransitTestEnd   = 'Transit in at most %2d steps with ShipVector: %s -- OK';
  final String msgQstep            = 'q.step %2d';
  final String msgQuad             = 'Quadrant [%2d, %2d]';
  final String msgSect             = 'Sector [%2d, %2d]';
  final String msgStartStepQuad    = "$msgQstep starting with  %s";
  final String msgStepNowIn        = "$msgQstep:        now in %s";
  final String msgStepExpectIn     = "$msgQstep:  should be in %s";
  final String msgEndStepQuad      = "$msgQstep: finished with %s";

  @groovy.transform.TypeChecked
  private float getCourseFrom( final int expectedRowOffset, final int expectedColOffset ) {
    final Map course = [
      [0,1]: 1F,   // "East"
      [1,0]: 7F,   // "South"
      [1,1]: 8F    // "South-East"
    ]

    final float rv = course[ expectedRowOffset, expectedColOffset ]
    logger.info "Expected course is $rv"
    rv
  }

  @groovy.transform.TypeChecked
  final ShipVector shipWarpDir( final float warp, final float dir ) {
    assert warp > 0F && warp < 13F
    assert dir  > 0F && dir  < 8.01F /// @todo Max course is weird.

    new ShipVector().tap {
      course      = dir
      warpFactor  = warp
      assert isValid()
    }
  }

  abstract protected ShipVector getTransitShipVector( final float course ) ;
  abstract protected List<Integer> getExpectedTransitCoords(
      final int stepNum,
      final int expectedRowOffset,
      final int expectedColOffset );

  @Newify([MockFor,Position,Coords2d])
  protected void transitSteps(
      final int expectedRowOffset, final int expectedColOffset, final int maxSteps ) {
    logger.info "transitSteps called with $expectedRowOffset, $expectedColOffset, $maxSteps"
    final float course = getCourseFrom( expectedRowOffset, expectedColOffset )
    ShipVector sv = getTransitShipVector( course )
    logger.printf Level.INFO, msgTransitTestStart, maxSteps, sv
    final Coords2d c2d = [1,1]
    Position shipPos = [c2d.clone(),c2d.clone()]

    Map fakeQuadrant = [
      contains   : { int z1, int z2 -> z1 in minCoord..maxCoord && z2 in minCoord..maxCoord },
      isOccupied : { int z1, int z2 -> false }, // empty quadrant
      [1, 1]     : Thing.ship,
    ]

    for ( int row in 2..maxCoord ) {
      for ( int col in 2..maxCoord ) {
        fakeQuadrant[row, col] = Thing.emptySpace
      }
    }

    assert  maxSteps > 0
    TestUi ui = new TestUi()

    for ( int stepNum in 1..maxSteps ) {
      logger.printf Level.INFO, msgStartStepQuad, stepNum, shipPos

      MockFor trekMock = MockFor( Trek ) // No demnds, should never be accessed.
      MockFor shipMock = MockFor( FederationShip )
      shipMock.demand.with {
        getId                     { 'transitSteps' }
        getEnergyUsedByLastMove   { 8F * sv.warpFactor }
        getPosition(5..99)        { shipPos }
        getPosition(0..1)         { shipPos } // 0..1, depending on the log level.
      }

      trekMock.use {
        shipMock.use {
          Repositioner rp = new ShipRepositioner(
            ship:     new FederationShip(),
            ui:       ui,
            quadrant: fakeQuadrant
          )
          rp.repositionShip sv

          Coords2d expectedQuadrant = getExpectedTransitCoords(
              stepNum, expectedRowOffset, expectedColOffset )

          logger.printf Level.INFO, msgStepNowIn,    stepNum, shipPos
          logger.printf Level.INFO, msgStepExpectIn, stepNum, shipPos.quadrant

          assert shipPos.quadrant == expectedQuadrant
        }
      }
      logger.printf Level.INFO, msgTransitTestEnd, maxSteps, sv
    }
  }
}
