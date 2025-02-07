package com.ritense.objecttypenapi.client

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

class ObjecttypeVersionTest {

    @Test
    fun `should deserialize json type version`() {
        val json = """
            {
                "url": "http://localhost:8011/api/v1/objecttypes/3e852115-277a-4570-873a-9a64be3aeb34/versions/1",
                "version": 1,
                "objectType": "http://localhost:8011/api/v1/objecttypes/3e852115-277a-4570-873a-9a64be3aeb34",
                "status": "draft",
                "jsonSchema": {
                    "type": "object",
                    "title": "ObjectType Taak",

                    "properties": {
                        "someNumber": {
                            "type": "string",
                            "pattern": "^[0-9]+${'$'}"
                        }
                    }
                },
                "createdAt": "2022-02-03",
                "modifiedAt": "2022-02-04",
                "publishedAt": "2022-02-05"
            }
        """.trimIndent()
        val version =MapperSingleton.get().readValue<ObjecttypeVersion>(json)

        assertThat(version.url).isEqualTo(URI("http://localhost:8011/api/v1/objecttypes/3e852115-277a-4570-873a-9a64be3aeb34/versions/1"))
        assertThat(version.version).isEqualTo(1)
        assertThat(version.objectType).isEqualTo(URI("http://localhost:8011/api/v1/objecttypes/3e852115-277a-4570-873a-9a64be3aeb34"))
        assertThat(version.status).isEqualTo(ObjecttypeVersion.Status.DRAFT)
        assertThat(version.jsonSchema).isInstanceOf(ObjectNode::class.java)
        assertThat(version.createdAt).isEqualTo("2022-02-03")
        assertThat(version.modifiedAt).isEqualTo("2022-02-04")
        assertThat(version.publishedAt).isEqualTo("2022-02-05")
    }
}