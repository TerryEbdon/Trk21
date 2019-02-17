package net.ebdon.trk21;

import static ShipDevice.*;
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

//@groovy.util.logging.Log4j2('logger')
final class ShipDeviceTest extends GroovyTestCase {
  ShipDevice sd;

  @Override void setUp() {
    sd = new ShipDevice()
    // sd.state=-99
  }

  void testShipDeviceEnum() {
    sd = new ShipDevice( DeviceType.engine )
    assertEquals  DeviceType.engine.id, sd.id
    assertTrue    "Wrong device ID", sd.id.contains( 'ENGINE')
    assertTrue    "Wrong device state", sd.state == 0
  }

  void testShipDevice() {
    assertEquals "$sd", 0, sd.state
    assertEquals "$sd", "", sd.name
    assertEquals "$sd", sd[1], sd.state
    assertEquals "$sd", sd[0], sd.name

    sd.name = "Neutron Blaster"
    assertEquals "$sd", "Neutron Blaster", sd.name
    assertEquals "$sd", sd[0],             sd.name

    --sd.state
    assertEquals "$sd", sd[1], sd.state
    assertEquals("$sd",  -1,  sd.state)

    -- sd[1]
    assertEquals "$sd", sd[1], sd.state
    assertEquals("$sd",  -2,   sd.state )
  }
}
