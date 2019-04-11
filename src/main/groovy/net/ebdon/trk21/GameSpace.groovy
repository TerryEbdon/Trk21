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
  static final Coords2d topLeftCoords     = [minCoord,minCoord];
  static final Coords2d bottomRightCoords = [maxCoord,maxCoord];

  Map board = [:];

  static final float maxSectorDistance =
    distanceBetween( topLeftCoords, bottomRightCoords ); /// Sector diagonal

  @TypeChecked
  static boolean insideGalaxy( final Coords2d coords ) {
    insideGalaxy( coords.first(), coords.last() )
  }

  @TypeChecked
  static boolean insideGalaxy( final int x, final int y ) {
    contains( x, y )
  }

  @TypeChecked
  static boolean contains( final Coords2d c2d ) {
    contains c2d.row, c2d.col
  }

  @TypeChecked
  static boolean contains( final int x, final int y ) {
    (minCoord..maxCoord).containsAll( x, y )
  }

  @TypeChecked
  static String logFmtCoords( final int x, final int y ) {
    "${[x,y]} == $y - $x"
  }

  /// Distance between two sectors, calculated via Pythagorous
  @TypeChecked
  static float distanceBetween(
      final Coords2d coordsFrom,
      final Coords2d coordsTo ) {

    assert coordsFrom != coordsTo

    assert sectorIsInsideQuadrant( coordsFrom )
    assert sectorIsInsideQuadrant( coordsTo )

    final int rowSeparation = coordsFrom.row - coordsTo.row
    final int colSeparation = coordsFrom.col - coordsTo.col
    Math.sqrt( ( rowSeparation ** 2.0 + colSeparation ** 2.0 ).toDouble() )
  }

  @TypeChecked
  static boolean sectorIsInsideQuadrant( final Coords2d coords ) {
    insideGalaxy( coords )
  }

  static boolean sectorIsInsideQuadrant( final List<Integer> coords ) {
    insideGalaxy( *coords )
  }

  @TypeChecked
  int size() {
    assert validBoardSize
    boardSize
  }

  @TypeChecked
  @SuppressWarnings('InsecureRandom')
  List<Integer> getRandomCoords() {
    Random rnd = new Random()
    [
      rnd.nextInt(maxCoord) + 1,
      rnd.nextInt(maxCoord) + 1
    ]
  }

  @TypeChecked
  private boolean isValidBoardSize() {
    board?.size() == boardSize
  }

  @TypeChecked
  boolean isValidKey( final int row, final int col ) {
    board.keySet().contains( [row, col] )
  }

  boolean isValid() {
    def badQuadrant = board.keySet().find {
      !insideGalaxy( *(it[0..1]) )
    } // sparse = false
    // assert null == badQuadrant
    log.debug "badQuadrant: $badQuadrant, $this"
    String diag = ''
    minCoord.upto(maxCoord) {
      diag += "[$it, $it]=${board[it,it]}, "
    }
    log.debug diag
    badQuadrant == null && board.size() == boardSize
  }

  @TypeChecked
  String toString() {
    "board size: ${board.size()}"
  }
  // final def putAt( key, int value ) {
  //   board[key] = Thing.find { it.ordinal() == value }
  // }

  // final def putAt( key, Thing value ) {
  //   board[key] = value
  // }

  final void clear() {
    minCoord.upto(maxCoord) { r1 ->
      minCoord.upto(maxCoord) { r2 ->
        clearSquare r1, r2
      }
    }
  }

  abstract void clearSquare( final int row, final int col );
  abstract int getCellPadding();
  abstract String symbol( final List<Integer> key );

  void dump() {
    minCoord.upto(maxCoord) { i ->
      def boardRow = ''
      minCoord.upto(maxCoord) { j ->
        boardRow += "${symbol([i,j]).padLeft(cellPadding,'0')} "
      }
      log.info boardRow
    }
  }
}
