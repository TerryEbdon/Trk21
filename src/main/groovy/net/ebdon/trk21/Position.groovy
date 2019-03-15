package net.ebdon.trk21;

import groovy.transform.*;
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

@AutoClone
@Canonical @TypeChecked
@ToString(includePackage=false,includeNames=true)
class Position {
  Coords2d quadrant = new Coords2d();  ///< Q1% and Q2% in TREK.BAS
  Coords2d sector   = new Coords2d();  ///< S1% and S2% in TREK.BAS

  boolean isValid() {
    quadrant.isValid() && sector.isValid()
  }

}
