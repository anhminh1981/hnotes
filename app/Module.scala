import com.google.inject.AbstractModule

import play.api.Configuration
import play.api.Environment
import play.api.Mode
import services.DevData

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module(
  environment: Environment,
  configuration: Configuration) extends AbstractModule {

  override def configure() = {
    
    // in dev mode, will instantiate the DevData object
    if(environment.mode == Mode.Dev) {
      bind(classOf[DevData]).asEagerSingleton()
    }
  }

}
