package info.hupel.example

import scala.concurrent._
import scala.concurrent.duration._

import info.hupel.isabelle._
import info.hupel.isabelle.api._
import info.hupel.isabelle.setup._

import shapeless.tag

import io.rbricks.scalog._

import monix.execution.Scheduler.Implicits.global

import caseapp._

case class ExampleApp(
  verbose: Boolean,
  version: String = "2016"
) extends App {

  val level = if (verbose) Level.Debug else Level.Warn
  LoggingBackend.console("info.hupel" -> level)

  val Pretty = Operation.implicitly[String, String]("pretty")

  val setup: Setup = Setup.default(Version(version)) match {
    case Left(err) =>
      Console.err.println("Could not construct setup.")
      Console.err.println(s"Reason: ${err.explain}")
      sys.exit(1)
    case Right(setup) => setup
  }

  val arg = remainingArgs.headOption.getOrElse {
    Console.err.println("Missing argument to pretty-print.")
    sys.exit(1)
  }

  val resources = Resources.dumpIsabelleResources() match {
    case Left(err) =>
      Console.err.println("Could not dump resources.")
      Console.err.println(s"Reason: ${err.explain}")
      sys.exit(1)
    case Right(res) => res
  }

  val config = Configuration.simple("Example")

  def build(env: Environment) =
    if (!System.build(env, config)) {
      Console.err.println("Could not build session.")
      sys.exit(1)
    }

  val transaction =
    for {
      env <- setup.makeEnvironment(resources)
      _ = build(env)
      sys <- System.create(env, config)
      response <- sys.invoke(Pretty)(arg)
      _ = println(env.decode(tag[Environment.Raw].apply(response.unsafeGet)))
      () <- sys.dispose
    } yield ()

  Await.result(transaction, Duration.Inf)

}

object Example extends AppOf[ExampleApp]
