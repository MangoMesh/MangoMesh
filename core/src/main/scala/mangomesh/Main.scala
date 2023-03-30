package mangomesh

import cats.effect.*
import cats.syntax.all.*
import org.http4s.server.Router
import org.http4s.*, org.http4s.dsl.io.*, org.http4s.implicits.*
import com.comcast.ip4s.*
import org.http4s.ember.server.*

val service = HttpRoutes
  .of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Holis $name")
    case _ =>
      IO(Response(Status.Ok))
  }

val aggregate = Router(
  "/" -> service
).orNotFound

object Server extends IOApp.Simple:
  override def run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8090")
      .withHttpApp(aggregate)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
