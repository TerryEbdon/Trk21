package net.ebdon.trk21;

import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager;
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
abstract class LoggingBase {

  static config;
  static final String gameConfigFile  = 'TrekGameConfig.groovy';
  static final String log4jConfigFile = 'TrekLogConfig.groovy';

  LoggingBase() {
    // super
    if ( config == null ) {
      //~ loadConfig(log4jConfigFile)
      // println "Loading config from $gameConfigFile"
      // config = new ConfigSlurper().parse(new File( configFile ).toURL())
      //
      // println "Logging config: " + config.Trek.log4j + "\n"
      // println "All config: " + config + "\n"
      //~ PropertyConfigurator.configure(
        //~ loadConfig(log4jConfigFile).Trek.toProperties()
      //~ )
      config = loadConfig( gameConfigFile ).Trek
      // println "repositioner config: " + config.Repositioner
    } else {
      // println "Config already loaded."
    }
  }

  private def loadConfig( configFile ) {
     log.trace "Loading config from $configFile"
     log.trace "Current folder is " + new File('.').absolutePath
     log.trace "package:  " + getClass().packageName
     log.trace "Class is: " + getClass().name

    ClasspathResourceManager resourceManager = new ClasspathResourceManager()
    def configScript = resourceManager.getReader(configFile)
    def retConfig = null
    if ( configScript ) {
        final String scriptText = configScript.text
        log.trace "---\n${scriptText}\n---\n"
        retConfig = new ConfigSlurper().parse( scriptText )
        log.trace "\nconfig:\n${config}\n"
    } else {
        log.fatal "Couldn't load resource for configuration script."
        System.exit(0)
    }
    log.trace 'Resource loaded'
//    System.exit(0)
    retConfig
  }
  void logException( closure ) {
    try {
      closure.call()
    } catch ( Throwable ex ) {
      log.fatal ex.message
      ex.stackTrace.each {
        if ( it.fileName && it.fileName.contains( '.groovy' ) ) {
          log.error '>>' + it.methodName +
              '\t' + it.fileName + ':' + it.lineNumber
        }
      }
      throw ex
    }
  }
}
