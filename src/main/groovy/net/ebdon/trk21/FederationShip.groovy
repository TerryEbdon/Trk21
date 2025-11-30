package net.ebdon.trk21;

import static GameSpace.*;
import static Quadrant.*;

import groovy.transform.TypeChecked;
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

/// @todo consider using an enum class for #allowedConditions
@groovy.util.logging.Log4j2
@TypeChecked
final class FederationShip implements Moveable {
    final int energyAtStart      = 3000;               ///< E0% in TREK.BAS
    final int lowEnergyThreshold = (energyAtStart / 10).toInteger(); ///< Triggers condition yellow
    int energyNow                = energyAtStart;      ///< E% in TREK.BAS
    String condition             = 'GREEN';            ///< C$ in TREK.BAS
    private Condition condV2 = Condition.green;
    final int maxTorpedoes       = 10;
    int numTorpedoes             = maxTorpedoes;
    int energyUsedByLastMove     = 0 // N%
    Position position            = new Position();
    String id;

    final List<String> allowedConditions = [
      'GREEN',
      'YELLOW',
      'RED',
      'DOCKED'
    ];

    enum Condition {
      green, yellow, red, docked
    }

    String getCondition() {
      condV2.toString().toUpperCase()
    }

    private void useEnergyForMove( final int energyUsedByLastMove ) { /// @bug arg name same as member variable.
      energyNow -= energyUsedByLastMove
      log.info "Ship's movement used $energyUsedByLastMove units of energy."
      logFuelReduction()
    }

    private void energyReducedByEnemyAttack( final int damagelevel ) {
      energyNow -= damagelevel
      log.info "Hit with $damagelevel units of energy."
      logFuelReduction()
    }

    Torpedo getTorpedo() {
      assert numTorpedoes > 0
      new Torpedo(
        id: String.format('Torpedo-%02d',numTorpedoes--),
        position: position.clone()
      )
    }
    void energyReducedByPhaserUse( final int phaserEnergyUsed ) {
      energyNow -= phaserEnergyUsed
      log.info "Firing phasers used $phaserEnergyUsed units of energy."
      logFuelReduction()
    }

    private void logFuelReduction() {
      log.info "Energy reserves reduced to $energyNow"
      if ( deadInSpace() ) {
        log.info 'The ship is dead in space.'
      }
    }

    boolean isValid() {
      energyValid && armouryValid && conditionValid && position.valid
    }

    private boolean isEnergyValid() {
      energyNow >= 0 && energyNow <= energyAtStart
    }

    private boolean isArmouryValid() {
      numTorpedoes >= 0 && numTorpedoes <= maxTorpedoes
    }

    private boolean isConditionValid() {
      allowedConditions.contains( condition )
    }

    String toString() {
        "energy: $energyNow, condition: $condV2, " +
        "torpedoes: $numTorpedoes, " +
        "energyUsedByLastMove: $energyUsedByLastMove, " +
        "energyAtStart: $energyAtStart, $position"
    }

    /// There's a design flaw here. The ship doesn't have enough information to
    /// move, and it has no access to the current sector or the galaxy.
    ///
    /// It doesn't know:
    /// - It's position in the current quadrant.
    /// - The position of the current quadrant in the galaxy.
    /// - The position of objects it might collide with.
    ///
    /// @note Should this be handled in the FederationShip class?
    ///
    /// @arg The desired course as a ShipVector.
    ///
    void move( final ShipVector sv ) {
      assert( sv.valid ) /// @pre The provided Shipvector is valid.
      log.info( "Ship moving: $sv" )

      energyUsedByLastMove = (sv.warpFactor * 8).toInteger()
          // 1 warp factor:
          //  - uses 1 unit of energy
          //  - moves the ship by 1 quadrant.

      useEnergyForMove energyUsedByLastMove
    }

    boolean deadInSpace() {
      final boolean dis = energyNow <= 0
      if ( dis ) {
        log.info "Ship is dead in space.\n$this"
      }
      dis
    }

    void setCondition( final String newCond ) {
        assert allowedConditions.contains( newCond )
        final String oldCond = this.condition
        this.condition = newCond
        log.error( "$id Conditon changed from $oldCond to ${this.condition}" )
    }

    void setCondition( final Condition newCond ) {
        final Condition oldCond = condV2
        condV2 = newCond
        this.@condition = condV2.toString().toUpperCase()
        log.error( "$id CondV2 changed from $oldCond to ${condV2}" )
    }

  /// Attempt to dock if adjacent to a @ref StarBase.
  /// @todo attemptDocking() hard-codes 'DOCKED' condition
  /// @pre the current sector is inside the quadrant
  /// @note It's not possible to dock with a @ref StarBase
  ///       in an adjacent quadrant.
  void attemptDocking( final Quadrant quadrant ) {
    final String logCheckForStarBase = 'Checking for star base in {}, value is {}'
    final String logNowDocked = '{} now docked in sector {} to star base in sector {}'
    final String logDockCheck = 'Checking if I can dock from sector {}'
    final String logAtEdge = 'Ship at board edge, sector {} is outside quadrant.'
    log.debug 'attemptDocking'

    position.sector.with {
      assert quadrant[ row, col ] != Thing.base  // Ship can't be in same sector as a star base.
      assert quadrant.contains( row, col )
      assert quadrant[ row, col ] != Thing.base  // Ship can't be in same sector as a star base.
      log.debug logDockCheck, logFmtCoords(row,col) /// @bug fixme!
      // for ( int i = row - 1; i <= row + 1 && condition != 'DOCKED'; i++) { // 1530
      //   for ( int j = col - 1; j <= col + 1 && condition != 'DOCKED'; j++) { // 1530
      for ( int i = row - 1; i <= row + 1 && condV2 != Condition.docked; i++) { // 1530
        for ( int j = col - 1; j <= col + 1 && condV2 != Condition.docked; j++) { // 1530
          if ( quadrant.contains(i,j) ) {
            log.trace logCheckForStarBase, logFmtCoords(i,j), quadrant[i,j]
            if ( quadrant[i,j] == Thing.base ) {
              condV2       = Condition.docked
              condition = 'DOCKED'
              numTorpedoes = maxTorpedoes
              energyNow    = energyAtStart
              log.error logNowDocked,
                id, logFmtCoords(row,col), logFmtCoords(i,j)
            }
          } else {
            log.debug logAtEdge, logFmtCoords(i,j)
          }
        }
      }
    }
  }

  private void battleStations( final int numEnemyShipsHere ) {
    log.info "There are $numEnemyShipsHere enemy craft in quadrant " +
      position.quadrant
    log.info "Condition RED: there are $numEnemyShipsHere enemy craft here!"
    condV2 = Condition.red
  }

  /// @todo Remove commented out code
  void shortRangeScan( final Galaxy galaxy ) {
    log.debug 'shortRangeScan() called for quadrant {}', position.quadrant
    final int minGalaxyDimension = 4                  // Galaxy must be at least this "long".
    final int minGalaxyArea = (minGalaxyDimension ** 2).toInteger() // Galaxy is square.

    assert galaxy.boardSize >= minGalaxyArea
    assert galaxy.insideGalaxy( position.quadrant )

    final int numEnemyShipsHere = (galaxy[ position.quadrant ] / 100).toInteger()
    if ( numEnemyShipsHere > 0 ) {
      battleStations( numEnemyShipsHere )
    } else {
      nonBattleCondition( galaxy )
    }
  }

  /// @todo Refactor - Extract method nonBattleCondition()
  private void nonBattleCondition( final Galaxy galaxy ) {
    if ( energyNow > lowEnergyThreshold ) {
      log.debug "$energyNow is above threshold of $lowEnergyThreshold"
      condV2 = Condition.green
      scanAdjacentQuadrants( galaxy )
    } else {
      log.debug "$energyNow is below threshold of $lowEnergyThreshold"
      condV2 = Condition.yellow
      log.info "Low on energy: $this"
    }
  }

  /// @todo Refactor - Extract method scanAdjacentQuadrants()
  private void scanAdjacentQuadrants( final Galaxy galaxy ) {
    Range<Integer> rowsToScan = (position.quadrant.row - 1)..( position.quadrant.row + 1)
    Range<Integer> colsToScan = (position.quadrant.col - 1)..( position.quadrant.col + 1)
    for ( int i in rowsToScan ) {
      for ( int j in colsToScan ) {
        if ( insideGalaxy( i, j ) ) {
          final int quadrantStatus = galaxy[i,j]
          if ( quadrantStatus > 99 ) {
            condV2 = Condition.yellow
            log.info "adjacent quadrant [$i,$j] = $quadrantStatus has enemy ships!"
          } else {
            log.debug "adjacent quadrant [$i,$j] = $quadrantStatus is clear."
          }
        } else {
          log.debug "adjacent quadrant [$i,$j] is outside the galaxy."
        }
      }
    }
  }

  @TypeChecked
  boolean insideGalaxy( final int x, final int y ) { /// @todo insideGalaxy() should be in a new Galaxy class.
    [1..8].flatten().containsAll( [x,y] ) /// @todo insideGalaxy() uses hardcoded galaxy size
  }

  /// @deprecated Use Quadrant.contains()
  @TypeChecked
  private boolean inQuadrant( final int x, final int y ) { /// @todo inQuadrant() should be in a new Galaxy class.
    insideGalaxy(x,y)
  }

  @TypeChecked
  boolean isProtectedByStarBase() {
    condV2 == Condition.docked
  }

  @TypeChecked
  void hitFromEnemy( final int damagelevel ) {
    assert !isProtectedByStarBase()
    assert condV2 == Condition.red
    energyReducedByEnemyAttack damagelevel
  }
}
