package io.taig.flog.http4s

import cats.data.OptionT
import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Resource, Sync}
import cats.implicits._
import fs2.{Chunk, Stream}
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Json, JsonObject}
import io.taig.flog.algebra.Logger
import io.taig.flog.data.Scope
import org.http4s.client.Client
import org.http4s._

object LoggingClient {
  val RequestScope = Scope.Root / "client" / "request"

  val ResponseScope = Scope.Root / "client" / "response"

  def apply[F[_]: Concurrent](
      client: Client[F],
      logger: Logger[F],
      logBody: Boolean = false
  ): Client[F] = response(request(client, logger, logBody), logger, logBody)

  def request[F[_]: Concurrent](
      client: Client[F],
      logger: Logger[F],
      logBody: Boolean
  ): Client[F] = Client[F] { request =>
    Resource.suspend {
      Ref[F].of(Vector.empty[Chunk[Byte]]).map { bytes =>
        val newBody = Stream
          .eval(bytes.get)
          .flatMap(Stream.emits(_).covary[F])
          .flatMap(Stream.chunk(_).covary[F])

        val changedRequest = request.withBodyStream(
          request.body
            .observe(_.chunks.evalMap(chunk => bytes.update(_ :+ chunk)))
            .onFinalizeWeak(
              log(logger)(request.withBodyStream(newBody), logBody)
            )
        )

        client.run(changedRequest)
      }
    }
  }

  def response[F[_]: Concurrent](
      client: Client[F],
      logger: Logger[F],
      logBody: Boolean
  ): Client[F] = Client { req =>
    client.run(req).flatMap { response =>
      Resource.suspend {
        Ref[F].of(Vector.empty[Chunk[Byte]]).map { bytes =>
          Resource.make(
            response
              .copy(body = response.body.observe(_.chunks.evalMap { chunks =>
                bytes.update(_ :+ chunks)
              }))
              .pure[F]
          ) { _ =>
            val newBody = Stream
              .eval(bytes.get)
              .flatMap(Stream.emits(_).covary[F])
              .flatMap(Stream.chunk(_).covary[F])

            log(logger)(response.withBodyStream(newBody), logBody)
          }
        }
      }
    }
  }

  private def log[F[_]: Sync](
      logger: Logger[F]
  )(message: Message[F], logBody: Boolean): F[Unit] = {
    val body = if (logBody) {
      val charset = message.charset.getOrElse(Charset.`UTF-8`)
      val isBinary = message.contentType.exists(_.mediaType.binary)
      val isJson = message.contentType.exists { contentType =>
        contentType.mediaType === MediaType.application.json ||
        contentType.mediaType === MediaType.application.`vnd.hal+json`
      }
      val isText = isJson || !isBinary

      if (isText)
        OptionT
          .liftF(message.bodyAsText(charset).compile.string)
          .filter(_.nonEmpty)
          .map(body => parse(body).getOrElse(Json.fromString(body)))
          .value
      else none[Json].pure[F]
    } else none[Json].pure[F]

    message match {
      case request: Request[F] =>
        body.flatMap { body =>
          logger.info(RequestScope, payload = encode(request, body))
        }
      case response: Response[F] =>
        body.flatMap { body =>
          logger.info(ResponseScope, payload = encode(response, body))
        }
    }
  }

  private def encode[F[_]](
      request: Request[F],
      body: Option[Json]
  ): JsonObject =
    JsonObject(
      "request" := JsonObject(
        "method" := request.method.renderString,
        "uri" := request.uri.renderString,
        "headers" := request.headers.toList.map(_.renderString),
        "body" := body
      )
    )

  private def encode[F[_]](
      response: Response[F],
      body: Option[Json]
  ): JsonObject =
    JsonObject(
      "response" := JsonObject(
        "status" := response.status.renderString,
        "headers" := response.headers.toList.map(_.renderString),
        "body" := body
      )
    )
}
