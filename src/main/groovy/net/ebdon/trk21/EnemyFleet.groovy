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

    def klingons = new int[maxKlingonBCinQuad + 1][4]; ///< k%[] in TREK.BAS

    final softProbs   = [
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
      numKlingonBatCrTotal <= maxPossibleKlingonShips
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

    void resetQuadrant() { ///> Forget all Klingon position data for the quadrant.
        log.trace "Removing all battle cruisers from quadrant."
        1.upto(9) { klingon ->
            1.upto(3) { j ->
                klingons[ klingon ][j] = 0
            }
        }
    }

    void positionInSector( final klingonShipNo, final klingonPosition ) {
      assert klingonShipNo >= 0 && klingonShipNo <= numKlingonBatCrInQuad

      assert sectorIsInsideQuadrant( klingonPosition )
      // 0.upto(1) { /// @todo positionInSector() hardcodes the sector size.
      //   assert klingonPosition[it] >=0 && klingonPosition[it] <= 8
      // }

      /// @pre Target sector must be empty
      assert null == klingons.find {
        it[1] == klingonPosition[0] && it[2] == klingonPosition[1]
      }

      log.debug "Klingon $klingonShipNo is at sector " +
      GameSpace.logFmtCoords( *klingonPosition )
      /// @todo replace array with new EnemyShip class.
      klingons[klingonShipNo][1] = klingonPosition[0]
      klingons[klingonShipNo][2] = klingonPosition[1]
      klingons[klingonShipNo][3] = maxKlingonEnergy
    }

    String toString() {
      "Enemy Bat C total, $numKlingonBatCrTotal, " +
      "remain: $numKlingonBatCrRemain, " +
      "in quad: $numKlingonBatCrInQuad\n" +
      "Bat Cru: ${klingons[1..numKlingonBatCrInQuad]}"
    }

    boolean canAttack() {
      numKlingonBatCrInQuad > 0
    }

    /// Distance to target calculated via Pythagorous
    float distanceToTarget( final shipNo, final targetSectorCoords ) {
      final float distance = distanceBetween(
        klingons[ shipNo ][1..2], targetSectorCoords
      )
      log.info(
        "Ship $shipNo in ${klingons[shipNo][1..2]} " +
        "is $distance sectors from target in " +
        "${targetSectorCoords}"
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

      def rnd = new Random().nextFloat()
      ( ( energyReleased / distanceToTarget ) * ( 2 + rnd ) ) + 1
    }

    void attack( targetSectorCoords, reportAttack ) {
      assert sectorIsInsideQuadrant( targetSectorCoords)
      assert canAttack()
      assert reportAttack != null
      assert klingons.count { it[1] && it[2] } == numKlingonBatCrInQuad

      log.info "Fleet is beginning an attack with $numKlingonBatCrInQuad ships." // 1740 IF K3%>0 THEN GOSUB 2370
      log.info "Target is in sector $targetSectorCoords"
      1.upto( maxKlingonBCinQuad ) { shipNo ->
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
}
