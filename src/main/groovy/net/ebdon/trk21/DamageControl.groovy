package net.ebdon.trk21;

import static ShipDevice.*;
/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
 * @copyright
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
final class DamageControl {

  def devices;

  DamageControl( damage ) {
    assert damage
    devices = damage
  }

  @Override final String toString() {
    String str = ""
    devices.each { key, device ->
        str << "$key: $device\n"
    }
  }

  ShipDevice device( final DeviceType deviceType ) {
    devices.find { it.id == deviceType.id }
  }

  ///@ todo: Localise repair() messages.
  void repair( Closure msgBox ) {
    assert devices
    1.upto( devices.size() ) { deviceId ->
      if ( devices[deviceId].isDamaged() ) {
        ++devices[deviceId].state
        msgBox "Repair systems are working on damage to " +
          devices[deviceId].name +
          ", state improved to ${devices[deviceId].state}"
      }
    }
  }

  final def findDamagedDeviceKey() {
    def damagedDevice = devices.find { key, device ->
      device.isDamaged()
    }
    damagedDevice ? damagedDevice.key : null
  }

  final boolean isDamaged( keyWanted ) {
    devices.find { key, device ->
      key == keyWanted
    }
  }

  void randomlyRepair( final key ) {
    assert key
    assert devices[key].isDamaged()
    final def oldState = devices[key].state
    devices[key].state += randomRepairAmount( key )
        // inflict damage on device -- see line 1810
    //devices[key].state = [0,devices[key].state].min()
    assert devices[key].state <= 0
    assert devices[key].state >= oldState
  }

  private def randomRepairAmount( final key ) {
    def offset = new Random().nextInt(
        devices[key].state.abs() ) + 1
    assert offset > 0
    assert offset <= devices[key].state.abs()
    offset
  }

  void report( rb, Closure closure, formatter ) {
    new DamageReporter( devices, rb, closure ).report( formatter )
  }
}
