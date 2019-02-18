package net.ebdon.trk21;

import static GameSpace.*;
import static Quadrant.*;
import static CourseOffset.*;
/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
 * @copyright
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
  final String msgArrivedInQuad     = 'Ship arrived in quadrant %s';
  final String msgArrivedInsector   = 'Ship arrived in sector   %s';
  final String msgReposInfo         = "Reposition from sector   %s with energy %d and offset %s";
  final String msgJumpFrom          = "Jumping from: quadrant:  %s";
  final String msgfloatQuadFormat   = "[${CourseOffset.format1}, ${CourseOffset.format1}]";
  final String msgJumpTo            = "jumping to:   quadrant: $msgfloatQuadFormat";
  final String msgJumpOffset        = "Jumping with offset: %s";
  final String msgJumpCoord         = "Jump: quadCoord: %d offset: ${CourseOffset.format1} sectCoord: %d";

  boolean moveAborted   = false;
  def           trek    = null;
  // ShipVector    sv      = null;
  CourseOffset  offset  = null;

  float newX; // Line 1840 Stat.2
  float newY; // Line 1840 Stat.3

  def startSector;

  Repositioner( t ) {
    assert t
    trek = t
  }

  String toString() {
    "moveAborted: $moveAborted, newX: $newX, newY: $newY startSector: $startSector\n" +
    "trek=   $trek\n" +
    // "sv=     $sv\n" +
    "offset= $offset"
  }

  void repositionShip( final ShipVector shipVector ) {
    newX = trek.entSectX // Line 1840 Stat.2
    newY = trek.entSectY // Line 1840 Stat.3
    startSector =[ newX, newY ]

    assert trek && shipVector
    moveAborted = false
    // sv = shipVector
    offset = new CourseOffset( shipVector ) // gosub 2300 @ line 1840
    log.info sprintf( msgReposInfo,
        startSector.toString(), trek.ship.energyUsedByLastMove, offset )
    log.debug "Before move: $this"
    trek.with {
      quadrant[ entSectX, entSectY ] = Thing.emptySpace  // Line 1840 Stat.1
      newX = entSectX // Line 1840 Stat.2
      newY = entSectY // Line 1840 Stat.3
    }

    // 1 unit of energy = 1 warp factor & moves ship 1... sector? or quadrant?
    // 1.upto( ship.energyUsedByLastMove ) { // for each sector traversed. L.1860
    for ( int it = 1; it <= trek.ship.energyUsedByLastMove && !moveAborted; ++it ) {
      moveSector it
    }

    trek.with {
      // (entSectX, entSectY) = [newX + 0.5, newY + 0.5]  // Line 1875
      quadrant[ entSectX, entSectY ] = Thing.ship // 1875
      log.info sprintf( msgArrivedInQuad,   logFmtCoords( entQuadX, entQuadY ) )
      log.info sprintf( msgArrivedInsector, logFmtCoords( entSectX, entSectY ) )
      // log.info "Ship arrived in quadrant ${logFmtCoords( entQuadX, entQuadY )}"
      // log.info "Ship arrived in sector   ${logFmtCoords( entSectX, entSectY )}"
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

  void moveSector( subMoveNo ) {
    assert offset
    int z1, z2 // sector arrived at?
    (z1,z2) = [(newX += offset.x) + 0.5, (newY += offset.y) + 0.5] // Line 1860

    if ( trek.quadrant.contains( z1, z2 ) ) { // Line 1870
      if ( trek.quadrant.isOccupied( z1, z2 ) ) { // Line 1870.2
        objectAtSector subMoveNo, z1, z2
        newX -= offset.x
        newY -= offset.y
      } else {
        trek.entSectX = z1
        trek.entSectY = z2
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
        // z1 = newX = newCoordIfOutsideQuadrant( sv, entSectX, offset.x ) // 1920, 1925 stat 1
        // z2 = newY = newCoordIfOutsideQuadrant( sv, entSectY, offset.y ) // 1920, 1925 stat 2

        // (z1,z2,newX,newY) = newCoordIfOutsideQuadrant() // 1920, 1925 stat 2
        (entQuadX,entQuadY) = newCoordIfOutsideQuadrantV2() // 1920, 1925 stat 2

        // entSectX  = bounceToSectCoord( newX, z1 ) // Line 1925, stats 3.
        // entSectY  = bounceToSectCoord( newY, z2 ) // Line 1925, stats 4.

        log.info sprintf( msgTryToEnterQuad, subMoveNo, logFmtCoords( entQuadX,entQuadY ) )
        log.info sprintf( msgTryToEnterSect, subMoveNo, logFmtCoords( entSectX,entSectY ) )
        // log.info "Ship move part $subMoveNo - try to enter quadrant ${[z1,z2]} == $z2 - $z1"
        // (entQuadX,entQuadY) = constrainCoords( [z1, z2] ) // 1930 & 1940 -- Can't leave galaxy.
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
    log.info sprintf( msgJumpFrom, logFmtCoords( trek.entQuadX, trek.entQuadY ) )
    log.info sprintf( msgJumpOffset, offset )
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

  // static float newCoordIfOutsideQuadrant( final ShipVector sv, final coord, final offset ) {
  //   coord + sv.warpFactor * offset + ( coord -0.5 ) / 8 // Line 1920
  // }
}
