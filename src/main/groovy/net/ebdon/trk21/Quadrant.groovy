package net.ebdon.trk21;

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
final class Quadrant extends GameSpace {

  enum Thing {
    emptySpace( '.' ),
    ship(       'E' ),
    enemy(      'K' ),
    base(       'B' ),
    star(       '*' ),
    torpedo(    't' )

    private final List<Integer> multipliers = [0, 0, 100, 10, 1, 0]
    final String symbol;
    Thing( final String sym ) { symbol = sym }
    int getMultiplier() { multipliers[ordinal()] }
  }

  int getCellPadding() {
    1
  }

  String symbol( final List<Integer> key ) {
    board.get( key ).symbol
  }

  @groovy.transform.TypeChecked
  boolean isOccupied( final int i, final int j ) {
    board[i,j] != Thing.emptySpace
  }

  @groovy.transform.TypeChecked
  final Thing getAt( final Coords2d c2d ) {
    board.get c2d.toList()
  }

  @groovy.transform.TypeChecked
  final Thing getAt( List<Integer> key ) {
    board.get key
  }

  final def putAt( key, Thing value ) {
    board[key] = value
  }

  final def putAt( final Coords2d coords, Thing value ) {
    board[coords.row,coords.col] = value
  }

  @groovy.transform.TypeChecked
  final void clearSquare( final int row, final int col ) {
    board[row,col] = Thing.emptySpace //.ordinal()
  }

  /// @bug Infinite loop is possible.
  /// @todo add a time-out.
  List<Integer> getEmptySector() {
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
    objects present. This is the top-half of the display generated by
    Trek.shortRangeScan(). It's also used for logging in Trek.positionEnemy()
    during the sector setup.

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

  Map<List<Integer>,Thing> findEnemies() {
    board.findAll { it.value == Thing.enemy }
  }

  def removeEnemy( key ) {
    assert board[key] == Thing.enemy || board[key] == Thing.torpedo
    clearSquare key
  }

  int count( Closure closure ) {
    board.count closure
  }
}
