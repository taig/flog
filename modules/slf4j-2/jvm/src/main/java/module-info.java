module org.slf4j.flog {
  requires org.slf4j;
  provides org.slf4j.spi.SLF4JServiceProvider with io.taig.flog.slf4j2.FlogSlf4j2ServiceProvider;
}