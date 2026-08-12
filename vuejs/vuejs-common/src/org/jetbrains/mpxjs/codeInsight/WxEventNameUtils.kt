// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.mpxjs.codeInsight

/**
 * Utility object for converting between WeChat Mini Program event binding syntaxes.
 *
 * WeChat Mini Program supports two equivalent syntaxes for event binding:
 * - Merged syntax: `bindtap`, `catchtouchstart`, `capture-bindtap`
 * - Colon syntax: `bind:tap`, `catch:touchstart`, `capture-bind:tap`
 *
 * This utility provides conversion between these two formats.
 */
object WxEventNameUtils {

  /**
   * Event prefix pairs, sorted by length in descending order to ensure correct matching.
   * Format: Pair(merged prefix, colon prefix)
   */
  private val EVENT_PREFIXES = listOf(
    "capture-catch" to "capture-catch:",
    "capture-bind" to "capture-bind:",
    "mut-bind" to "mut-bind:",
    "catch" to "catch:",
    "bind" to "bind:"
  )

  /**
   * Checks if the given attribute name is a WeChat event attribute.
   *
   * @param name The attribute name to check
   * @return true if it's a WeChat event attribute (either syntax)
   */
  fun isWxEventAttribute(name: String): Boolean {
    return EVENT_PREFIXES.any { (merged, colon) ->
      // Merged syntax: must have characters after the prefix and not already be colon syntax
      (name.startsWith(merged) && name.length > merged.length && !name.startsWith(colon)) ||
      // Colon syntax: must start with colon prefix
      name.startsWith(colon)
    }
  }

  /**
   * Gets all equivalent variants of the given event attribute name.
   * Returns merged syntax first, then colon syntax.
   *
   * @param name The event attribute name (either syntax)
   * @return List of all equivalent variants, merged syntax first
   */
  fun getAllVariants(name: String): List<String> {
    if (!isWxEventAttribute(name)) return listOf(name)

    val merged = toMergedSyntax(name) ?: name
    val colon = toColonSyntax(merged)

    return if (colon != null) {
      listOf(merged, colon)
    } else {
      listOf(merged)
    }
  }

  /**
   * Converts merged syntax to colon syntax.
   * Example: `bindtap` → `bind:tap`
   *
   * @param name The attribute name in merged syntax
   * @return The colon syntax, or null if not a valid merged syntax
   */
  fun toColonSyntax(name: String): String? {
    for ((merged, colon) in EVENT_PREFIXES) {
      if (name.startsWith(merged) && name.length > merged.length && !name.startsWith(colon)) {
        val eventName = name.substring(merged.length)
        return "$colon$eventName"
      }
    }
    return null
  }

  /**
   * Converts colon syntax to merged syntax.
   * Example: `bind:tap` → `bindtap`
   *
   * @param name The attribute name in colon syntax
   * @return The merged syntax, or null if not a valid colon syntax
   */
  fun toMergedSyntax(name: String): String? {
    for ((merged, colon) in EVENT_PREFIXES) {
      if (name.startsWith(colon)) {
        val eventName = name.substring(colon.length)
        return "$merged$eventName"
      }
    }
    return null
  }

  /**
   * Normalizes the event attribute name to merged syntax.
   *
   * @param name The event attribute name (either syntax)
   * @return The normalized name in merged syntax
   */
  fun normalize(name: String): String {
    return toMergedSyntax(name) ?: name
  }

  /**
   * Extracts the event name from either supported attribute syntax.
   */
  fun getEventName(name: String): String? {
    for ((merged, colon) in EVENT_PREFIXES) {
      if (name.startsWith(colon)) {
        return name.substring(colon.length).takeIf { it.isNotEmpty() }
      }
      if (name.startsWith(merged) && name.length > merged.length) {
        return name.substring(merged.length)
      }
    }
    return null
  }

  /**
   * Checks if two event attribute names are equivalent.
   *
   * @param name1 First attribute name
   * @param name2 Second attribute name
   * @return true if they represent the same event binding
   */
  fun areEquivalent(name1: String, name2: String): Boolean {
    return normalize(name1) == normalize(name2)
  }
}
