package net.ebdon.trk21;

import groovy.transform.TypeChecked;

import static ShipDevice.*;
/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
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
final class DamageControl {

  Map<Integer,ShipDevice> devices = [
    1: new ShipDevice('device.WARP.ENGINES'), 2: new ShipDevice('device.S.R..SENSORS'),
    3: new ShipDevice('device.L.R..SENSORS'), 4: new ShipDevice('device.PHASER.CNTRL'),
    5: new ShipDevice('device.PHOTON.TUBES'), 6: new ShipDevice('device.DAMAGE.CNTRL')
  ]; ///< D%[] and D$[] in TREK.BAS.
     ///< @note elements [n][0] are keys to the Language resource bundle, via #rb.

  @Override final String toString() {
    String str = ''
    devices.each { key, device ->
        str << "$key: $device\n"
    }
  }

  ShipDevice device( final DeviceType deviceType ) {
    devices.find { it.id == deviceType.id }
  }

  // @TypeChecked
  void repair( Closure logMsg ) {
    log.trace 'repair()'
    assert devices
    1.upto( devices.size() ) { int deviceId ->
      if ( devices[deviceId].isDamaged() ) {
        ++devices[deviceId].state
        logMsg 'damage.control.repair',
          [ devices[deviceId].name, devices[deviceId].state ] as Object[]
      }
    }
    log.trace 'repair() -- OK'
  }

  @TypeChecked
  final int findDamagedDeviceKey() {
    devices.find { int key, ShipDevice device ->
      device.isDamaged()
    }?.key ?: 0
  }

  @TypeChecked
  String deviceId( final int key ) {
    devices[key].id
  }

  @TypeChecked
  String deviceState( final int key ) {
    devices[key].state
  }

  @TypeChecked
  String deviceName( final int key ) {
    devices[key].name
  }

  @TypeChecked
  int getRandomDeviceKey() {
    new Random().nextInt( devices.size() ) + 1
  }

  @TypeChecked
  final boolean isDamaged( final int keyWanted ) {
    log.trace "Checking for damage to DeviceType key: $keyWanted"
    assert devices.keySet().contains( keyWanted )
    devices[keyWanted].state
  }

  @TypeChecked
  final boolean isDamaged( DeviceType deviceTypeWanted ) {
    log.trace "Checking for damage to DeviceType $deviceTypeWanted"
    devices.find { int key, ShipDevice shipDevice ->
      shipDevice.id == deviceTypeWanted.id
    }?.value.state
  }

  @TypeChecked
  void randomlyRepair( final int key ) {
    assert key
    assert devices[key].isDamaged()
    final int oldState = devices[key].state
    devices[key].state += randomRepairAmount( key ) // inflict damage; see line 1810
    assert devices[key].state <= 0
    assert devices[key].state >= oldState
  }

  @TypeChecked
  private int randomRepairAmount( final int key ) {
    final int offset = new Random().nextInt(
        devices[key].state.abs() ) + 1
    assert offset > 0
    assert offset <= devices[key].state.abs()
    offset
  }

  @TypeChecked
  void inflictDamage( final int key, final int amount ) {
    assert key > 0 && key < devices.size()
    assert amount > 0
    devices[key].state -= amount
  }

  void report( Closure rbGetString, Closure localMsg ) {
    new DamageReporter().report( devices, rbGetString, localMsg )
  }
}
