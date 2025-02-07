package com.ritense.objectmanagement.service

import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JsonSchemaValidationServiceTest {

    private val validatorService = JsonSchemaValidationService()
    private val mapper = MapperSingleton.get()

    @Test
    fun `should validate with errors`() {
        val schema = """
            {
              "${"$"}schema": "https://json-schema.org/draft/2020-12/schema",
              "${"$"}id": "https://example.com/product.schema.json",
              "title": "Product",
              "description": "A product from Acme's catalog",
              "type": "object",
              "properties": {
                "productName": {
                  "description": "Name of the product",
                  "type": "string"
                },
                "releaseDate": {
                  "description": "Release date of the product",
                  "type": "string",
                  "format": "date"
                }
              }
            }
        """.trimIndent()

        val data = """
            {
                "productName": null,
                "releaseDate": "invalid"
            }
        """.trimIndent()

        val exception = assertThrows<JsonSchemaValidationService.JsonSchemaValidationException> {
            validatorService.validate(
                schema = mapper.readTree(schema),
                data = mapper.readTree(data)
            )
        }

        assertThat(exception.messages).hasSize(2)
    }
}