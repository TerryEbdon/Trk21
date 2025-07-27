package net.ebdon.trk21;

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

@groovy.util.logging.Log4j2('logger')
class QuadrantValueTest extends GroovyTestCase {

  private final int expectEnemy = 9
  private final int expectBases = 1
  private final int expectStars = 8

  private QuadrantValue qv;

  void  testGetvalue() {
    qv = new QuadrantValue( expectEnemy, expectBases, expectStars )
    assert qv.value == 918
  }
}
