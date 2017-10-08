package net.enilink.pkzs.web.snippet

import scala.collection.immutable.Nil

import net.enilink.komma.core.IEntity
import net.enilink.komma.core.URIs
import net.enilink.komma.model.concepts.Model
import net.enilink.lift.util.AjaxHelpers
import net.enilink.lift.util.CurrentContext
import net.enilink.lift.util.Globals
import net.enilink.lift.util.TemplateHelpers
import net.enilink.pkzs.web.PKZS
import net.enilink.pkzs.web.SnippetHelpers.doAlert
import net.enilink.pkzs.web.SnippetHelpers.paramNotEmpty
import net.enilink.pkzs.web.SnippetHelpers.uriToStr
import net.enilink.vocab.rdf.RDF
import net.liftweb.common.Box.box2Iterable
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JE.Str
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JsCmds.Run
import net.liftweb.util.ClearNodes
import net.liftweb.util.Helpers.strToCssBindPromoter

class Projects {
  val model = Globals.contextModel.vend

  def create = SHtml.hidden(() => {
    val result = for {
      name <- doAlert(paramNotEmpty("name", "Bitte einen Namen eingeben."))
      architecture <- doAlert(paramNotEmpty("architecture", "Bitte eine Architektur auswählen."))
      project <- model map (_.resolve(PKZS.PROJECTS.appendLocalPart(name)))
    } yield CurrentContext.withSubject(project) {
      val projectType = PKZS.VOCAB.appendLocalPart("Project")
      TemplateHelpers.render(<span about="?this" typeof={ projectType } data-lift="rdfa"></span>) flatMap {
        case (ns, _) if ns.isEmpty =>
          val archUri = URIs.createURI(architecture)
          project.addProperty(RDF.PROPERTY_TYPE, projectType)
          //project.addProperty(XIL.SYSTEM.appendLocalPart("architecture"), archUri)
          val projectModel = project.getModel.getModelSet.createModel(project.getURI)
          projectModel.asInstanceOf[Model].setModelLoaded(true)
          //projectModel.addImport(XIL.PROJECTS, null);
          projectModel.addImport(archUri, null);
          //projectModel.addImport(XIL.MODELS, null);
          Full(Run("if (typeof postCreate === 'function') postCreate('" + project.getURI + "')"))
        case _ =>
          S.error("alert-name", "Ein Projekt mit diesem Namen existiert bereits.")
          Empty
      } openOr Noop
    }
    result openOr Noop
  }: JsCmd)

  def delete = CurrentContext.value.map { c =>
    AjaxHelpers.onEvents("onclick" :: Nil, _ => {
      val project = c.subject.asInstanceOf[IEntity]
      val t = project.getEntityManager.getTransaction
      val result =
        try {
          for {
            m <- model
            modelSet = m.getModelSet
            projectModel <- Option(modelSet.getModel(project.getURI, false))
          } {
            projectModel.getManager.clear
            modelSet.getModels.remove(projectModel)
            modelSet.getMetaDataManager.remove(projectModel)
          }
          t.begin
          project.getEntityManager.removeRecursive(project, true)
          t.commit
          Full(project.getURI)
        } catch {
          case _ : Throwable => if (t.isActive) t.rollback; Empty
        }
      Run("if (typeof postDelete === 'function') " +
        Call("postDelete", result.toSeq flatMap { uri => List(Str(uri.localPart), Str(uri.toString)) }: _*).toJsCmd)
    }) andThen "a [href]" #> "javascript://"
  } openOr ClearNodes
}