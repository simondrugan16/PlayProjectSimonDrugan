// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/simon.drugan/Documents/ScalaWorkplace/PlayProjectSimonDrugan/play-template/conf/routes
// @DATE:Thu Mar 16 12:00:15 GMT 2023


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
