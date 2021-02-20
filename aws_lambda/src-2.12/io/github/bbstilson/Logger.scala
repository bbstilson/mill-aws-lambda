package io.github.bbstilson

object Logger {

  val logger = new mill.util.PrintLogger(
    true,
    false,
    ammonite.util.Colors.Default,
    System.out,
    System.err,
    System.err,
    System.in,
    debugEnabled = false,
    useContext = true
  )

  val prefix = logger
}
