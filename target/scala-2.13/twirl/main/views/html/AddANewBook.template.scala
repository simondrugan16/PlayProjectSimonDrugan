
package views.html

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._
/*1.2*/import models.DataModel
/*2.2*/import helper._

object AddANewBook extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template3[Form[DataModel],RequestHeader,Messages,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*4.2*/(bookForm: Form[DataModel])(implicit request: RequestHeader, messages: Messages):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*5.1*/("""
"""),_display_(/*6.2*/main("Add Book")/*6.18*/{_display_(Seq[Any](format.raw/*6.19*/("""
"""),format.raw/*7.1*/("""<div>
    """),_display_(/*8.6*/helper/*8.12*/.form(action = routes.ApplicationController.addBookForm())/*8.70*/ {_display_(Seq[Any](format.raw/*8.72*/("""
    """),_display_(/*9.6*/helper/*9.12*/.CSRF.formField),format.raw/*9.27*/("""
    """),_display_(/*10.6*/helper/*10.12*/.inputText(bookForm("_id"))),format.raw/*10.39*/("""
    """),_display_(/*11.6*/helper/*11.12*/.inputText(bookForm("name"))),format.raw/*11.40*/("""
    """),_display_(/*12.6*/helper/*12.12*/.inputText(bookForm("description"))),format.raw/*12.47*/("""
    """),_display_(/*13.6*/helper/*13.12*/.inputText(bookForm("numSales"))),format.raw/*13.44*/("""
    """),format.raw/*14.5*/("""<input class="submitButton" type="submit" value="Submit">
    """)))}),format.raw/*15.6*/("""
"""),format.raw/*16.1*/("""</div>
""")))}))
      }
    }
  }

  def render(bookForm:Form[DataModel],request:RequestHeader,messages:Messages): play.twirl.api.HtmlFormat.Appendable = apply(bookForm)(request,messages)

  def f:((Form[DataModel]) => (RequestHeader,Messages) => play.twirl.api.HtmlFormat.Appendable) = (bookForm) => (request,messages) => apply(bookForm)(request,messages)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: 2023-02-21T16:22:16.467
                  SOURCE: /Users/simon.drugan/Documents/ScalaWorkplace/PlayProjectSimonDrugan/play-template/app/views/AddANewBook.scala.html
                  HASH: 9a98ee13b561ceb299916915654cc8539c3480d7
                  MATRIX: 432->1|463->26|821->44|995->125|1022->127|1046->143|1084->144|1111->145|1147->156|1161->162|1227->220|1266->222|1297->228|1311->234|1346->249|1378->255|1393->261|1441->288|1473->294|1488->300|1537->328|1569->334|1584->340|1640->375|1672->381|1687->387|1740->419|1772->424|1865->487|1893->488
                  LINES: 17->1|18->2|23->4|28->5|29->6|29->6|29->6|30->7|31->8|31->8|31->8|31->8|32->9|32->9|32->9|33->10|33->10|33->10|34->11|34->11|34->11|35->12|35->12|35->12|36->13|36->13|36->13|37->14|38->15|39->16
                  -- GENERATED --
              */
          