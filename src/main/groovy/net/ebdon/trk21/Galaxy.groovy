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

final class Galaxy extends GameSpace {
  int getCellPadding() {
    3
  }

  @TypeChecked
  final String symbol( final List<Integer> key ) {
    board[key]
  }

  // @TypeChecked
  final int getAt( key ) {
    board.get key
  }

  // @TypeChecked
  final int getAt( final Coords2d keyCoords) {
    assert size()
    assert keyCoords?.valid
    board.get keyCoords.toList()
  }

  @TypeChecked
  final List<Integer>putAt( final Coords2d c2dKey, value ) {
    final List<Integer> key = c2dKey.toList()
    board[key] = value
    key
  }

  @TypeChecked
  final List<Integer> putAt( final List<Integer>key, final int value ) {
    assert key.size() == 2
    board[key] = value
    key
  }

  final void clearSquare( final Coords2d squareCoords ) {
    this[squareCoords] = 0
  }

  @TypeChecked
  String scan( final Coords2d c2d ) {
    scan c2d.row, c2d.col
  }

  @TypeChecked
  String scan( final int row, final int col ) {
    boolean inside = insideGalaxy( row, col )
    assert !inside || isValidKey( row, col )
    inside ? sprintf( '%03d', board[row, col] ) : '000'
  }
}
