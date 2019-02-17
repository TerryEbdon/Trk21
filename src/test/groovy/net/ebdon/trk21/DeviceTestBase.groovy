package net.ebdon.trk21;

import static ShipDevice.*
/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
 * @copyright
 *
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

 abstract class DeviceTestBase extends GroovyTestCase {
  def damage = [
    1:new ShipDevice('111'),
    2:new ShipDevice('222'),
    3:new ShipDevice('333'),
    4:new ShipDevice( DeviceType.phasers),
    5:new ShipDevice('555'),
    6:new ShipDevice('666')
  ];
}
