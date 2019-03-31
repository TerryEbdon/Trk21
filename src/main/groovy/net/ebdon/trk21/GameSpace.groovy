package net.ebdon.trk21;

import groovy.transform.TypeChecked;
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

@groovy.util.logging.Log4j2
abstract class GameSpace {
  static final int minCoord      = 1;
  static final int maxCoord      = 8;
  static final int boardSize     = (minCoord..maxCoord).size() ** 2;
  static final topLeftCoords     = [minCoord,minCoord];
  static final bottomRightCoords = [maxCoord,maxCoord];

  LinkedHashMap board = [:];

  static final float maxSectorDistance =
    distanceBetween( topLeftCoords, bottomRightCoords ); /// Sector diagonal

  def size() {
    assert validBoardSize
    boardSize
  }

  List<Integer> getRandomCoords() {
    Random rnd = new Random()
    [
      rnd.nextInt(maxCoord) + 1,
      rnd.nextInt(maxCoord) + 1
    ]
  }

  @TypeChecked
  private boolean isValidBoardSize() {
    board.size() == boardSize
  }

  @TypeChecked
  boolean isValidKey( final int row, final int col ) {
    board.keySet().contains( [row, col] )
  }

  boolean isValid() {
    def badQuadrant = board.keySet().find {
      !insideGalaxy( it[0..1] )
    } // sparse = false
    // assert null == badQuadrant
    log.debug "badQuadrant: $badQuadrant, $this"
    String diag = ""
    minCoord.upto(maxCoord) {
      diag += "[$it, $it]=${board[it,it]}, "
    }
    log.debug diag
    badQuadrant == null && board.size() == boardSize
  }

  String toString() {
    "board size: ${board.size()}"
  }
  // final def putAt( key, int value ) {
  //   board[key] = Thing.find { it.ordinal() == value }
  // }

  // final def putAt( key, Thing value ) {
  //   board[key] = value
  // }

  final def clear() {
    minCoord.upto(maxCoord) { r1 ->
      minCoord.upto(maxCoord) { r2 ->
        clearSquare r1, r2
      }
    }
  }

  abstract void clearSquare( final row, final col ) ;
  abstract int getCellPadding() ;
  abstract String symbol( final key ) ;

  def dump() {
    // log.debug "Dumping board:\n$board"
    // println "Dumping board:\n$board"
    minCoord.upto(maxCoord) { i ->
      def boardRow = ''
      minCoord.upto(maxCoord) { j ->
        // println "i,j: $i,$j"
        boardRow += "${symbol([i,j]).padLeft(cellPadding,'0')} "
      }
      log.info boardRow
    }
  }

  static boolean insideGalaxy( final coords ) {
    insideGalaxy( coords.first(), coords.last() )
  }

  static boolean insideGalaxy( final x, final y ) {
    contains( x, y )
  }

  static boolean contains( final x, final y ) {
    (minCoord..maxCoord).containsAll( x, y )
  }

  static def logFmtCoords( final x, final y ) {
    "${[x,y]} == $y - $x"
  }

  /// Distance between two sectors, calculated via Pythagorous
  static float distanceBetween(
      final coordsFrom,
      final coordsTo ) {

    assert coordsFrom && coordsTo
    assert coordsFrom != coordsTo

    assert sectorIsInsideQuadrant( coordsFrom )
    assert sectorIsInsideQuadrant( coordsTo )

    // println "Calculating distance between $coordsFrom and $coordsTo"
    final distance = Math.sqrt(
      ( coordsFrom.first() - coordsTo.first() ) ** 2 +
      ( coordsFrom.last() - coordsTo.last() ) ** 2 )

    // assert distance <= maxSectorDistance
    // println "Distance is $distance"
    distance
  }

  static boolean sectorIsInsideQuadrant( final coords ) {
    insideGalaxy( coords )
  }
}
