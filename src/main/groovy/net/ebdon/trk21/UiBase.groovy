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

abstract class UiBase extends LoggingBase { /// @todo LoggingBase required for config.
  Trek  trek;

  abstract void run();
  abstract void outln( final String str );
  abstract void setConditionText( final String displayableCondition );
  abstract Float getFloatInput( final String promptId );
  /// @deprecated Not needed with new font config.
  final String btnText( final String text ) {
    // "<html><font size=+3>$text</font></html>"
    text
  }

  @groovy.transform.TypeChecked
  String getPrompt( final String key ) {
    trek.rb.getString key
  }

  void fmtMsg( final String formatId, final List args ) {
    outln String.format( getPrompt( formatId ), *args )
  }

  final String pleaseEnterNumber() {
    getPrompt( 'input.err.enterNumber' )
  }

  @groovy.transform.TypeChecked
  void localMsg( final String msgId ) {
    outln getPrompt( msgId )
  }

  @groovy.transform.TypeChecked
  void localMsg( final String msgId, Object[] msgArgs ) {
    trek.formatter.applyPattern( getPrompt( msgId ) )
    outln trek.formatter.format( msgArgs )
  }
}
