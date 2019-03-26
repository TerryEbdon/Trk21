package net.ebdon.trk21;

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
@groovy.transform.ToString(includePackage=false)
@groovy.transform.Canonical
// @groovy.transform.TypeChecked
final class LongRangeScan {
  def ui;
  DamageControl damageControl;
  Galaxy galaxy;

  void scanAround( final Coords2d quadrantCoords ) {
    if ( damageControl.isDamaged( ShipDevice.DeviceType.lrSensor ) ) {
      ui.localMsg 'sensors.longRange.offline'
    } else {
      scanFromQuadrant quadrantCoords
    }
  }

  private void scanFromQuadrant( final Coords2d quadrantCoords ) {
    quadrantCoords.with {
      ui.localMsg 'sensors.longRange.scanQuadrant', [col, row] as Object[]

      final Range<Integer> columnRange = (col - 1)..(col + 1)
      for ( int rowToScan in (row - 1)..(row + 1) ) { // q1% -1 to q1% + 1
        ui.outln scanRow( rowToScan, columnRange )
      }
    }
  }

  @groovy.transform.TypeChecked
  private String scanRow( final int rowToScan, final Range<Integer> colRange ) {
    String rowStatus = ''
    for ( int colToscan in colRange ) { // q2% -1 to q2% + 1
      rowStatus += ( '  ' + galaxy.scan( rowToScan, colToscan ) )
    }
    rowStatus
  }
}
