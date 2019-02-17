package net.ebdon.trk21;

import static ShipDevice.*
import groovy.mock.interceptor.MockFor

final class DamageControlTest extends DeviceTestBase {

  private DamageControl dc;

  @Override void setUp() {
    damage[2].state = -3
    dc = new DamageControl( damage )
  }

  void testDamageControl() {
    dc.with {
      def damaged = damage[ findDamagedDeviceKey() ]
      assertEquals  toString(), '222', damaged.id
      assertEquals  toString(), -3, damaged.state
      assertTrue    toString(), damaged.isDamaged()
    }
  }

  void testFindDamagedWithNoDamage() {
    damage[2].state = 0
    assertEquals null, dc.findDamagedDeviceKey()
  }

  void testRandomlyRepair() {
    def oldState = damage[2].state
    dc.randomlyRepair 2
    assertTrue "oldState:$oldState\n$dc", damage[2].state > oldState
  }

  void testDamageControlRepair() {
    // if ( notYetImplemented() ) return
    def oldState = damage[2].state
    dc.repair  DamageControlRepairCallBackMock.&callBack
    assertTrue DamageControlRepairCallBackMock.called
    assertTrue DamageControlRepairCallBackMock.calledWith.size() > 0
    assertTrue "oldState:$oldState\n$dc", damage[2].state > oldState
    // assert false
  }
  
  void testDamageControlReport() {
    if ( notYetImplemented() ) return

    // dc.report( rb, Closure closure, formatter )
    // MockFor drMock = new MockFor( DamageReporter )
    // drMock.demand.report { rb, Closure closure, formatter ->
    //
    // }
    // drMock.use {
    //   dc.report( {true} )
    // }
    assert false
  }

  class DamageControlRepairCallBackMock {

    static boolean called = false;
    static String calledWith = ""

    static void callBack( msg ) {
      called = true
      calledWith = msg
    }
  }
}
