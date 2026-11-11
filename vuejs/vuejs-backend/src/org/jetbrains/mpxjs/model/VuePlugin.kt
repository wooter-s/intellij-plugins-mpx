package org.jetbrains.mpxjs.model

interface VuePlugin : VueContainer {
  val plugins: List<VuePlugin>
}
