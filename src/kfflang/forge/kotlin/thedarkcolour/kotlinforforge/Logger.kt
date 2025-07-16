package thedarkcolour.kotlinforforge

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.MarkerManager

/**
 * Logger field for KotlinForForge.
 *
 * Kept here instead of [KotlinForForge] because logger is used
 * before [KotlinModContainer] should initialize.
 */
internal val LOGGER = LogManager.getLogger()
internal val LOADING = MarkerManager.getMarker("LOADING")
