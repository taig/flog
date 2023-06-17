package io.taig.flog.stackdriver.grpc

import java.util.Map as JMap

import cats.effect.Sync
import com.google.cloud.MonitoredResource

object MonitoredResources {
  private def unsafeGetEnv(name: String): String = Option(System.getenv(name)).getOrElse("unknown")

  val global: MonitoredResource = MonitoredResource.newBuilder("global").build()

  def cloudRun[F[_]](implicit F: Sync[F]): F[MonitoredResource] = F.delay {
    MonitoredResource
      .newBuilder("cloud_run_revision")
      .setLabels(
        // format: off
        JMap.of(
          "service_name", unsafeGetEnv("K_SERVICE"),
          "revision_name", unsafeGetEnv("K_REVISION"),
          "configuration_name", unsafeGetEnv("K_CONFIGURATION")
        )
        // format: on
      )
      .build()
  }
}
