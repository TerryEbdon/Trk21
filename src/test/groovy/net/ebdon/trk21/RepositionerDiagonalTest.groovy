package net.ebdon.trk21;

import static GameSpace.*;
import static Quadrant.*;
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
final class RepositionerDiagonalTest extends RepositionerTestBase {

  final void testTransitDiagonal() {
    transit 1, 1
  }

  @Override
  protected ShipVector getTransitShipVector( final float course ) {
    shipWarpDir 12F, course
  }

  @groovy.transform.TypeChecked
  @Override protected List<Integer> getExpectedTransitCoords(
      final int stepNum, final int expectedRowOffset, final int expectedColOffset ) {
    [8,8]
  }

  final void transit( final int expectedRowOffset, final int expectedColOffset ) {
    logger.info "Transit with expected offsets: $expectedRowOffset, $expectedColOffset"
    assert expectedRowOffset == expectedColOffset

    final float cornerToCornerDistance = Math.sqrt( 2 * maxCoord**2 )
    final float stepSize = Math.sin( Math.toRadians( 45 ) )
    final int maxSteps = Math.round( cornerToCornerDistance / stepSize )
    logger.info "Calling transitSteps for $maxSteps steps"
    transitSteps expectedRowOffset, expectedColOffset, maxSteps

    logger.info "Transit with expected offsets: " +
      "$expectedRowOffset, $expectedColOffset -- OK"
  }
}
