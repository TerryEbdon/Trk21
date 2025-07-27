package net.ebdon.trk21;

import groovy.test.GroovyTestCase
import groovy.mock.interceptor.MockFor;
import groovy.transform.TypeChecked;
/**
 * @file
 * @author      Terry Ebdon
 * @date        April 2019
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

@groovy.util.logging.Log4j2('logger')
final class TrekShortRangeScanTest extends TrekTestBase {

  private MockFor game;
  private MockFor enemyFleet;
  private MockFor dcMock;
  private MockFor srScan;

  @TypeChecked
  @Override void setUp() {
    super.setUp()
    srScan     = new MockFor( ShortRangeScan )
    game       = new MockFor( TrekCalendar )
    enemyFleet = new MockFor( EnemyFleet )
    dcMock     = new MockFor( DamageControl )
  }

  void testOffline() {
    dcMock.demand.isDamaged { true }

    dcMock.use {
      trek = new Trek( ui )
      trek.damageControl = new DamageControl()
      srScan.use { // No demands, shouldn't be used.
        trek.shortRangeScan()
      }
    }

    assert ui.localMsgLog == ['sensors.shortRange.offline']
  }

  void testShortRangeScan() {
    srScan.demand.scan { }
    dcMock.demand.isDamaged { false }
    game.demand.getCurrentSolarYear { 12345 }
    enemyFleet.demand.getNumKlingonBatCrRemain { 99 }

    enemyFleet.use {
      trek = new Trek( ui )
      trek.enemyFleet = new EnemyFleet()
      game.use {
        trek.game = new TrekCalendar()
        dcMock.use {
          srScan.use {
            trek.damageControl = new DamageControl()
            trek.shortRangeScan()
          }
        }
      }
    }

    assert ui.empty
  }
}
