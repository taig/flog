package io.taig.flog.http4s

import cats.syntax.all._
import io.circe.syntax._
import io.circe.{Encoder, JsonObject}
import org.http4s.{Headers, Request, Response}

object implicits {
  implicit private val encoderHeaders: Encoder[Headers] = Encoder[Map[String, String]]
    .contramap(_.headers.map(header => (header.name.show, header.value)).toMap)

  implicit def encoderRequest[F[_]]: Encoder.AsObject[Request[F]] = request =>
    JsonObject(
      "request" := JsonObject(
        "method" := request.method.renderString,
        "uri" := request.uri.renderString,
        "headers" := request.headers
      )
    )

  implicit def encoderResponse[F[_]]: Encoder.AsObject[Response[F]] = response =>
    JsonObject(
      "response" := JsonObject(
        "status" := response.status.renderString,
        "headers" := response.headers
      )
    )
}
