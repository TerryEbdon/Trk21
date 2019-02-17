package net.ebdon.trk21;

import static ShipDevice.*

//@groovy.util.logging.Log4j2('logger')
final class ShipDeviceTest extends GroovyTestCase {
  ShipDevice sd

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
