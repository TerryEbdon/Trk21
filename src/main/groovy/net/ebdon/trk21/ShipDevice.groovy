package net.ebdon.trk21;

import groovy.transform.TypeChecked;
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

final class ShipDevice {

  @TypeChecked
  enum DeviceType {
    none('device.NONE'),
    engine('device.WARP.ENGINES'),
    srSensor('device.S.R..SENSORS'),
    lrSensor('device.L.R..SENSORS'),
    phasers('device.PHASER.CNTRL'),
    torpedoes('device.PHOTON.TUBES'),
    damage('device.DAMAGE.CNTRL')

    final String id;
    DeviceType( final String sym ) { id = sym }
  }

  int state = 0;
  String id = '';
  String name = '';

  @TypeChecked
  ShipDevice() {
    state = 0;
    id = name = ''
  }

  @TypeChecked
  ShipDevice( DeviceType dt ) {
    state = 0
    id = dt.id
  }

  @TypeChecked
  ShipDevice( final String anId ) {
    id    = anId
    name  = id
  }

  @TypeChecked
  boolean isDamaged() {
    state
  }

  def getAt( final int n) {
    assert [0,1].contains(n)
    n ? state : name
  }

  int putAt( final int n, value) {
    assert [0,1].contains(n)
    if ( n ) {
      state = value
    } else {
      name = value
    }
    n
  }

  final String toString() {
    "id: $id, name: $name: state: $state"
  }
}
