package com.bolt.challenge.ralie_usinas.controller

import com.bolt.challenge.ralie_usinas.entity.UsinaGeradora
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.client.RestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod

@Controller
class PaginasController {

    @GetMapping("/usinas/view")
    fun exibirTop5Usinas(model: Model): String {
        val restTemplate = RestTemplate()
        val url = "http://localhost:8080/usinas/top5"
        val responseType = object : ParameterizedTypeReference<List<UsinaGeradora>>() {}

        val usinas = restTemplate.exchange(url, HttpMethod.GET, null, responseType).body
        model.addAttribute("usinas", usinas)

        return "usinas-view"
    }
}