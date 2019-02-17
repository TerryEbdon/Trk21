package net.ebdon.trk21;

import static GameSpace.*

@groovy.util.logging.Log4j2('logger')
class TrekMock {

  int entSectX = 0;
  int entSectY = 0;
  int entQuadX = 0;
  int entQuadY = 0;

  Galaxy galaxy;
  Quadrant quadrant;
  FederationShipMock ship;
  boolean moveBlocked = false;

  TrekMock() {
    ship        = new FederationShipMock()
    galaxy      = new Galaxy()
    quadrant    = new Quadrant()
    moveBlocked = false
    log.info "Constructed with " + toString()
  }

  String toString() {
    // "Calendar   : $game\n" +
    // "Ship       : $ship\n" +
    // "EnemyFleet : $enemyFleet\n" +
    // "Quadrant   : [$entQuadX, $entQuadY]\n" +
    // "Sector     : [$entSectX, $entSectY]"

    "Ship= $ship, Quadrant: [$entQuadX, $entQuadY], " +
    "Sector: [$entSectX, $entSectY], moveBlocked: $moveBlocked"
  }

  void blockedAtSector( row, col ) {
    log.info "Move blocked by object at sector ${logFmtCoords( row, col )}"
    quadrant.dump()
    moveBlocked = true
  }
}
