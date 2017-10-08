package net.enilink.pkzs.web

import net.enilink.core.ModelSetManager
import net.enilink.lift.sitemap.Application
import net.enilink.lift.util.Globals
import net.liftweb.common.StringFunc.strToStringFunc
import net.liftweb.http.S
import net.liftweb.sitemap.Loc
import net.liftweb.sitemap.Loc._
import net.liftweb.sitemap.LocPath.stringToLocPath
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.SiteMap
import net.enilink.lift.sitemap.Menus

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class LiftModule {
  object Right extends MenuCssClass("pull-right")

  def sitemapMutator: SiteMap => SiteMap = {
    def profileText = Globals.contextUser.vend.getURI.localPart

    val entries = List[Menu](Menus.application("pkzs", List("pkzs"), List(
      Menu("pkzs.Home", S ? "Home") / "pkzs" / "index",
      Menu("pkzs.MasterData", S ? "Master data") / "pkzs" / "masterdata" >> QueryParameters(() => List(("model", PKZS.VOCAB.toString))),
      Menu("pkzs.ProcessBlueprints", S ? "Process blueprints") / "pkzs" / "processbps" >> QueryParameters(() => List(("model", PKZS.VOCAB.toString))),
      Menu("pkzs.Ressourcen", S ? "Resources") / "pkzs" / "resources" >> QueryParameters(() => List(("model", PKZS.VOCAB.toString))),
      Menu("pkzs.Formulas", S ? "Formulas") / "pkzs" / "nw" / "static" / "constraints" >> QueryParameters(() => List(("model", PKZS.VOCAB.toString))),
      Menu("pkzs.Vocabulary", S ? "Vocabulary") / "pkzs" / "static" / "ontology" >> QueryParameters(() => List(("model", PKZS.VOCAB.toString))),

      // /pkzs/static path to be visible
      Menu(Loc("pkzs.Static", Link(List("pkzs", "static"), true, "/pkzs/static/index"),
        "Static Content", Hidden)))))

    Menus.sitemapMutator(entries)
  }

  def boot {
    Globals.contextModelSet.vend map { ms =>
      try {
        ms.getUnitOfWork.begin
        ms.createModel(PKZS.VOCAB)
      } finally {
        ms.getUnitOfWork.end
      }
    }
  }
}