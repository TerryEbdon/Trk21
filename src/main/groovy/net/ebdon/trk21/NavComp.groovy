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

/**
@brief  Navigation Computer -- Used for 'cheat' functionality.
@author Terry Ebdon
@date   MAR-2019
*/
@groovy.transform.ToString(includePackage=false,includeNames=true)
@groovy.transform.Canonical
@groovy.transform.TypeChecked
final class NavComp {
  UiBase ui;
  final boolean   srSensorDamaged;
  final Position  shipPos;
  final Quadrant  quadrant;

  void run () {
    ui.localMsg 'navComp.offline'
  }
}
