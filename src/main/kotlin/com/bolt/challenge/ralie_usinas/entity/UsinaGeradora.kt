package com.bolt.challenge.ralie_usinas.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "tb_usina_geradora")
data class UsinaGeradora(
    @Id
    @Column(name = "id_nucleo_ceg")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val ideNucleoCEG: Int,

    @Column(name = "cod_ceg", length = 300)
    val codCEG: String,

    @Column(name = "sig_uf_principal")
    val sigUFPrincipal: String,

    @Column(name = "dsc_origem_combustivel", length = 500)
    val dscOrigemCombustivel: String? = null,

    @Column(name = "sig_tipo_geracao", length = 50)
    val sigTipoGeracao: String,

    @Column(name = "nom_empreendimento", length = 1000)
    val nomEmpreendimento: String,

    @Column(name = "mda_potencia_outorgada_kw", precision = 20, scale = 2)
    val mdaPotenciaOutorgadaKw: BigDecimal,

    @Column(name = "nom_complexo", length = 1000)
    val nomComplexo: String? = null





)