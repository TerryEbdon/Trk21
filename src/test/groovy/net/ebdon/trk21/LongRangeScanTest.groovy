package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
import groovy.test.GroovyTestCase
/**
 * @file
 * @author      Terry Ebdon
 * @date        April 2019
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

@Newify( [MockFor,Coords2d] )
final class LongRangeScanTest extends GroovyTestCase {
  private MockFor dcMock;
  private MockFor galaxyMock;
  private Coords2d shipQuad;
  private TestUi ui;


  @groovy.transform.TypeChecked
  @Override void setUp() {
    super.setUp()
    dcMock     = MockFor( DamageControl )
    galaxyMock = MockFor( Galaxy )
    shipQuad   = Coords2d(1,1)
    ui         = new TestUi()
  }

  void testOffline() {
    dcMock.demand.isDamaged { true }

    dcMock.use {
      galaxyMock.use {
        new LongRangeScan( ui, new DamageControl(), new Galaxy() ).
            scanAround( shipQuad )
      }
    }

    assert ui.localMsgLog == ['sensors.longRange.offline']
    assert ui.argsLog     == [ [] ]
  }

  void testOnline() {
    galaxyMock.demand.scan(9) { int row, int col -> 'scan' }

    dcMock.demand.isDamaged { false }

    dcMock.use {
      galaxyMock.use {
        new LongRangeScan( ui, new DamageControl(), new Galaxy() ).
            scanAround( shipQuad )
      }
    }

    assert ui.localMsgLog == ['sensors.longRange.scanQuadrant']
    assert ui.argsLog     == [ [1,1] ]
    assert ui.msgLog      == ['  scan' * 3] * 3
  }
}
