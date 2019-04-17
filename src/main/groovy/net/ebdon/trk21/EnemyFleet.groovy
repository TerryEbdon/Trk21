package net.ebdon.trk21;

import static net.ebdon.trk21.GameSpace.*;
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
@groovy.util.logging.Log4j2
final class EnemyFleet {
    /**
    Energy level assigned to each Klingon ship. This is reset every time
    the Enterprise enters a quadrant that contains Klingons. This is `S9%`
    in TREK.BAS.
    **/
    static final int maxKlingonEnergy = 200;
    static final int maxKlingonBCinQuad = 9;

    int numKlingonBatCrTotal  = 0; ///< k0% in TREK.BAS
    int numKlingonBatCrRemain = 0; ///< k9% in TREK.BAS
    int numKlingonBatCrInQuad = 0; ///< K3% in TREK.BAS

    final int maxPossibleKlingonShips = 64 * maxKlingonBCinQuad;

    private final int[][] klingons = new int[maxKlingonBCinQuad + 1][4]; ///< k%[] in TREK.BAS

    final private int idIdx     = 0;
    final private int rowIdx    = 1;
    final private int colIdx    = 2;
    final private int energyIdx = 3;

    private final List<Integer> scrapHeap = [];

    /// @bug A private array was being accessed from in Trek.distributeKlingons()
    final float[] softProbs = ///< r[0..9] in TREK.BAS
        [ 0, 0.0001, 0.01, 0.03, 0.08, 0.28, 1.28, 3.28, 6.28, 13.28 ];

    /// @todo Move energyHittingTarget() into a new Galaxy or GamePhysics class?
    @SuppressWarnings('InsecureRandom')
    @TypeChecked
    static int energyHittingTarget(
        final int energyReleased,
        final float distanceToTarget ) {

      assert energyReleased   > 0 && energyReleased   <= maxKlingonEnergy
      assert distanceToTarget > 0 && distanceToTarget <= maxSectorDistance

      /// @todo: Same bug as was in PhaserControl - it's possible to
      /// hit the target with more energy than was fired at it.
      float rnd = new Random().nextFloat()
      ( ( ( energyReleased / distanceToTarget ) * ( 2 + rnd ) ) + 1 ).toInteger()
    }

    static eachShipNo( final Closure closure ) {
      for ( int shipNo in 1..maxKlingonBCinQuad ) {
        closure shipNo
      }
    }

    boolean isValid() {
      numKlingonBatCrInQuad in 0..numKlingonBatCrRemain   &&
      numKlingonBatCrRemain in 0..numKlingonBatCrTotal    &&
      numKlingonBatCrTotal  in 0..maxPossibleKlingonShips &&
      scrapHeap.size() <= maxKlingonBCinQuad
    }

    boolean getDefeated() {
      assert isValid()
      numKlingonBatCrRemain == 0
    }

    void setNumKlingonBatCrRemain( final int newNumKbcRemain ) {
      assert newNumKbcRemain in 0..numKlingonBatCrTotal
      numKlingonBatCrRemain = newNumKbcRemain
    }

    void setNumKlingonBatCrTotal( final int newNumKbcTotal ) {
      assert newNumKbcTotal in 0..maxPossibleKlingonShips
      numKlingonBatCrTotal = newNumKbcTotal
    }

    void setNumKlingonBatCrInQuad( final int newNumKbcIq ) {
      assert newNumKbcIq in 0..maxPossibleKlingonShips
      assert newNumKbcIq <= numKlingonBatCrRemain

      log.trace( "numKlingonBatCrInQuad changed from $numKlingonBatCrInQuad to $newNumKbcIq" )
      numKlingonBatCrInQuad = newNumKbcIq

      if ( numKlingonBatCrInQuad == 0 ) {
          resetQuadrant()
      }
    }

    void resetQuadrant() { ///> Forget all Klingon ship data for the quadrant.
      log.trace 'Removing all battle cruisers & scrap from quadrant.'
      1.upto( maxKlingonBCinQuad ) { shipNum ->
        if ( shipExists( shipNum ) ) {
          launchIntoStar shipNum
        }
      }
      scrapHeap.clear()
    }

    // @TypeChecked
    boolean isShipAt( List<Integer> key ) {
      klingons.find {
        key.first() == it[rowIdx] && key.last() == it[colIdx]
      }
    }

    void positionInSector( final int klingonShipNo, final List<Integer>klingonPosition ) {
      assert klingonShipNo in 0..numKlingonBatCrInQuad
      assert sectorIsInsideQuadrant( klingonPosition )

      /// @pre Target sector must be empty
      assert klingons.find {
        it[rowIdx] == klingonPosition[0] && it[colIdx] == klingonPosition[1]
      } == null

      log.debug "Klingon $klingonShipNo is at sector " +
        GameSpace.logFmtCoords( *klingonPosition )
      /// @todo replace array with new EnemyShip class.
      klingons[klingonShipNo][idIdx]     = klingonShipNo
      klingons[klingonShipNo][rowIdx]    = klingonPosition[0]
      klingons[klingonShipNo][colIdx]    = klingonPosition[1]
      klingons[klingonShipNo][energyIdx] = maxKlingonEnergy
    }

    @TypeChecked
    String toString() {
      "Enemy Bat C total, $numKlingonBatCrTotal, " +
      "remain: $numKlingonBatCrRemain, " +
      "in quad: $numKlingonBatCrInQuad\n" +
      "Bat Cru: ${klingons[1..maxKlingonBCinQuad]}"
    }

    @TypeChecked
    boolean canAttack() {
      numKlingonBatCrInQuad > 0
    }

    @TypeChecked
    private Coords2d shipCoords( final int shipNo ) {
      final int row = klingons[ shipNo ][rowIdx]
      final int col = klingons[ shipNo ][colIdx]
      new Coords2d( row, col )
    }

    /// Distance to target calculated via Pythagorous
    float distanceToTarget( final int shipNo, final Coords2d targetSectorCoords ) {
      final float distance = distanceBetween(
        shipCoords( shipNo ), targetSectorCoords
      )
      log.info(
        sprintf(
          'Ship %d in %d - %d is %1.3f sectors from target %d - %d',
          shipNo, *(klingons[shipNo][colIdx..rowIdx]),distance,
          targetSectorCoords.col, targetSectorCoords.row
        )
      )
      distance
    }

    @TypeChecked
    void attack( final Coords2d targetSector, Closure reportAttack ) {
      assert sectorIsInsideQuadrant( targetSector)
      assert canAttack()
      assert reportAttack != null
      assert klingons.count { it[rowIdx] && it[colIdx] } == numKlingonBatCrInQuad

      log.debug 'Attack with {} ships out of a max {} ships', // 1740 IF K3%>0 THEN GOSUB 2370
        numKlingonBatCrInQuad, maxKlingonBCinQuad
      log.debug "Target is in sector $targetSector"

      eachShipNo { int shipNo ->
        attackWithShip shipNo, targetSector, reportAttack
      }
      log.debug 'Finished attack run.'
    }

    @TypeChecked
    private void attackWithShip(
        final int shipNo, final Coords2d targetSector, Closure reportAttack ) {
      final int attackerEnergy = klingons[ shipNo ][energyIdx]
      log.trace 'Inspecting ship {} it has {} units of energy', shipNo, attackerEnergy

      if ( attackerEnergy > 0 ) {
        log.info 'Ship {} is attacking with energy level {}',
            shipNo, attackerEnergy
        final float distance = distanceToTarget( shipNo, targetSector )
        final int hitWithEnergy = energyHittingTarget( attackerEnergy, distance )

        log.info "Fed Ship hit with $hitWithEnergy units of energy."
        reportAttack 'enemyFleet.hitOnFedShip', klingons[shipNo][rowIdx..colIdx]
      } else {
        log.trace "Ship $shipNo is dead or never existed."
      }
    }

    // @TypeChecked
    void shipHitByTorpedo( final Coords2d shipSector ) {
      log.info 'Ship at {} has been hit with a torpedo', shipSector
      int[] deadShip = klingons.find {
        it[rowIdx..colIdx] == shipSector.toList()
      }
      assert deadShip
      log.info "Dead ship details: $deadShip"
      scrapShip deadShip[idIdx]
    }

    @TypeChecked
    void hitOnShip( final int shipNum, final int hitAmount ) {
      log.info 'Fleet ship {} hit by {} units of Federation phasers',
        shipNum, hitAmount

      assert energy( shipNum ) > 0
      final int nrg = energy( shipNum ) - hitAmount
      klingons[ shipNum ][energyIdx] = [0,nrg].max()
      if (!shipExists(shipNum)) {
        scrapShip(shipNum)
      }
    }

    @TypeChecked
    void scrapShip( final int shipNum ) {
      assert shipNumIsValid( shipNum )
      scrapHeap << shipNum
      log.info "Ship $shipNum destroyed."
      log.info "There are ${scrapHeap.size()} scrapped ships in this quadrant."
      assert scrapHeap.size() in 1..maxKlingonBCinQuad
    }

    @TypeChecked
    void regroup() {
      log.info "${scrapHeap.size()} dead ships will be launched into a star."
      while ( scrapHeap.size() ) {
        removeShip scrapHeap.pop()
      }
    }

    @TypeChecked
    private void launchIntoStar( final int shipNum ) {
      assert shipNumIsValid( shipNum )
      log.debug "Launching ship $shipNum into a star."
      klingons[shipNum] = [shipNum,0,0,0]
    }

    @TypeChecked
    private void removeShip( final int shipNum ) {
      launchIntoStar shipNum
      --numKlingonBatCrInQuad
      --numKlingonBatCrRemain
    }

    @TypeChecked
    int energy( final int shipNum ) {
      assert shipNumIsValid( shipNum )
      klingons[ shipNum ][energyIdx]
    }

    @TypeChecked
    private boolean shipNumIsValid( final int shipNum ) {
      shipNum in 1..maxKlingonBCinQuad
    }

    @TypeChecked
    boolean shipExists( final int shipNum ) {
      assert shipNumIsValid( shipNum )
      klingons[ shipNum ][energyIdx] > 0
    }
}
