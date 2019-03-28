package net.ebdon.trk21;

// import static Quadrant.*;
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
final class CliTest extends GroovyTestCase {

  void testNavComp() {
    MockFor trekMock          = MockFor( Trek )
    MockFor scannerMock       = MockFor( Scanner )

    trekMock.demand.asBoolean       { true } /// @todo refactor Repositioner & Trek to avoid this.
    trekMock.demand.setupGame       { }
    trekMock.demand.startGame       { }
    trekMock.demand.navComp         { }
    trekMock.demand.gameWon         { false }
    trekMock.demand.gameLost        { false }
    trekMock.demand.gameContinues   { true }

    scannerMock.demand.useDelimiter { String dlm -> assert dlm == '\n'; scannerMock }
    scannerMock.demand.next         { 'n' }
    scannerMock.demand.next         { 'q' }

    scannerMock.use {
      trekMock.use {
        scannerMock.use {
          new TrekCli().run()
        }
      }
    }
  }
}
