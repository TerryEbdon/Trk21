package net.ebdon.trk21;

import groovy.transform.*;
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
@ToString(includePackage=false,includeNames=true,excludes='valid')
@AutoClone
@Canonical
final class Coords2d {
  int row; // = -1
  int col; // = -1

  boolean isValid() {
    row > 0 && col > 0
  }

  final int first() { row } /// @todo work-around for GameSpace.distanceBetween()
  final int last()  { col } /// @todo work-around for GameSpace.distanceBetween()

  // String toString() {
  //   "row: $row, col: $col"
  // }
}
