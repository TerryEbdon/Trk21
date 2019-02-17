package net.ebdon.trk21;

// 
// import groovy.util.logging.Log4j
// import static GameSpace.*
import static ShipDevice.*

// @groovy.util.logging.Log4j2('logger')
abstract class DeviceTestBase extends GroovyTestCase {
  def damage = [
    1:new ShipDevice('111'),
    2:new ShipDevice('222'),
    3:new ShipDevice('333'),
    4:new ShipDevice( DeviceType.phasers),
    5:new ShipDevice('555'),
    6:new ShipDevice('666')
  ]
}
