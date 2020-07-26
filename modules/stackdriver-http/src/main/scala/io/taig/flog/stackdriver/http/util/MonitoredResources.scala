package io.taig.flog.stackdriver.http.util

import cats.effect.Sync
import com.google.api.services.logging.v2.model.MonitoredResource

object MonitoredResources {
  private def unsafeGetEnv(name: String): String = Option(System.getenv(name)).getOrElse("unknown")

  val global: MonitoredResource = new MonitoredResource().setType("global")

  def cloudRun[F[_]](implicit F: Sync[F]): F[MonitoredResource] = F.delay {
    new MonitoredResource()
      .setType("cloud_run_revision")
      .setLabels(
        java.util.Map.of(
          "service_name",
          unsafeGetEnv("K_SERVICE"),
          "revision_name",
          unsafeGetEnv("K_REVISION"),
          "configuration_name",
          unsafeGetEnv("K_CONFIGURATION")
        )
      )
  }
}
