package io.taig.flog.http4s

import cats.syntax.all.*
import io.circe.Encoder
import io.circe.JsonObject
import io.circe.syntax.*
import org.http4s.Headers
import org.http4s.Request
import org.http4s.Response

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
