package net.ebdon.trk21;

import groovy.mock.interceptor.MockFor;
import groovy.test.GroovyTestCase
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
      [
        srsDamaged: true,
        expectedMsgs: ['navComp.retrofit','navComp.srSensor.offline'], ],
      [
        srsDamaged: false,
        expectedMsgs: ['navComp.retrofit',],
      ],
    ]

    testTable.each { Map<Boolean,List<String>> testEntry ->
      TestUi ui = new TestUi()
      MockFor quadMock = MockFor( Quadrant )
      int numCallsToFindEnemies = testEntry.srsDamaged ? 0 : 1
      quadMock.demand.findEnemies(numCallsToFindEnemies) { [ ] }

      quadMock.use {
        new NavComp( ui, testEntry.srsDamaged, shipPos, new Quadrant() ).run()
      }

      assert ui.localMsgLog == testEntry.expectedMsgs
      assert ui.argsLog == [ [] ] * testEntry.expectedMsgs.size()
      assert ui.msgLog.empty
    }
  }

  void testDegreesToCourse() {
    final Map<double,double> expectedResults = [
        0.00d: 3.00d,  22.50d: 2.50d,
       45.00d: 2.00d,  67.50d: 1.50d,
       90.00d: 1.00d, 112.50d: 8.50d,
      135.00d: 8.00d, 157.50d: 7.50d,
      180.00d: 7.00d, 202.50d: 6.50d,
      225.00d: 6.00d, 247.50d: 5.50d,
      270.00d: 5.00d, 292.50d: 4.50d,
      315.00d: 4.00d, 337.50d: 3.50d,
      360.00d: 3.00d,
    ]
    expectedResults.each { degrees, course ->
      assert NavComp.degreesToCourse(degrees) == course
    }
  }
}
