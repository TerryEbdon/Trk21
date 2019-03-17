package net.ebdon.trk21;
/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
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

/**
@brief Trek's Game Calendar
@author Terry Ebdon
@date JAN-2019
*/
@groovy.transform.TypeChecked
@groovy.util.logging.Log4j2
final class TrekCalendar { // line 1110
    final int missionLifeInSolarYears   = 40;
    final int gameStartSolarYear        = ( new Random().nextInt(20) + 20 ) * 100; // T%, T0%
    int currentSolarYear                = gameStartSolarYear;

    boolean isValid() {
        currentSolarYear > 0 &&
        currentSolarYear >= gameStartSolarYear &&
        currentSolarYear <= gameStartSolarYear + missionLifeInSolarYears
    }

  	/// Standard method for getting a string description of the class.
  	/// @return String representation of the class instance.
    String toString() {
        "* DT: $currentSolarYear, " +
        "Start: $gameStartSolarYear, " +
        "Elapsed: ${elapsed()}, " +
        "Lifetime: $missionLifeInSolarYears sol yrs, " +
        "Left: ${remaining()}, OK: ${isValid()}"
    }

    /// Get the mission's elapsed time in \ref SolarYears.
    /// @post elapsed time <= \ref missionLifeInSolarYears.
    /// @return The elapsed mission time in \ref SolarYears.
    int elapsed() {
        final int rv = currentSolarYear - gameStartSolarYear
        assert rv <= missionLifeInSolarYears
        rv
    }

    /// Get the @ref StarDate by which the mission must end.
    /// @post end date >= \ref currentSolarYear.
    /// @return The \ref StarDate by which the mission must end.
    int endDate() {
        final int rv = gameStartSolarYear + missionLifeInSolarYears
        assert rv >= currentSolarYear
        rv
    }

    /// Get the number of \ref SolarYears remaining in the game.
    /// This is the numnber of allowed course changes remaining.
    /// @post remaining <= Mission Lifetime
    /// @return Number of \ref SolarYears remaining in the game.
    int remaining() {
        int syLeft  = endDate() - currentSolarYear
        assert syLeft <= missionLifeInSolarYears
        syLeft
    }

    boolean outOfTime() {
      final boolean oot = remaining() <= 0
      if ( oot ) log.info "Game timer expired.\n$this"
      oot
    }
    /// Force the game clock to tick. Adds one solar year to the current \ref StarDate.
    /// @return the new \ref StarDate.
    int tick() {
        ++currentSolarYear
        // println "tick:  ${this}"
        currentSolarYear
    }
}
