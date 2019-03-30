package net.ebdon.trk21;

import static GameSpace.*;
import static Quadrant.*;
import static CourseOffset.*;
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
/// @todo Replace sprintf() with Log4j2 formatting.
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
  final String msgReposInfo         = "Reposition from sector   {} with energy {} and offset {}";
  final String msgJumpFrom          = "Jumping from: quadrant:  {}";
  final String msgfloatQuadFormat   = "[${CourseOffset.format1}, ${CourseOffset.format1}]";
  final String msgJumpTo            = "jumping to:   quadrant: $msgfloatQuadFormat";
  final String msgJumpOffset        = "Jumping with offset: {}";
  final String msgJumpCoord         = "Jump: quadCoord: %d offset: ${CourseOffset.format1} sectCoord: %d";

  boolean moveAborted   = false;
  def           trek    = null;
  CourseOffset  offset  = null;

  float newX; // Line 1840 Stat.2
  float newY; // Line 1840 Stat.3

  def startSector;
  def ship;

  Repositioner( t = null ) {
    // assert t
    trek = t
  }

  String toString() {
    "moveAborted: $moveAborted, newX: $newX, newY: $newY startSector: $startSector\n" +
    "trek=   $trek\n" +
    "offset= $offset"
  }

  void repositionShip( final ShipVector shipVector ) {
    ship = trek.ship

    newX = ship.position.sector.row // Line 1840 Stat.2
    newY = ship.position.sector.col // Line 1840 Stat.3
    startSector = [ newX, newY ]

    moveAborted = false

    offset = new CourseOffset( shipVector ) // gosub 2300 @ line 1840
    log.info msgReposInfo,
        startSector.toString(), ship.energyUsedByLastMove, offset
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

  private void objectAtSector( subMoveNo, row, col ) {
    log.debug sprintf(
      msgMoveBlocked, subMoveNo,
      trek.quadrant[row,col], logFmtCoords( row, col ) )
    trek.blockedAtSector row, col
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
        ship.position.sector.row = z1
        ship.position.sector.col = z2
        log.debug sprintf(
            msgInEmptySector, subMoveNo,
            logFmtCoords( z1, z2 ), newX, newY )
      }
    } else { // Line 1920 - 1925
      log.info sprintf( msgCrossQuadEdge, subMoveNo, logFmtCoords( z1, z2 ) )
      // log.info "Ship move part $subMoveNo - crossing quadrant edge at ${logFmtCoords( z1, z2 )}"
      moveAborted = true
      trek.with {
        log.info "Resetting sector to $startSector"
        (entSectX,entSectY) = startSector
        (entQuadX,entQuadY) = newCoordIfOutsideQuadrantV2() // 1920, 1925 stat 2

        // entSectX  = bounceToSectCoord( newX, z1 ) // Line 1925, stats 3.
        // entSectY  = bounceToSectCoord( newY, z2 ) // Line 1925, stats 4.

        log.info sprintf( msgTryToEnterQuad, subMoveNo, logFmtCoords( entQuadX,entQuadY ) )
        log.info sprintf( msgTryToEnterSect, subMoveNo, logFmtCoords( entSectX,entSectY ) )

        (entQuadX,entQuadY) = constrainCoords( [entQuadX,entQuadY] ) // 1930 & 1940 -- Can't leave galaxy.
        (entSectX,entSectY) = constrainCoords( [entSectX,entSectY] ) // 1930 & 1940 -- Can't leave galaxy.
        log.info sprintf( msgConstrainedToQuad, subMoveNo, logFmtCoords( entQuadX,entQuadY ) )
        log.info sprintf( msgConstrainedToSect, subMoveNo, logFmtCoords( entSectX,entSectY ) )
        // log.info "Ship move part $subMoveNo - constrained to quadrant ${[z1,z2]} == $z2 - $z1"
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

  /// Returns the new coordinates as floats of the form:
  /// `q.sss` where `q` is the new quadrant and `sss` is the sector,
  /// expressed as eights of a quadrant. e.g. 2.125 means 2 quadrants and 1
  /// sector.
  // def newCoordIfOutsideQuadrant() {
  //   def rv = []
  //   [ [trek.entQuadX, offset.x, trek.entSectX],
  //     [trek.entQuadY, offset.y, trek.entSectY]
  //   ].each { quadCoord, offset, sectCoord -> // Line 1920
  //     rv << quadCoord + sv.warpFactor * offset + ( sectCoord -0.5 ) / 8
  //   }
  //   rv * 2
  // }

  /// Returns:
  ///  [quadrantRow,quadrantCol.sectorRow,sectorCol]
  def newCoordIfOutsideQuadrantV2() {
    log.info msgJumpFrom, logFmtCoords( trek.entQuadX, trek.entQuadY )
    log.info msgJumpOffset, offset
    def rQuadCoords = []
    final def warpFactor = offset.shipVector.warpFactor
    // def rSectCoords = []
    [ [trek.entQuadX, offset.x, trek.entSectX],
      [trek.entQuadY, offset.y, trek.entSectY]
    ].each { quadCoord, offsetCoord, sectCoord -> // Line 1920
      // log.debug "quadCoord: $quadCoord warp: ${sv.warpFactor} offset: $offset sectCoord: $sectCoord"
      log.debug sprintf( msgJumpCoord, quadCoord, offsetCoord, sectCoord )
      rQuadCoords << quadCoord + warpFactor * offsetCoord + (sectCoord - 0.5) / 8
      // rSectCoords << ( sectCoord -0.5 ) / 8
    }
    log.info sprintf( msgJumpTo, *rQuadCoords )
    rQuadCoords
  }
}
