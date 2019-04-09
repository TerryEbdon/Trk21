package net.ebdon.trk21;

import static Quadrant.Thing;

/**
 * @file
 * @author      Terry Ebdon
 * @date        March 2019
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

@groovy.transform.TypeChecked
final class QuadrantValue {
  private final int value;

  final int enemy;
  final int bases;
  final int stars;

  QuadrantValue( int val ) {
    value = val
    enemy = calcNumEnemy()
    bases = calcNumBases()
    stars = calcNumStars()
  }

  private int calcNumEnemy() {
    (value / Thing.enemy.multiplier).toInteger()
  }

  private int calcNumBases() {
    ( (value - enemy * Thing.enemy.multiplier) / Thing.base.multiplier ).toInteger()
  }

  private int calcNumStars() {
    value - enemy * Thing.enemy.multiplier - bases * Thing.base.multiplier
  }

  @groovy.transform.TypeChecked( groovy.transform.TypeCheckingMode.SKIP )
  int num( Thing thing ) {
    assert thing.multiplier
    this.((thing.name() + 's')[0..4])
  }
}
