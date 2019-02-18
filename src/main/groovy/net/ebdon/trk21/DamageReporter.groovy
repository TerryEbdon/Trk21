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
class DamageReporter {
  def devices;
  def rb;
  def msgBox;

  DamageReporter( damage, bundle, Closure closure  ) {
    devices = damage
    rb = bundle
    msgBox = closure
  }

  def report( formatter ) {
    if ( !devices[6].isDamaged() ) {
      header()
      body formatter
      } else {
        offline()
      }
    }

  private void header() {
    msgBox ''
    msgBox rb.getString( 'damage.cntrl.report.h1' )
    msgBox rb.getString( 'damage.cntrl.report.h2' )
    msgBox rb.getString( 'damage.cntrl.report.h3' )
  }

  private void offline() {
    msgBox rb.getString( 'damage.control.offline' ) // "Damage Control is damaged!"
  }

  private void body( formatter ) {
    devices.values().each { device ->
      Object[] msgArgs = [
        rb.getString( device.id ).padRight(15),
        sprintf( '%2d', device.state)
      ]

      formatter.applyPattern( rb.getString( 'damage.cntrl.report.line' ) );
      msgBox formatter.format( msgArgs );
    }
    msgBox ''
  }
}
