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

/**
When the ship moves diagonally the X offset any Y offset will differ depending
on the angle, the value in ShipVector#course. The X and Y offsets are
determined via trigonometry. [Konstantin
Serov](https://tutsplus.com/authors/konstantin-serov) walks through the details
in this tuts+ article: [Quick Tip: Trigonometry for Flash Game
Developers](https://code.tutsplus.com/tutorials/quick-tip-trigonometry-for-flash-game-developers--active-4458)
 */
@groovy.util.logging.Log4j2
@groovy.transform.TypeChecked
final class CourseOffset {
  final ShipVector shipVector;
  float x;
  float y;
  static final int precision = 6;
  static final String format1 = "%+1.${precision}f";
  static final String format2 = "x: $format1, y: $format1";

  CourseOffset( final ShipVector sv ) {
    shipVector = sv
    final float radians = courseAsRadians()
    x = -Math.sin( radians ).toFloat() /// Negative as rows start at top.
    y =  Math.cos( radians ).toFloat()
  }

  String toString() {
    // "x: $x, y: $y, sv= $shipVector"
    sprintf "$format2, sv= %s", x, y, shipVector
  }

  /**
   The coordinate system assumes 0 degress == 'East'. One 'course unit' is
   equivalent to -45 degrees, so the compass works counter-clockwise relative to
   East.

   |Course|Degrees|
   |:----:|------:|
   |   1  |     0 |
   |   2  |   -45 |
   |   3  |   -90 |
   |   4  |  -135 |
   |   5  |  -180 |
   |   6  |  -225 |
   |   7  |  -270 |
   |   8  |  -315 |

   */
  private float courseAsRadians() {
    final float multiplesOf45Degrees = shipVector.course - 1F
    final float degrees = 45F * multiplesOf45Degrees
    log.debug  "course ${shipVector.course} is $multiplesOf45Degrees * 45 = $degrees degrees"
    Math.toRadians( degrees ).toFloat()
  }
}
