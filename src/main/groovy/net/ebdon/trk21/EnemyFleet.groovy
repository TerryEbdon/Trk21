package net.ebdon.trk21;

import static net.ebdon.trk21.GameSpace.*;
/**
 * @file
 * @author      Terry Ebdon
 * @date        January 2019
 * @copyright
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
final class EnemyFleet /*extends LoggingBase */ {
    int numKlingonBatCrTotal  = 0; ///< k0% in TREK.BAS
    int numKlingonBatCrRemain = 0; ///< k9% in TREK.BAS
    int numKlingonBatCrInQuad = 0; ///< K3% in TREK.BAS

    final int maxKlingonBCinQuad = 9;
    final int maxPossibleKlingonShips = 64 * maxKlingonBCinQuad;

    /**
    Energy level assigned to each Klingon ship. This is reset every time
    the Enterprise enters a quadrant that contains Klingons. This is `S9%`
    in TREK.BAS.
    **/
    static final int maxKlingonEnergy = 200;

    def klingons    = new int[maxKlingonBCinQuad + 1][4]; ///< k%[] in TREK.BAS
    def scrapHeap   = []
    final softProbs = [
      0,
      0.0001,
      0.01,
      0.03,
      0.08,
      0.28,
      1.28,
      3.28,
      6.28,
      13.28 ]; ///< r[0..9] in TREK.BAS

    boolean isValid() {
      numKlingonBatCrTotal >= 0 &&
      numKlingonBatCrRemain >= 0 &&
      numKlingonBatCrInQuad >= 0 &&
      numKlingonBatCrInQuad <= numKlingonBatCrRemain &&
      numKlingonBatCrRemain <= numKlingonBatCrTotal &&
      numKlingonBatCrTotal <= maxPossibleKlingonShips &&
      scrapHeap.size() <= maxKlingonBCinQuad
    }

    boolean getDefeated() {
      assert isValid()
      numKlingonBatCrRemain == 0
    }

    void setNumKlingonBatCrRemain( final int newNumKbcRemain ) {
      assert newNumKbcRemain >= 0 && newNumKbcRemain <= numKlingonBatCrTotal
      numKlingonBatCrRemain = newNumKbcRemain
    }

    void setNumKlingonBatCrTotal( newNumKbcTotal ) {
      assert newNumKbcTotal >= 0 && newNumKbcTotal <= maxPossibleKlingonShips
      numKlingonBatCrTotal = newNumKbcTotal
    }

    void setNumKlingonBatCrInQuad( final int newNumKbcIq ) {
      assert newNumKbcIq >= 0 && newNumKbcIq <=9
      assert newNumKbcIq <= numKlingonBatCrRemain

      log.trace( "numKlingonBatCrInQuad changed from $numKlingonBatCrInQuad to $newNumKbcIq" )
      numKlingonBatCrInQuad = newNumKbcIq

      if ( numKlingonBatCrInQuad == 0 ) {
          resetQuadrant()
      }
    }

    void resetQuadrant() { ///> Forget all Klingon ship data for the quadrant.
      log.trace "Removing all battle cruisers & scrap from quadrant."
      1.upto( maxKlingonBCinQuad ) { shipNum ->
        if ( shipExists( shipNum ) ) {
          launchIntoStar shipNum
        }
      }
      scrapHeap.clear()
    }

    boolean isShipAt( key ) {
      klingons.find {
        key.first() == it[1] && key.last() == it[2]
      }
    }

    void positionInSector( final klingonShipNo, final klingonPosition ) {
      assert klingonShipNo >= 0 && klingonShipNo <= numKlingonBatCrInQuad
      assert sectorIsInsideQuadrant( klingonPosition )

      /// @pre Target sector must be empty
      assert null == klingons.find {
        it[1] == klingonPosition[0] && it[2] == klingonPosition[1]
      }

      log.debug "Klingon $klingonShipNo is at sector " +
        GameSpace.logFmtCoords( *klingonPosition )
      /// @todo replace array with new EnemyShip class.
      klingons[klingonShipNo][0] = klingonShipNo
      klingons[klingonShipNo][1] = klingonPosition[0]
      klingons[klingonShipNo][2] = klingonPosition[1]
      klingons[klingonShipNo][3] = maxKlingonEnergy
    }

    String toString() {
      "Enemy Bat C total, $numKlingonBatCrTotal, " +
      "remain: $numKlingonBatCrRemain, " +
      "in quad: $numKlingonBatCrInQuad\n" +
      "Bat Cru: ${klingons[1..maxKlingonBCinQuad]}"
    }

    boolean canAttack() {
      numKlingonBatCrInQuad > 0
    }

    /// Distance to target calculated via Pythagorous
    float distanceToTarget( final int shipNo, final Coords2d targetSectorCoords ) {
      final float distance = distanceBetween(
        klingons[ shipNo ][1..2], targetSectorCoords
      )
      log.info(
        sprintf(
          'Ship %d in %d - %d is %1.3f sectors from target %d - %d',
          shipNo, *(klingons[shipNo][2..1]),distance,
          targetSectorCoords.col, targetSectorCoords.row
        )
      )
      distance
    }

    /// @todo Move energyHittingTarget() into a new Galaxy or GamePhysics class?
    static float energyHittingTarget(
        final float energyReleased,
        final float distanceToTarget ) {

      assert energyReleased > 0 && energyReleased <= maxKlingonEnergy
      assert distanceToTarget > 0 &&
             distanceToTarget <= maxSectorDistance

      /// @todo: Same bug as was in PhaserControl - it's possible to
      /// hit the target with more energy than was fired at it.
      def rnd = new Random().nextFloat()
      ( ( energyReleased / distanceToTarget ) * ( 2 + rnd ) ) + 1
    }

    void attack( final Coords2d targetSectorCoords, reportAttack ) {
      assert sectorIsInsideQuadrant( targetSectorCoords)
      assert canAttack()
      assert reportAttack != null
      assert klingons.count { it[1] && it[2] } == numKlingonBatCrInQuad

      log.info "Fleet is beginning an attack with $numKlingonBatCrInQuad ships." // 1740 IF K3%>0 THEN GOSUB 2370
      log.info "Target is in sector $targetSectorCoords"
      1.upto( maxKlingonBCinQuad ) { int shipNo ->
        final int attackerEnergy = klingons[ shipNo ][3]
        if ( attackerEnergy > 0 ) {
          log.info "Ship $shipNo is attacking. It's energy level is: $attackerEnergy"

          final float distance = distanceToTarget( shipNo, targetSectorCoords )
          final int hitWithEnergy = energyHittingTarget( attackerEnergy, distance )

          reportAttack(
            hitWithEnergy,
            "Hit from Klingon at sector " +
              GameSpace.logFmtCoords( *(klingons[shipNo][1..2]) ) // Line 2410
          )
        } else {
          log.trace "Ship $shipNo is dead or never existed."
        }
      }
    }

    def hitOnShip( final int shipNum, final int hitAmount ) {
      log.info 'Fleet ship {} hit by {} units of Federation phasers',
        shipNum, hitAmount

      assert energy( shipNum ) > 0
      final int nrg = energy( shipNum ) - hitAmount
      klingons[ shipNum ][3] = [0,nrg].max()
      if (!shipExists(shipNum)) {
        scrapShip(shipNum)
      }
    }

    void scrapShip( final shipNum ) {
      scrapHeap << shipNum
      log.info "Ship $shipNum destroyed."
      log.info "There are ${scrapHeap.size()} scrapped ships in this quadrant."
      assert scrapHeap.size() <= maxKlingonBCinQuad
    }

    void regroup() {
      log.info "${scrapHeap.size()} dead ships will be launched into a star."
      while ( scrapHeap.size() ) {
        removeShip scrapHeap.pop()
      }
    }

    private void launchIntoStar(final shipNum) {
      log.debug "Launching ship $shipNum into a star."
      // 1.upto(3) {
        klingons[shipNum] = [shipNum,0,0,0]
      // }
    }
    private void removeShip(final shipNum) {
      launchIntoStar shipNum
      --numKlingonBatCrInQuad
      --numKlingonBatCrRemain
    }

    int energy( final int shipNum ) {
      assert shipNum >=1 && shipNum <= 9
      klingons[ shipNum ][3]
    }

    boolean shipExists(final int shipNum) {
      klingons[ shipNum ][3] > 0
    }
}
