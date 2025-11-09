package net.ebdon.trk21.arms_man;

import groovy.test.GroovyTestCase;
import groovy.mock.interceptor.MockFor;
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError;

import net.ebdon.trk21.Quadrant;
import net.ebdon.trk21.EnemyFleet;
import net.ebdon.trk21.FederationShip;
import net.ebdon.trk21.DamageControl;
import net.ebdon.trk21.TestUi;
import net.ebdon.trk21.Battle;
/**
 * @file
 * @author      Terry Ebdon
 * @date        April 2019
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

@groovy.util.logging.Log4j2('logger')
@Newify(MockFor)
// @groovy.transform.TypeChecked
final class TorpedoManagerTest extends GroovyTestCase {
  private static final float minTorpedoCourse = 1.0F
  private static final float maxTorpedoCourse = 9.0F
  private TestUi ui;
  private MockFor battleMock;
  private MockFor shipMock;
  private MockFor fleetMock;
  private MockFor quadMock;
  private MockFor dcMock;

  @Override void setUp() {
    super.setUp()
    ui = new TestUi()
    battleMock = MockFor( Battle )
    shipMock   = MockFor( FederationShip )
    fleetMock  = MockFor( EnemyFleet )
    dcMock     = MockFor( DamageControl )
    quadMock   = MockFor( Quadrant )
  }

  void testFireCancel() {
    ui.inputValues = [0F]
    boolean fired = false
    battleMock.use { // No demands, shouldn't be accessed
      shipMock.use { // No demands, shouldn't be accessed
        quadMock.use {
          TorpedoManager tm = new TorpedoManager(
            ui,
            new EnemyFleet(),
            new FederationShip(),
            new DamageControl()
          )
          fired = tm.fire( new Quadrant() )
        }
      }
    }
    assert ui.empty
    assert fired == false
  }

  void testFireNoTorpedoes() {
    ui.inputValues = [1F]
    boolean fired = false
    battleMock.use { // No demands, shouldn't be accessed
      shipMock.demand.getNumTorpedoes { 0 }
      shipMock.use {
        quadMock.use {
          TorpedoManager tm = new TorpedoManager(
            ui,
            new EnemyFleet(),
            new FederationShip(),
            new DamageControl()
          )
          fired = tm.fire( new Quadrant() )
        }
      }
    }
    assert ui.localMsgLog == [ 'torpedo.unavailable' ]
    assert fired == false
  }

  void testFireGood() {
    ui.inputValues = [1F]
    boolean fired = false
    battleMock.demand.with {
      fireTorpedo { course, quadrant ->
        assert course >= minTorpedoCourse && course < maxTorpedoCourse
        assert quadrant != null
      }
      getThingDestroyed { Quadrant.Thing.emptySpace }
    }
    battleMock.use {
      shipMock.demand.getNumTorpedoes { 1 }
      shipMock.use {
        quadMock.use {
          TorpedoManager tm = new TorpedoManager(
            ui,
            new EnemyFleet(),
            new FederationShip(),
            new DamageControl()
          )
          fired = tm.fire( new Quadrant() )
        }
      }
    }
    assert ui.empty
    assert fired == true
  }

  void testFireNullQuadrant() {
    final float torpedoCourse = 1.0F
    ui.inputValues = [torpedoCourse]
    boolean fired = false
    battleMock.use {
      shipMock.demand.getNumTorpedoes(0) { }
      shipMock.use {
        quadMock.use {
          TorpedoManager tm = new TorpedoManager(
            ui,
            new EnemyFleet(),
            new FederationShip(),
            new DamageControl()
          )
          shouldFail( PowerAssertionError ) {
            fired = tm.fire( null )
          }
        }
      }
    }
    assert ui.inputValues == [torpedoCourse]
    assert fired == false
  }
}
