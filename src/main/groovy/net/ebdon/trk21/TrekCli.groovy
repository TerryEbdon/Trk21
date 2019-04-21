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

  private final Scanner sc = new Scanner(System.in);

  static void main( String[] args ) {
      new TrekCli().run()
  }

  @Override void run() {
    sc.useDelimiter('\n')
    trek = new Trek( this );
    trek.setupGame()
    trek.startGame()
    gameLoop()
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
  private void gameLoop() {
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
        case 'n': trek.navComp();  break
        case 'q': outln '\nBye!\n'; finished = true; trek = null; break
        default: println 'What?'
      }

      if (!finished) {
        if (trek.gameWon()) {
          finished = true
          trek.victoryDance()
        } else {
          if (trek.gameLost()) {
            finished = true
            trek.shipDestroyed()
          } else {
            assert trek.gameContinues()
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

  @Override Float getFloatInput( final String promptId ) {
    String rv = ''
    while ( !rv.isFloat() ) {
      rv = getLine( "  ${getPrompt(promptId)}: " ) - '\r' ?: 0
      if ( !rv.isFloat() ) {
        outln pleaseEnterNumber()
      }
    }
    Float.parseFloat( rv )
  }
}
