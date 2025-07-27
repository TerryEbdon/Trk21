package net.ebdon.trk21;

import static ShipDevice.*;

import groovy.test.GroovyTestCase

/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
 * @copyright   Terry Ebdon, 2019
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
  private ShipDevice sd;

  @Override void setUp() {
    super.setUp()
    sd = new ShipDevice()
    // sd.state=-99
  }

  void testShipDeviceEnum() {
    sd = new ShipDevice( DeviceType.engine )
    assert sd.id == DeviceType.engine.id
    assert sd.id.contains( 'ENGINE')
    assert sd.state == 0
  }

  void testShipDevice() {
    assert sd.state == 0
    assert sd.name == ''
    assert sd.state == sd[1]
    assert sd.name == sd[0]

    sd.name = 'Neutron Blaster'
    assert sd.name == 'Neutron Blaster'
    assert sd.name == sd[0]

    --sd.state
    assert sd.state == sd[1]
    assert sd.state == -1

    -- sd[1]
    assert sd.state == sd[1]
    assert sd.state == -2
  }
}
