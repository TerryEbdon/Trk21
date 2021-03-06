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

/**
@brief  ShipVector holds details of the ship's direction and speed.
@author Terry Ebdon
@date   JAN-2019
*/
@groovy.transform.TypeChecked
final class ShipVector {
  static final float minCourse = 1F;
  static final float maxCourse = 8.999999F;
  static final float minWarpFactor = 0.125F;
  static final float maxWarpFactor = 12F;

  float course        = 0F;
  float warpFactor    = 0F;

  static boolean isValidCourse( final float newCourse ) {
    newCourse >= minCourse && newCourse <= maxCourse
  }

  static boolean isValidWarpFactor( final float newWarpFactor ) {
    newWarpFactor >= minWarpFactor && newWarpFactor <= maxWarpFactor
  }

  boolean isValid() {
    isValidCourse( course ) &&
    isValidWarpFactor( warpFactor )
  }

  String toString() {
      "course: $course, warpFactor: $warpFactor"
  }
}
