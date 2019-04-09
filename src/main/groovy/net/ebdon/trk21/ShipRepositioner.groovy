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
final class ShipRepositioner extends Repositioner {

  /// @todo quadrant should not be a dynamic member.
  @Override
  // @groovy.transform.TypeChecked
  protected void objectAtSector( final int subMoveNo, final int row, final int col ) {
    log.info logMoveStepImpact, id, subMoveNo, [row,col]

    assert thingMoved == Quadrant.Thing.ship
    final String msgId = 'blockedAtSector'

    log.printf Level.DEBUG,
      msgMoveBlocked, id, subMoveNo, quadrant[row,col], [row, col]

    /// @todo Localise the first agument, e.g. Thing.star
    final Quadrant.Thing thingHit = quadrant[row,col]
    ui.fmtMsg msgId, [ thingHit, col, row ]

    newX -= offset.x
    newY -= offset.y
    moveAborted = true
  }
}
