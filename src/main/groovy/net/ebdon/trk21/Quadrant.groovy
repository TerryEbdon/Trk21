package net.ebdon.trk21;
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
 
@groovy.util.logging.Log4j2
final class Quadrant extends GameSpace {

  enum Thing {
    emptySpace( '.' ),
    ship(       'E' ),
    enemy(      'K' ),
    base(       'B' ),
    star(       '*' )

    final char symbol;
    Thing( sym ) { symbol = sym }
  }

  int getCellPadding() {
    1
  }

  String symbol( final key ) {
    // println "get key: $key"
    // log.debug "get key: $key"
    board.get( key ).symbol
  }

  boolean isOccupied( i, j ) {
    board[i,j] != Thing.emptySpace
  }

  final Thing getAt( key ) {
    board.get key
  }

  final def putAt( key, Thing value ) {
    board[key] = value
  }

  final def putAt( final Coords2d coords, Thing value ) {
    board[coords.row,coords.col] = value
  }

  final void clearSquare( final row, final col ) {
    board[row,col] = Thing.emptySpace //.ordinal()
  }

  /// @bug Infinite loop is possible.
  /// @todo add a time-out.
  def getEmptySector() {
    int r1
    int r2
    (r1,r2) = getRandomCoords()
    log.trace "getEmptySector() starts with r1:$r1, r2:$r2 = ${board[r1,r2]} board size is ${board.size()}"
    while ( isOccupied(r1,r2) ) {
       (r1,r2) = getRandomCoords()
       log.trace "Is ${[r1,r2]} empty?"
    }
    log.trace "getEmptySector() ends with r1:$r1, r2:$r2 = ${board[r1,r2]}"
    [r1,r2]
  }

  /**
    Output the sectors of the current quadrant, showing the position of all game
    objects present. This is the top-half of the display generated by a
    shortRangeScan(). it's also used for logging during in positionKlingons()
    durimg the sector setup.

    @arg linePrinter - A closure that will be called to output each line of the
    sector display.
  */
  def displayOn( Closure linePrinter ) {
    1.upto(8) { i ->
      String scanLine = ''
      1.upto(8) { j ->
        scanLine += "${symbol([i,j])} "
      }
      linePrinter( scanLine )
    }
  }

  int count( Closure closure ) {
    board.count closure
  }
}
