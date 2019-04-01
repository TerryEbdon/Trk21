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
final class Repositioner {
  final String msgMovePart          = 'Ship move part %2d -';
  final String msgMoveBlocked       = "$msgMovePart object %s at sector %s";
  final String msgCrossQuadEdge     = "$msgMovePart crossing quadrant edge at %s";
  final String msgInEmptySector     = "$msgMovePart in empty sector %s, NewX/Y: ${CourseOffset.format2}";
  final String msgTryToEnterQuad    = "$msgMovePart try to enter quadrant   %s";
  final String msgTryToEnterSect    = "$msgMovePart try to enter sector     %s";
  final String msgConstrainedToQuad = "$msgMovePart constrained to quadrant %s";
  final String msgConstrainedToSect = "$msgMovePart constrained to sector   %s";
  final String msgArrivedInQuad     = 'Ship arrived in quadrant {}';
  final String msgArrivedInsector   = 'Ship arrived in sector   {}';
  final String msgReposInfo         = 'Reposition from sector   {} with energy {} and offset {}';
  final String msgJumpFrom          = 'Jumping from: quadrant:  {}';
  final String msgfloatQuadFormat   = "[${CourseOffset.format1}, ${CourseOffset.format1}]";
  final String msgJumpTo            = "jumping to:   quadrant: $msgfloatQuadFormat";
  final String msgJumpOffset        = 'Jumping with offset: {}';
  final String msgJumpCoord         = "Jump: quadCoord: %d offset: ${CourseOffset.format1} sectCoord: %d";

  boolean moveAborted   = false;
  CourseOffset  offset  = null;

  float newX; // Line 1840 Stat.2
  float newY; // Line 1840 Stat.3

  Coords2d startSector;
  def trek;     // For quadrant and blockedAtSector()
  def ship;     // For position and energyUsedByLastMove
  UiBase ui;    // To replace Trek.blockedAtSector()
  def quadrant; // To replace trek.quadrant.

  Repositioner( t = null ) {
    // assert t
    trek = t
  }

  String toString() {
    "moveAborted: $moveAborted, newX: $newX, newY: $newY startSector: $startSector\n" +
    // "trek=   ${trek?.toString() ?: '???'}\n" +
    "offset= $offset"
  }

  void repositionShip( final ShipVector shipVector ) {
    if ( ship == null ) {
      ship      = trek.ship
      quadrant  = trek.quadrant
      ui        = trek.ui
    }

    startSector = ship.position.sector
    newX = startSector.row // Line 1840 Stat.2
    newY = startSector.col // Line 1840 Stat.3

    moveAborted = false

    offset = new CourseOffset( shipVector ) // gosub 2300 @ line 1840
    log.info msgReposInfo, startSector, ship.energyUsedByLastMove, offset
    log.debug "Before move: $this"
    ship.position.sector.with {
      trek.quadrant[row, col] = Thing.emptySpace  // Line 1840 Stat.1
    }

    // 1 unit of energy = 1 warp factor & moves ship 1 quadrant?
    for ( int it = 1; !moveAborted && it <= ship.energyUsedByLastMove; ++it ) {
      moveSector it   // for each sector traversed. L.1860
    }

    ship.position.with {
      trek.quadrant[ sector.row, sector.col ] = Thing.ship // 1875
      log.info msgArrivedInQuad,   quadrant
      log.info msgArrivedInsector, sector
    }
    log.debug "After move: $this"
  }

  private void objectAtSector( final int subMoveNo, final int row, final int col ) {
    log.info 'Move step {} blocked at {}', subMoveNo, [row,col]
    log.printf Level.DEBUG,
      msgMoveBlocked, subMoveNo, trek.quadrant[row,col], [row, col]

    /// @todo Localise the first agument, e.g. Thing.star
    ui.fmtMsg 'blockedAtSector', [ quadrant[row,col], col, row ]
    moveAborted = true
  }

  private void moveSector( final int subMoveNo ) {
    assert offset
    int z1, z2 // sector arrived at?
    (z1,z2) = [(newX += offset.x) + 0.5, (newY += offset.y) + 0.5] // Line 1860

    if ( trek.quadrant.contains( z1, z2 ) ) { // Line 1870
      if ( trek.quadrant.isOccupied( z1, z2 ) ) { // Line 1870.2
        objectAtSector subMoveNo, z1, z2
        newX -= offset.x
        newY -= offset.y
      } else {
        ship.position.sector = [z1, z2]
        log.printf Level.DEBUG,
            msgInEmptySector, subMoveNo, [z1, z2], newX, newY
      }
    } else { // Line 1920 - 1925
      log.printf Level.INFO, msgCrossQuadEdge, subMoveNo, [z1, z2]
      moveAborted = true
      log.info "Resetting sector to $startSector"
      ship.position.sector = startSector

      ship.position.with {
        quadrant.with {
          log.info 'Resetting quadrant'
          (row,col) = newCoordIfOutsideQuadrantV2() // 1920, 1925 stat 2
        }
        // entSectX  = bounceToSectCoord( newX, z1 ) // Line 1925, stats 3.
        // entSectY  = bounceToSectCoord( newY, z2 ) // Line 1925, stats 4.

        log.printf Level.INFO, msgTryToEnterQuad, subMoveNo, quadrant
        log.printf Level.INFO, msgTryToEnterSect, subMoveNo, sector
      }

      ship.position.with {
        constrain() // 1930 & 1940
        log.printf Level.INFO, msgConstrainedToQuad, subMoveNo, quadrant
        log.printf Level.INFO, msgConstrainedToSect, subMoveNo, sector
      }
    }
  }

  /// @arg compoundCoord - float of form q.sss where q is a quadrantt No.
  ///   and sss is the sector expressed as a fraction of a quadrant.
  ///   e.g. 1.125 is quadrant 1, sector 1
  /// @arg quadrantCoord - The quadrant coord as an integer.
  /// @return A sector coordinate
  /// @bug The `+ 0.5`, to round, is pointles as integer conversion truncates.
  static int bounceToSectCoord( float compoundCoord, int quadrantCoord ) {
    maxCoord * ( compoundCoord - quadrantCoord ) + 0.5  // Line 1925, stats 3 and 4.
  }

  /// @returns:
  /// Returns the new coordinates as floats of the form:
  /// `q.sss` where `q` is the new quadrant and `sss` is the sector,
  /// expressed as eights of a quadrant. e.g. 2.125 means 2 quadrants and 1
  /// sector.
  List<Float> newCoordIfOutsideQuadrantV2() {
    log.info msgJumpFrom, ship.position.quadrant
    log.info msgJumpOffset, offset
    List rQuadCoords = []
    final float warpFactor = offset.shipVector.warpFactor
    [ [ship.position.quadrant.row, offset.x, ship.position.sector.row],
      [ship.position.quadrant.col, offset.y, ship.position.sector.col]
    ].each { int quadCoord, float offsetCoord, int sectCoord -> // Line 1920
      log.printf Level.DEBUG, msgJumpCoord, quadCoord, offsetCoord, sectCoord
      rQuadCoords << quadCoord + warpFactor * offsetCoord + (sectCoord - 0.5F) / 8F
    }
    log.printf( Level.INFO, msgJumpTo, *rQuadCoords )
    rQuadCoords
  }
}
