package net.ebdon.trk21;

import groovy.transform.TypeChecked;
/**
 * @file
 * @author      Terry Ebdon
 * @date        April 2019
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
@TypeChecked
@groovy.transform.TupleConstructor
final class ShortRangeScan {

  FederationShip ship;
  Quadrant quadrant;
  Galaxy galaxy;
  UiBase ui;
  int currentSolarYear;
  int numKlingonBatCrRemain;

  @TypeChecked
  private void scanSummary() {
    ship.position.sector.with {
      ui.fmtMsg 'sensors.shipStatus.1', [ currentSolarYear, ship.condition ]
      ui.fmtMsg 'sensors.shipStatus.2', [ currentQuadrant(), col, row ]
      ui.fmtMsg 'sensors.shipStatus.3', [ ship.energyNow, ship.numTorpedoes ]
      ui.fmtMsg 'sensors.shipStatus.4', [ numKlingonBatCrRemain ]
    }
  }

  @TypeChecked
  String currentQuadrant() {
    "${ship.position.quadrant.col} - ${ship.position.quadrant.row}"
  }

  @TypeChecked
  private void showCondition() {
    ui.conditionText = displayableCondition()
  }

  /// @return A localised display string for the ship's condition
  /// @todo Move displayableCondition() into FederationShip
  /// @todo Localise via the #rb resource bundle.
  /// @bug  Code assumes that all condition values, other than "DOCKED",
  ///       are also valid HTML colours. This is currently true for ENGLISH
  ///       locales, but will fail for other languages. Colours and language
  ///       should be orthogonal.
  /// @todo Consider splitting into two methods, to allow use where HTML is
  ///       not appropriate.
  private String displayableCondition() {
    /// @bug Should use config insteaf of HTML fonts.
    /// @todo This is incompatible with a CLI based UI.
    final String colour = ship.condition != 'DOCKED' ? ship.condition : 'GREEN'
    "<html><font size=+2 color=$colour>${ship.condition}</font></html>"
  }

  @TypeChecked
  private void scanQuadrant() {
    ui.localMsg 'sensors.shortRange.header'
    quadrant.displayOn ui.&outln
    ui.localMsg 'sensors.shortRange.divider'
  }

  void scan() {
    ship.shortRangeScan( galaxy ) // Performs a LR scan to set ship's condition.
    ship.attemptDocking( quadrant )
    scanQuadrant()
    showCondition()
    scanSummary()
  }
}
