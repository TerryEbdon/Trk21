package net.ebdon.trk21;

/**
 * @file
 * @author      Terry Ebdon
 * @date        March 2019
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
@groovy.transform.TypeChecked
@groovy.transform.ToString(includePackage=false)
final class TestUi extends UiBase {
  List<Float>  inputValues = [];
  List<String> msgLog = [];
  List<String> localMsgLog = [];
  String conditionText;

  void outln( String str ) {
    msgLog << str
  }

  void localMsg( final String msgId ) {
    localMsgLog << msgId
  }

  @SuppressWarnings('UnusedMethodParameter')
  void localMsg( final String msgId, Object[] msgArgs ) {
    localMsgLog << msgId
  }

  Float getFloatInput( final String prompt ) {
    assert prompt.length()
    inputValues.pop()
  }

  @Override
  void run() { }
}