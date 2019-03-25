package net.ebdon.trk21;

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
@groovy.util.logging.Log4j2
@groovy.transform.TypeChecked
class DamageReporter {
  Map<Integer, ShipDevice> devices;
  Closure<String> rbString;
  Closure localMsg;

  void report( Map<Integer, ShipDevice> damage, Closure<String> rbStrCl, Closure localMsgCl ) {
    log.trace 'In DamageReporter.report() -- begin'

    devices  = damage
    rbString = rbStrCl
    localMsg = localMsgCl

    if ( !devices[6].isDamaged() ) {
      header()
      body()
    } else {
      offline()
    }
    log.trace 'DamageReporter.report() -- End'
  }

  private void header() {
    localMsg 'damage.control.report.h0'
    localMsg 'damage.control.report.h1'
    localMsg 'damage.control.report.h2'
    localMsg 'damage.control.report.h3'
  }

  private void offline() {
    localMsg 'damage.control.offline' // Damage Control is damaged!
  }

  private void body() {
    devices.values().each { device ->
      Object[] msgArgs = [
        rbString( device.id ).padRight(15),
        sprintf( '%2d', device.state)
      ]

      localMsg 'damage.control.report.line', msgArgs
    }
    localMsg 'damage.control.report.footer'
  }
}
