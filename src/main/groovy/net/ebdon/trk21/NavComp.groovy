package net.ebdon.trk21;

import org.apache.logging.log4j.Level;
/**
 * @file
 * @author      Terry Ebdon
 * @date        March 2019
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
@brief  Navigation Computer -- Used for 'cheat' functionality.
@author Terry Ebdon
@date   MAR-2019
*/
@groovy.transform.ToString(includePackage=false,includeNames=true)
@groovy.util.logging.Log4j2
@groovy.transform.Canonical
@groovy.transform.TypeChecked
final class NavComp {
  final static double maxCourse = 8d

  UiBase ui;
  final boolean   srSensorDamaged;
  final Position  shipPos;
  final Quadrant  quadrant;

  /**
   * Convert course in degrees to Trk21 course units
   * @param   courseDegrees degrees clockwise from North (0 = North)
   * @return  units anticlockwise from East (double), 8 units/circle (45° per
   * unit).
   * <p>
   * Examples: 0->3, 45->2, 90->1, 135->8, 180->7, 225->6, 270->5, 315->4
   * </p>
   */
  static Double degreesToCourse(final Double courseDegrees) {
    final double normalised = normalise(courseDegrees)
    double units = (90d - normalised) / 45d
    units = wrapCourseUnits(units)
    // snap exact integers to avoid floating point rounding issues
    final double snapped = Math.rint(units)
    if (Math.abs(units - snapped) < 1e-9) {
      units = snapped
    }
    ++units // Convert from [0, 8) to [1, 9)
  }

  /**
   * Normalise an angle in degrees to the range [0, 360).
   *
   * @param courseDegrees angle in degrees, clockwise from North
   * @return angle normalised to 0 (inclusive) .. 360 (exclusive)
   */
  static double normalise( final double courseDegrees) {
    final double circleDegs = 360d
    ((courseDegrees % circleDegs) + circleDegs) % circleDegs
  }

  /**
  * wrap into 0..7 e.g. 9 wraps to 1
  * @param Course units in multiples of 45 degrees
  * @return course in range [0..9)
  */
  static double wrapCourseUnits(double units) {
    ((units % maxCourse) + maxCourse) % maxCourse
  }

  /**
  * Returns firing angle in radians. 0 = north (up), increases clockwise.
  * @param ship Ship's sector as a Coords2d
  * @param enemy Enemy's sector as a Coords2d
  * @return course, from ship to enemy, in radians
  */
  static double firingAngleRadians(Coords2d ship, Coords2d enemy) {
    // dx: right positive, dy: up positive (convert row-down to y-up)
    double dx = enemy.col - ship.col
    double dy = ship.row - enemy.row
    double angle = Math.atan2(dx, dy)
    // note: parameters swapped to make 0 = north, clockwise positive
    if (angle < 0) {
      angle += 2.0 * Math.PI // normalize to [0, 2π)
    }
    angle
  }

  /**
  * Returns firing angle in degrees. 0 = north (up), increases clockwise.
  * @param ship Ship's sector as a Coord2d
  * @param enemy Enemy's sector as a Coord2d
  * @return course, from ship to enemy, in degrees
  */
  static double firingAngleDegrees(Coords2d ship, Coords2d enemy) {
    Math.toDegrees( firingAngleRadians(ship, enemy) )
  }

  void run () {
    ui.localMsg 'navComp.retrofit'
    if ( srSensorDamaged ) {
      ui.localMsg 'navComp.srSensor.offline'
    } else {
      scanFor()
    }
  }

  // Scan current quadrant, report matching objects
  void scanFor() {
    final String enemyAtSector = 'Enemy at sector %1$s - %2$s'

    quadrant.findEnemies().each { Coords2d enemyPos ->
      log.printf Level.INFO,enemyAtSector, enemyPos,"from ${shipPos.sector}"

      final double targetAngle = firingAngleDegrees(shipPos.sector, enemyPos)
      final double targetCourse = degreesToCourse(targetAngle)

      ui.fmtMsg 'navComp.enemyCourse',[enemyPos, targetCourse, ]
    }
  }
}
