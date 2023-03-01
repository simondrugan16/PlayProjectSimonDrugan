
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

object example extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template1[DataModel,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*3.2*/(book: DataModel):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*4.1*/("""<head>
    <link rel="stylesheet" type="text/css" media="screen" href=""""),_display_(/*5.66*/routes/*5.72*/.Assets.versioned("/stylesheets/main.css")),format.raw/*5.114*/("""">
</head>
"""),_display_(/*7.2*/main("Find Book")/*7.19*/{_display_(Seq[Any](format.raw/*7.20*/("""
"""),format.raw/*8.1*/("""<h1 class="book">Google Book Api ProjectZZZZZZ</h1>
<ul class="book">
    <li class="book">"""),_display_(/*10.23*/book/*10.27*/.name),format.raw/*10.32*/("""</li>
    <li class="book">"""),_display_(/*11.23*/book/*11.27*/.description),format.raw/*11.39*/("""</li>
</ul>
""")))}))
      }
    }
  }

  def render(book:DataModel): play.twirl.api.HtmlFormat.Appendable = apply(book)

  def f:((DataModel) => play.twirl.api.HtmlFormat.Appendable) = (book) => apply(book)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: 2023-02-24T12:42:23.260
                  SOURCE: /Users/simon.drugan/Documents/ScalaWorkplace/PlayProjectSimonDrugan/play-template/app/views/example.scala.html
                  HASH: 4ef245903d1d4289877271cec718e97078ca47b8
                  MATRIX: 432->1|765->27|876->45|974->117|988->123|1051->165|1088->177|1113->194|1151->195|1178->196|1297->288|1310->292|1336->297|1391->325|1404->329|1437->341
                  LINES: 17->1|22->3|27->4|28->5|28->5|28->5|30->7|30->7|30->7|31->8|33->10|33->10|33->10|34->11|34->11|34->11
                  -- GENERATED --
              */
          