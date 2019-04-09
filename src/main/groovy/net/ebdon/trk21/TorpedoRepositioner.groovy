package net.ebdon.trk21;

import static GameSpace.*;
import static Quadrant.*;
import static CourseOffset.*;
import org.apache.logging.log4j.Level;
/**
 * @file
 * @author      Terry Ebdon
 * @date        March 2019
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

@groovy.util.logging.Log4j2
final class TorpedoRepositioner extends Repositioner {

  @Override
  protected void objectAtSector( final int subMoveNo, final int row, final int col ) {
    log.info logMoveStepImpact, id, subMoveNo, [row,col]

    assert thingMoved == Quadrant.Thing.torpedo
    thingHit = quadrant[row, col]

    log.printf Level.DEBUG,
      msgMoveBlocked, id, subMoveNo, thingHit, [row, col]

    /// @todo Localise the first agument, e.g. Thing.star
    final Quadrant.Thing thingHit = quadrant[row,col]
    ui.fmtMsg 'impactAtSector', [ thingHit, col, row ]

    if ( thingHit == Quadrant.Thing.base ) {
      ui.localMsg 'baseDestroyed'
    }
    ship.position.sector = [row, col]
    moveAborted = true
  }

  @groovy.transform.TypeChecked
  @Override void trackMove(
      final int subMoveNo, final int z1, final int z2 ) {
    super.trackMove subMoveNo, z1, z2
    ui.fmtMsg 'repositioner.position', constrain( [newX, newY] )
  }

  @groovy.transform.TypeChecked
  private List<Float> constrain( List<Float> coords ) {
    coords.collect { Float coord ->
      [coord, 8F].min()
    }
  }
}
