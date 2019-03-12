package net.ebdon.trk21;

import groovy.swing.SwingBuilder;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
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

/**
@brief  GUI for A Groovy version of the 1973 BASIC-PLUS program TREK.BAS
@author Terry Ebdon
@date   JAN-2019
*/
@groovy.util.logging.Log4j2
final class TrekGui extends UiBase {
  def swing = new SwingBuilder();

  static main( args ) {
      new TrekGui().run()
  }

  @Override void outln( final String str ) {
    swing.output.text += "$str\n"
  }

  @Override void setConditionText( final String displayableCondition ) {
    swing.condition.text = displayableCondition
  }

  def setDefaultFonts() {
    // see: https://www.rgagnon.com/javadetails/java-0335.html
    // Font f2 = new Font( 'monospaced', Font.BOLD, 20 ) // 28 )
    Font f2 = new Font( *(config.gui.font.standard) )
    java.util.Enumeration keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      Object value = UIManager.get (key);
      if (value instanceof javax.swing.plaf.FontUIResource) {
        UIManager.put (key, f2);
      }
    }
  }

  @Override Float getFloatInput( final String prompt ) {
    String rv = ''
    while( !rv.isFloat() ) {
      rv = JOptionPane.showInputDialog( swing.trekFrame, btnText( prompt ) )
      if ( rv ) {
        if ( !rv.isFloat() ) {
          JOptionPane.showMessageDialog( swing.trekFrame, pleaseEnterNumber() )
        }
      } else {
        rv = '0'
      }
    }
    Float.parseFloat( rv )
  }

  /// @return A localised display string for the ship's condition
  /// @todo Move displayableCondition() into FederationShip
  /// @todo Localise via the #rb resource bundle.
  /// @bug  Code assumes that all condition values, other than "DOCKED",
  ///       are also valid HTML colours. This is currently true for ENGLISH
  ///       locales, but will fail for other languages. Colours and language
  ///       should be orthogonal.
  /// @todo Consider splitting into two methods, to allow use where HTML is
  ///       not appropriate.
  String displayableCondition() {
    trek.ship.with {
      /// @bug Can be condition GREEN without being docked.
      ///       e.g. if energy > 10% && no enemy in current
      ///       or surrounding sectors.
      /// @todo Font size should be based on cong, not HTML.
      def colour = condition != 'DOCKED' ? condition : 'GREEN'
      "<html><font size=+2 color=$colour>${condition}</font></html>"
    }
  }

  @Override void run() {
    // assert damage
    trek.setupGame()

    log.info "Message font: ${config.gui.font.message}"
    Font f2 = new Font( *(config.gui.font.message) )
    setDefaultFonts()

    javax.swing.UIManager.put("OptionPane.messageFont", f2 );
    swing.edt {
      frame(id: 'trekFrame', title: 'Trek GUI', defaultCloseOperation: JFrame.EXIT_ON_CLOSE, pack: true, show: true) {
        vbox {
          panel( id: 'btnPanel' ) {
            gridLayout( cols: 2, rows: 0 )

            button( text: btnText('Short Ranger Sensors'), actionPerformed: { trek.shortRangeScan() } )
            button( text: btnText('Long Ranger Sensors'), actionPerformed: { trek.longRangeScan() } )

            button( text: btnText('Set Course'), actionPerformed: { trek.setCourse() } )
            button( text: btnText('Damage Control'), actionPerformed: { trek.reportDamage() } )

            button( text: btnText('Fire torpedo'), actionPerformed: { trek.fireTorpedo() } )
            button( text: btnText('Fire phasers'), actionPerformed: { trek.firePhasers() } )

            button( id:'condition', text: displayableCondition() )
          }

          scrollPane( border:BorderFactory.createRaisedBevelBorder() ) {
            textArea( id: 'output' ) //, text: '\n' * 20 )
          }
        }
      }
    }

    trek.startGame()
    // log.info "** Game Over **"
    return
  }
}
