package net.ebdon.trk21;
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

class TrekCli extends UiBase {

  private Scanner sc = new Scanner(System.in).useDelimiter('\n');

  static main( args ) {
      new TrekCli().run()
  }

  @Override void run() {
    trek = new Trek( this );
    trek.setupGame()
    trek.startGame()
    gameLoop()
  }

  void resetScanner() {
    sc = new Scanner(System.in).useDelimiter('\n');
  }
  /// @note Can't use System.console().readLine() with GroovyServ.
  private String getLine( final String prompt ) {
    printf prompt
    try {
      sc.next().toLowerCase()
    } catch ( NoSuchElementException ex ) { // Caused by user entering CTRL+Z
      ''
    }
  }

  /// @todo Localise gameLoop() messages.
  private gameLoop() {
    boolean finished
    while ( !finished ) {
      final String command = getLine( '%nCommand: ') ?: 'q' // CTRL+Z = Quit.
      switch ( command[0] ) {
        case 's': trek.shortRangeScan(); break
        case 'l': trek.longRangeScan(); break
        case 'c': trek.setCourse(); break
        case 't': trek.fireTorpedo(); break
        case 'p': trek.firePhasers(); break
        case 'd': trek.reportDamage();  break
        case 'q': outln '\nBye!\n'; finished = true; trek = null; break
        default: println 'What?'
      }

      if (!finished) {
        trek.with {
          if (gameWon()) {
            finished = true
            victoryDance()
          } else {
            if (gameLost()) {
              finished = true
              shipDestroyed()
            } else {
              assert gameContinues()
            }
          }
        }
      }
    }
  }

  @Override void outln( final String str ) {
    println "  $str"
  }

  @Override void setConditionText( final String displayableCondition ) {
    //outln "Condition: $displayableCondition" /// @todo localise setConditionText
  }

  @Override Float getFloatInput( final String prompt ) {
    String rv = ''
    while( !rv.isFloat() ) {
      rv = getLine( "  ${prompt}: " ) - '\r' ?: 0
      if ( !rv.isFloat() ) {
        outln pleaseEnterNumber()
      }
    }
    Float.parseFloat( rv )
  }
}
