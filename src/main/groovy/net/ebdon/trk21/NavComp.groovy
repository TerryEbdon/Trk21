package net.ebdon.trk21;

import java.lang.Math;
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
  UiBase ui;
  final boolean   srSensorDamaged;
  final Position  shipPos;
  final Quadrant  quadrant;

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
    final String enemyAtRelSect= 'Enemy at relative sector %1$s'
    final String enemyRadians  = 'Enemy at %1$s, course %2$3.4f radians'
    final String enemyDegrees  = 'Enemy at %1$s, course %2$3.4f degrees'

    quadrant.findEnemies().each { enemyPos ->
      log.printf Level.INFO,enemyAtSector, enemyPos,"from ${shipPos.sector}"
      int relativeCol = invertCoord(enemyPos.col - shipPos.sector.col)
      int relativeRow = enemyPos.row - shipPos.sector.row

      log.printf Level.INFO, enemyAtRelSect,
        new Coords2d(relativeRow,relativeCol)

      final double courseAngle = Math.atan2(
        relativeCol,
        relativeRow
      )
      log.printf Level.INFO, enemyRadians, enemyPos, courseAngle
      final double courseDegrees = Math.toDegrees(courseAngle)
      log.printf Level.INFO, enemyDegrees, enemyPos, courseDegrees

      ui.fmtMsg 'navComp.enemyCourse',[enemyPos,"${repos(courseDegrees)} (repos)"]
      ui.fmtMsg 'navComp.enemyCourse',[enemyPos,"${courseDegrees} (normal)"]
      ui.fmtMsg 'navComp.enemyCourse',[enemyPos,"${invertCourse(courseDegrees)} (inverse)"]
    }
  }

  // change 1..8 to 8..1
  static int invertCoord(final int coord) {
    Math.round(Math.abs( ((coord-8)%9)-1) )
  }

  // change 1..8 to 8..1
  static Double invertCourse(final Double course) {
    course / 45 + 1
  }

  Double reposD(Double d) {
    // ((360-(d+180)) /45 )+3
    d + 45 // convert North = 0 degrees to North = 45 degrees
  }

  List<Double> repos(Double d) {
    [reposD(d),reposD(Math.abs(d))].unique()
  }
  
}
