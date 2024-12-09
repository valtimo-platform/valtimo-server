package com.ritense.case_.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.semver4j.Semver

class SemverConverterTest {

     @Test
     fun `convert db string to semver`() {
         val converter = SemverConverter()

         val semver = converter.convertToEntityAttribute("1.0.0")
         assertEquals(Semver.of(1,0,0), semver)
     }

    @Test
    fun `convert semver to db string`() {
        val converter = SemverConverter()

        val semverString = converter.convertToDatabaseColumn(Semver.of(1,0,0))
        assertEquals("1.0.0", semverString)
    }
 }