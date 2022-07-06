package com.ritense.plugin.web.rest

import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.ProcessLink
import com.ritense.plugin.service.PluginService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/process-link"])

class PluginProcessLinkResource(
    private var pluginService: PluginService
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProcessLinks(
        @RequestParam("processDefinitionId") processDefinitionId: String,
        @RequestParam("activityId") activityId: String
    ): ResponseEntity<List<ProcessLink>> {
        val links: List<ProcessLink> = pluginService.getProcessLinks(processDefinitionId, activityId)

        return ResponseEntity.ok(links)
    }

    @PostMapping(value = ["/plugin"])
    fun createProcessLink(
        @RequestBody processLink: PluginProcessLink
    ): ResponseEntity<Unit> {
        pluginService.createProcessLink(processLink)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PostMapping(value = ["/plugin"])
    fun updateProcessLink(
        @RequestBody processLink: PluginProcessLink
    ): ResponseEntity<Unit> {
        pluginService.updateProcessLink(processLink)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}