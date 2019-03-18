package net.ebdon.trk21;

/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
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
final class TrekCalendarTest extends GroovyTestCase {
    private TrekCalendar trekCalendar; // = new TrekCalendar();

    @Override
    void setUp() {
        logger.trace 'setUp'
        assert trekCalendar == null
        trekCalendar = new TrekCalendar()
    }

    void testConstruction() {
      logger.info 'testConstruction'
      assert trekCalendar.valid
    }

    void testTick() { /// Test the calendar's clock tick.
        logger.info 'testTick'
        assert trekCalendar
        assert trekCalendar.currentSolarYear + 1 == trekCalendar.tick()
    }

    void testOutOfTime() {
      trekCalendar.with {
        assert !outOfTime()
        currentSolarYear += missionLifeInSolarYears
        assert outOfTime()
        --currentSolarYear
        assert !outOfTime()
      }
    }
}
