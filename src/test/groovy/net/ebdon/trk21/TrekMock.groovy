package net.ebdon.trk21;

import static GameSpace.*;
import groovy.transform.TypeChecked;

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
class TrekMock {

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

  @TypeChecked
  int getEntQuadX() {
    ship.position.quadrant.row
  }

  @TypeChecked
  void setEntQuadX( final int newPos ) {
    ship.position.quadrant.row = newPos
  }

  @TypeChecked
  int getEntSectX() {
    ship.position.sector.row
  }

  @TypeChecked
  void setEntSectX( final int newPos ) {
    ship.position.sector.row = newPos
  }

  @TypeChecked
  int getEntQuadY() {
    ship.position.quadrant.col
  }

  @TypeChecked
  void setEntQuadY( final int newPos ) {
    ship.position.quadrant.col = newPos
  }

  @TypeChecked
  int getEntSectY() {
    ship.position.sector.col
  }

  @TypeChecked
  void setEntSectY( final int newPos ) {
    ship.position.sector.col = newPos
  }

  String toString() {
    "Ship= $ship, Quadrant: [$entQuadX, $entQuadY], " +
    "Sector: [$entSectX, $entSectY], moveBlocked: $moveBlocked"
  }

  @groovy.transform.TypeChecked
  void blockedAtSector( final int row, final int column ) {
    moveBlocked = true
  }
}
