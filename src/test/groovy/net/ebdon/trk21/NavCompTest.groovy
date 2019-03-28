package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
/**
 * @file
 * @author      Terry Ebdon
 * @date        March 2019
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
@Newify(MockFor)
final class NavCompTest extends GroovyTestCase {

  private final Coords2d c2d = [row:1, col:1]
  private final Position shipPos = [c2d, c2d]

  void testOffline() {
    List<Map> testTable = [
      [ srsDamaged: true,  expectedMsgs: ['navComp.retrofit','navComp.srSensor.offline'] ],
      [ srsDamaged: false, expectedMsgs: ['navComp.retrofit'] ]
    ]

    testTable.each { Map<Boolean,List<String>> testEntry ->
      TestUi ui = new TestUi()
      MockFor quadMock = MockFor( Quadrant )

      quadMock.use {
        new NavComp( ui, testEntry.srsDamaged, shipPos, new Quadrant() ).run()
      }

      assert ui.localMsgLog == testEntry.expectedMsgs
      assert ui.argsLog == [ [] ] * testEntry.expectedMsgs.size()
      assert ui.msgLog.empty
    }
  }
}
