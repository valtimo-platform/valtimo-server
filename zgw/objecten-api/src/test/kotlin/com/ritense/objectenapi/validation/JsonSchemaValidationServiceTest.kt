package com.ritense.objectenapi.validation

import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class JsonSchemaValidationServiceTest {

    private val validatorService = JsonSchemaValidationService()
    private val mapper = MapperSingleton.get()

    @Test
    fun `should validate with errors`() {
        val data = """
            {
                "productName": null,
                "releaseDate": "invalid",
                "unknownProperty": 1
            }
        """.trimIndent()

        val exception = assertThrows<JsonSchemaValidationService.JsonSchemaValidationException> {
            validatorService.validate(
                jsonSchema = mapper.readTree(schema),
                data = mapper.readTree(data)
            )
        }

        assertThat(exception.messages).hasSize(4)
        assertThat(exception.messages.map { Pair(it.property ?: it.instanceLocation.toString(), it.type) }).containsExactlyInAnyOrder(
                Pair("/productName", "type"),
                Pair("/releaseDate", "format"),
                Pair("vendorName", "required"),
                Pair("unknownProperty", "unevaluatedProperties")
        )
    }

    @Test
    fun `should validate patch without errors`() {
        val data = """
            {
                "productName": "patch"
            }
        """.trimIndent()

        assertDoesNotThrow {
            validatorService.validate(
                jsonSchema = mapper.readTree(schema),
                data = mapper.readTree(data),
                true
            )
        }
    }

    @Test
    fun `should validate patch with errors`() {
        val data = """
            {
                "productName": "test",
                "releaseDate": "invalid"
            }
        """.trimIndent()

        val exception = assertThrows<JsonSchemaValidationService.JsonSchemaValidationException> {
            validatorService.validate(
                jsonSchema = mapper.readTree(schema),
                data = mapper.readTree(data),
                true
            )
        }

        assertThat(exception.messages).hasSize(1)
        assertThat(exception.messages.map { Pair(it.property ?: it.instanceLocation.toString(), it.type) }).containsExactlyInAnyOrder(
            Pair("/releaseDate", "format")
        )
    }

    val schema = """
            {
              "${"$"}schema": "https://json-schema.org/draft/2020-12/schema",
              "${"$"}id": "https://example.com/product.schema.json",
              "title": "Product",
              "description": "A product from Acme's catalog",
              "type": "object",
              "properties": {
                "vendorName": {
                  "description": "Name of the vendor",
                  "type": "string"
                },
                "productName": {
                  "description": "Name of the product",
                  "type": "string"
                },
                "releaseDate": {
                  "description": "Release date of the product",
                  "type": "string",
                  "format": "date"
                }
              },
              "required": [ "vendorName", "productName", "releaseDate" ],
              "unevaluatedProperties": false
            }
        """.trimIndent()
}