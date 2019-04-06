package net.ebdon.trk21;

import static GameSpace.*;
import static Quadrant.*;
import static CourseOffset.*;
import org.apache.logging.log4j.Level;
/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
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

/// @todo Move log format strings into configuration.
@groovy.util.logging.Log4j2
abstract class Repositioner {
  final String msgMovePart          = '%s move part %2d -';
  final String msgMoveBlocked       = "$msgMovePart object %s at sector %s";
  final String msgCrossQuadEdge     = "$msgMovePart crossing quadrant edge at %s";
  final String msgInSector          = "$msgMovePart in sector %s, ofs: ${CourseOffset.format2}";
  final String msgTryToEnterQuad    = "$msgMovePart try to enter quadrant   %s";
  final String msgTryToEnterSect    = "$msgMovePart try to enter sector     %s";
  final String msgConstrainedToQuad = "$msgMovePart constrained to quadrant %s";
  final String msgConstrainedToSect = "$msgMovePart constrained to sector   %s";
  final String msgArrivedInQuad     = '{} arrived in quadrant {}';
  final String msgArrivedInsector   = '{} arrived in sector   {}';
  final String msgReposInfo         = 'Reposition from sector   {} with energy {} and offset {}';
  final String msgJumpFrom          = '{} jumping from: quadrant:  {}';
  final String msgfloatQuadFormat   = "[${CourseOffset.format1}, ${CourseOffset.format1}]";
  final String msgJumpTo            = "%s jumping to:   quadrant: $msgfloatQuadFormat";
  final String msgJumpOffset        = '{} Jumping with offset: {}';
  final String msgJumpCoord         = "%s jump: quadCoord: %d offset: ${CourseOffset.format1} sectCoord: %d";
  final String logMoveStepImpact    = '{} move step {} impact at {}'


  boolean moveAborted   = false;

  protected CourseOffset  offset  = null;
  protected float newX; // Line 1840 Stat.2
  protected float newY; // Line 1840 Stat.3
  protected Coords2d startSector;
  protected Quadrant.Thing thingMoved;
  protected Quadrant.Thing thingHit = Thing.emptySpace;
  protected String id; ///< Id of the Moveable instance that's being repositioned.

  Moveable ship;  // For position and energyUsedByLastMove
  UiBase ui;      // To replace Trek.blockedAtSector()
  def quadrant;   // To replace trek.quadrant.

  private boolean tracked;
  private int energyBudget;

  String toString() {
    "moveAborted: $moveAborted, newX: $newX, newY: $newY startSector: $startSector\n" +
    "Moving thing: $thingMoved"
    "offset= $offset"
  }

  void repositionShip( final ShipVector shipVector ) {
    id = ship?.id
    energyBudget = ship?.energyUsedByLastMove
    assert energyBudget > 0
    tracked = ship.tracked
    startSector = ship.position.sector
    newX = startSector.row // Line 1840 Stat.2
    newY = startSector.col // Line 1840 Stat.3
    thingMoved = quadrant[ startSector.toList() ]

    log.debug "quadrant: $quadrant"
    log.debug "quadrant${startSector.toList()}: ${quadrant[*(startSector.toList())]}"
    log.debug "startSector: $startSector"
    moveAborted = false

    offset = new CourseOffset( shipVector ) // gosub 2300 @ line 1840
    log.debug msgReposInfo, startSector, energyBudget, offset
    log.debug "Before move: $this"
    quadrant[ship.position.sector] = Thing.emptySpace  // Line 1840 Stat.1

    // 1 unit of energy = 0.125 warp factor & moves ship 1 sector
    for ( int it = 1; !moveAborted && it <= energyBudget; ++it ) {
      moveSector it   // for each sector traversed. L.1860
    }

    final shipPos = ship.position
    assert thingMoved
    quadrant[ shipPos.sector ] = thingMoved // 1875
    log.info msgArrivedInQuad,   id, shipPos.quadrant
    log.info msgArrivedInsector, id, shipPos.sector
    log.debug "After move: $this"
  }

  abstract protected void objectAtSector( final int subMoveNo, final int row, final int col );

  private void moveSector( final int subMoveNo ) {
    assert offset
    int z1, z2 // sector arrived at?
    (z1,z2) = [(newX += offset.x) + 0.5, (newY += offset.y) + 0.5] // Line 1860

    if ( quadrant.contains( z1, z2 ) ) { // Line 1870
      trackMove subMoveNo, z1, z2
      if ( quadrant.isOccupied( z1, z2 ) ) { // Line 1870.2
        objectAtSector subMoveNo, z1, z2
      } else {
        ship.position.sector = [z1, z2]
      }
    } else { // Line 1920 - 1925
      hitQuadrantEdge subMoveNo, z1, z2
    }
  }

  @groovy.transform.TypeChecked
  private void hitQuadrantEdge( final int subMoveNo, final int z1, final int z2 ) {
    log.printf Level.INFO, msgCrossQuadEdge, id, subMoveNo, [z1, z2]
    moveAborted = true
    log.info "Resetting sector to $startSector for $id"
    ship.position.sector = startSector

    jumpQuadrant()
    ship.position.with {
      // entSectX  = bounceToSectCoord( newX, z1 ) // Line 1925, stats 3.
      // entSectY  = bounceToSectCoord( newY, z2 ) // Line 1925, stats 4.

      log.printf Level.INFO, msgTryToEnterQuad, id, subMoveNo, quadrant
      log.printf Level.INFO, msgTryToEnterSect, id, subMoveNo, sector

      constrain() // 1930 & 1940
      log.printf Level.INFO, msgConstrainedToQuad, id, subMoveNo, quadrant
      log.printf Level.INFO, msgConstrainedToSect, id, subMoveNo, sector
    }
  }

  private void jumpQuadrant() {
    ship.position.quadrant.with {
      log.info "Resetting quadrant for $id"
      (row,col) = newCoordIfOutsideQuadrantV2() // 1920, 1925 stat 2
    }
  }

  @groovy.transform.TypeChecked
  private void trackMove(
    final int subMoveNo, final int z1, final int z2 ) {
    log.printf Level.DEBUG,
        msgInSector, id, subMoveNo, [z1, z2], newX, newY
    if ( tracked ) {
      ui.fmtMsg 'repositioner.position', [newX, newY]
    }
  }

  /// @arg compoundCoord - float of form q.sss where q is a quadrantt No.
  ///   and sss is the sector expressed as a fraction of a quadrant.
  ///   e.g. 1.125 is quadrant 1, sector 1
  /// @arg quadrantCoord - The quadrant coord as an integer.
  /// @return A sector coordinate
  /// @bug The `+ 0.5`, to round, is pointles as integer conversion truncates.
  static int bounceToSectCoord( float compoundCoord, int quadrantCoord ) {
    maxCoord * ( compoundCoord - quadrantCoord ) + 0.5F  // Line 1925, stats 3 and 4.
  }

  /// @returns:
  /// Returns the new coordinates as floats of the form:
  /// `q.sss` where `q` is the new quadrant and `sss` is the sector,
  /// expressed as eights of a quadrant. e.g. 2.125 means 2 quadrants and 1
  /// sector.
  List<Float> newCoordIfOutsideQuadrantV2() {
    log.trace msgJumpFrom, id, ship.position.quadrant
    log.info  msgJumpOffset, id, offset
    List rQuadCoords = []
    final float warpFactor = offset.shipVector.warpFactor
    [ [ship.position.quadrant.row, offset.x, ship.position.sector.row],
      [ship.position.quadrant.col, offset.y, ship.position.sector.col]
    ].each { int quadCoord, float offsetCoord, int sectCoord -> // Line 1920
      log.printf Level.DEBUG, msgJumpCoord, id, quadCoord, offsetCoord, sectCoord
      rQuadCoords << quadCoord + warpFactor * offsetCoord + (sectCoord - 0.5F) / 8F
    }
    log.printf( Level.INFO, msgJumpTo, id, *rQuadCoords )
    rQuadCoords
  }
}
