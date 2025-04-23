package com.bolt.challenge.ralie_usinas.controller


import com.bolt.challenge.ralie_usinas.entity.UsinaGeradora
import com.bolt.challenge.ralie_usinas.repository.UsinaGeradoraRepository
import com.bolt.challenge.ralie_usinas.service.UsinaGeradoraService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/usinas")
class UsinaGeradoraController (private val service: UsinaGeradoraService) {

    @GetMapping("/top5")
    fun top5Geradores(): List<UsinaGeradora> =
        service.getTop5Usinas()
}