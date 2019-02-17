package net.ebdon.trk21;

import groovy.util.logging.*;

@groovy.util.logging.Log4j2('logger')
final class TrekCalendarTest extends GroovyTestCase {
    private trekCalendar; // = new TrekCalendar();

    @Override
    void setUp() {
        logger.trace 'setUp'
        assert trekCalendar == null
        trekCalendar = new TrekCalendar()
    }

    void testConstruction() {
      logger.info 'testConstruction'
      assertTrue( "$trekCalendar", trekCalendar.isValid() )
    }

    void testTick() { /// Test the calendar's clock tick.
        logger.info 'testTick'
        assertFalse( "trekCalendar shouldn't be null", trekCalendar == null )
        assertEquals(
            "$trekCalendar",
            trekCalendar.currentSolarYear + 1,
            trekCalendar.tick()
        )
    }

    void testOutOfTime() {
      trekCalendar.with {
        assertFalse "$trekCalendar", outOfTime()
        currentSolarYear += missionLifeInSolarYears
        assertTrue "$trekCalendar", outOfTime()
        --currentSolarYear
        assertFalse "$trekCalendar", outOfTime()
      }
    }
}
