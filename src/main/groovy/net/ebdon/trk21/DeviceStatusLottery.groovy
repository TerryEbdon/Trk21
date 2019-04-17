package net.ebdon.trk21;

/**
 * @file
 * @author      Terry Ebdon
 * @date        March 2019
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
@groovy.transform.TupleConstructor
@groovy.transform.TypeChecked
final class DeviceStatusLottery {

  DamageControl damageControl;
  Closure localMsg;

  @SuppressWarnings('InsecureRandom')
  static void run( DamageControl dc, Closure uiClosure ) {
    log.debug 'deviceStatusLottery()'
    new DeviceStatusLottery( dc, uiClosure ).with {
      if ( new Random().nextFloat() <= 0.5 ) { // 1760
        spaceStorm()
      } else { // 1790 - Not a space storm
        randomDeviceRepair()
      }
    }
  }

  @SuppressWarnings('InsecureRandom')
  int getRandomDamageAmount( ) {
    new Random().nextInt( 5 ) + 1
  }

  void spaceStorm() {
    final int systemToDamage  = damageControl.randomDeviceKey
    final int damageAmount    = randomDamageAmount
    damageControl.inflictDamage systemToDamage, damageAmount

    log.info  "Space storm has damaged device No. $systemToDamage"
    log.info  "   damage of $damageAmount units"
    log.debug "   new status: ${damageControl.deviceState(systemToDamage)} units"
    localMsg 'deviceStatusLottery.spaceStorm',
      [ damageControl.deviceName( systemToDamage) ] as Object[]
  }

  void randomDeviceRepair() {
    final int firstDamagedDeviceKey = damageControl.findDamagedDeviceKey()
    if ( firstDamagedDeviceKey ) {
      damageControl.randomlyRepair( firstDamagedDeviceKey )
      final String deviceId = damageControl.deviceId(firstDamagedDeviceKey)

      localMsg 'truce', [ "device.DAMAGE.$deviceId" ] as Object[]
    }
  }
}
