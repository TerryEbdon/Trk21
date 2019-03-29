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

@Newify(MockFor)
@SuppressWarnings('JUnitTestMethodWithoutAssert')
@SuppressWarnings('Println')
final class CliTest extends GroovyTestCase {

  private MockFor trekMock;
  private MockFor scannerMock;

  @Override void setUp() {
    super.setUp()
    trekMock    = MockFor( Trek )
    scannerMock = MockFor( Scanner )
    scannerMock.demand.useDelimiter { String dlm -> assert dlm == '\n'; scannerMock }
    trekPreTestDemands()
  }

  private void trekPreTestDemands() {
    trekMock.demand.with {
      setupGame       { }
      startGame       { }
    }
  }

  private void trekPostTestDemands() {
    trekMock.demand.with {
      gameWon         { false }
      gameLost        { false }
      gameContinues   { true  }
    }
  }

  private void runTestWith( final String commands ) {
    trekPostTestDemands()
    commands.toList().each { String nextCommand ->
      scannerMock.demand.next { nextCommand }
    }

    scannerMock.use {
      trekMock.use {
        new TrekCli().run()
      }
    }
  }

  void testShortRangeScan() {
    trekMock.demand.shortRangeScan  { println 's\nshortRangeScan() called.' }
    runTestWith 'sq'
  }

  void testLongRangeScan() {
    trekMock.demand.longRangeScan   { println 'l\nlongRangeScan() called.' }
    runTestWith 'lq'
  }

  void testSetCourse() {
    trekMock.demand.setCourse       { println 'c\nsetCourse() called.' }
    runTestWith 'cq'
  }

  void testFireTorpedo() {
    trekMock.demand.fireTorpedo     { println 't\nfireTorpedo() called.' }
    runTestWith 'tq'
  }

  void testFirePhasers() {
    trekMock.demand.firePhasers     { println 'p\nfirePhasers() called.' }
    runTestWith 'pq'
  }

  void testReportDamage() {
    trekMock.demand.reportDamage    { println 'd\nreportDamage() called.' }
    runTestWith 'dq'
  }

  void testNavComp() {
    trekMock.demand.navComp         { println 'n\nnavComp() called.' }
    runTestWith 'nq'
  }
}
