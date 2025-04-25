package com.bolt.challenge.ralie_usinas.repository


import com.bolt.challenge.ralie_usinas.entity.UsinaGeradora
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UsinaGeradoraRepository : JpaRepository<UsinaGeradora, Int> {

//    @Query("""
//        SELECT nom_empreendimento, SUM(mda_potencia_outorgada_kw) as total
//        FROM tb_usina_geradora
//        GROUP BY nom_empreendimento
//        ORDER BY total DESC
//        LIMIT 5
//    """, nativeQuery = true)
//    fun findTop5UsinasByPotencia(): List<Array<Any>>

}