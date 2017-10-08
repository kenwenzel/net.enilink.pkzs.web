package net.enilink.pkzs.web

import net.enilink.komma.core.URIs
import net.liftweb.common.Failure
import net.liftweb.common.Box
import net.enilink.komma.core.URI
import net.liftweb.http.S

object PKZS {
  val VOCAB = URIs.createURI("http://enilink.net/eniprod/pkzs")

  val PROJECTS = URIs.createURI("http://enilink.net/pkzs/projects").appendSegment("")
}

object SnippetHelpers {
  implicit def uriToStr(uri: URI) = uri.toString
  def paramNotEmpty(name: String, msg: String) = S.param(name).map(_.trim).filterMsg(name + ":" + msg)(!_.isEmpty)

  def doAlert[T](box: Box[T]) = box match {
    case f @ Failure(msg, _, _) =>
      msg.split(":", 2) match {
        case Array(name, alert) => S.error("alert-" + name, alert)
        case _ => S.error(msg)
      }; f
    case n => n
  }
}