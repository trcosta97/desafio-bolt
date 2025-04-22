package com.bolt.challenge.ralie_usinas.controller


import com.bolt.challenge.ralie_usinas.entity.UsinaGeradora
import com.bolt.challenge.ralie_usinas.repository.UsinaGeradoraRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/usinas")
class UsinaGeradoraController (private val repository: UsinaGeradoraRepository) {

    @GetMapping("/top5")
    fun top5Geradores(): List<UsinaGeradora> =
        repository.findAll()
            .sortedByDescending { it.mdaPotenciaOutorgadaKw }
            .take(5)
}