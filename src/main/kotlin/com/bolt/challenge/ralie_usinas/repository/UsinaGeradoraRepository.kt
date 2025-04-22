package com.bolt.challenge.ralie_usinas.repository


import com.bolt.challenge.ralie_usinas.entity.UsinaGeradora
import org.springframework.data.jpa.repository.JpaRepository

interface UsinaGeradoraRepository : JpaRepository<UsinaGeradora, Int> {

}