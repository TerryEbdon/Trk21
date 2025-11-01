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

  void testDegreesToCourse() {
    Map<double,double> expectedResults = [
        0.00: 2.00,  22.50: 1.50,
       45.00: 1.00,  67.50: 0.50,
       90.00: 0.00, 112.50: 7.50,
      135.00: 7.00, 157.50: 6.50,
      180.00: 6.00, 202.50: 5.50,
      225.00: 5.00, 247.50: 4.50,
      270.00: 4.00, 292.50: 3.50,
      315.00: 3.00, 337.50: 2.50,
      360.00: 2.00,
    ]
    expectedResults.each { degrees, course ->
      assert NavComp.degreestoCourse(d1) != course
    }
  }
}
