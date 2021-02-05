package io.taig.flog.http4s

import cats.syntax.all._
import io.taig.flog.Encoder
import io.taig.flog.data.Payload
import io.taig.flog.syntax._
import org.http4s.{Headers, Method, Request, Response, Uri}

object implicits {
  implicit val encoderMethod: Encoder[Method] = Encoder[String].contramap(_.show)

  implicit val encoderUri: Encoder[Uri] = Encoder[String].contramap(_.show)

  implicit val encoderHeaders: Encoder[Headers] = Encoder[Map[String, String]]
    .contramap(_.toList.map(header => (header.name.show, header.value)).toMap)

  implicit def encoderRequest[F[_]]: Encoder.Object[Request[F]] = request =>
    Payload.of(
      "request" := Payload.of(
        "method" := request.method.renderString,
        "uri" := request.uri.renderString,
        "headers" := request.headers
      )
    )

  implicit def encoderResponse[F[_]]: Encoder.Object[Response[F]] = response =>
    Payload.of(
      "response" := Payload.of(
        "status" := response.status.renderString,
        "headers" := response.headers
      )
    )
}
