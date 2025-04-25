package com.bolt.challenge.ralie_usinas.service

import com.bolt.challenge.ralie_usinas.entity.UsinaGeradora
import com.bolt.challenge.ralie_usinas.repository.UsinaGeradoraRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service


@Service
class UsinaGeradoraService(private val usinaGeradoraRepository: UsinaGeradoraRepository) {


    @Transactional
    fun getTop5Usinas(): List<UsinaGeradora> {
        return usinaGeradoraRepository.findAll()
            .sortedByDescending { it.mdaPotenciaOutorgadaKw }
            .take(5)
    }


}