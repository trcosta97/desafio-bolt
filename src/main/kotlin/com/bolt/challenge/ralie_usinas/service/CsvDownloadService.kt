package com.bolt.challenge.ralie_usinas.service

import com.bolt.challenge.ralie_usinas.entity.UsinaGeradora
import com.bolt.challenge.ralie_usinas.repository.UsinaGeradoraRepository
import com.opencsv.CSVReaderBuilder
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileReader
import java.math.BigDecimal
import java.net.URL

@Service
class CsvDownloadService(private val usinaGeradoraRepository: UsinaGeradoraRepository) {

    private val csvUrl = "https://dadosabertos.aneel.gov.br/dataset/ralie-relatorio-de-acompanhamento-da-expansao-da-oferta-de-geracao-de-energia-eletrica/resource/a3c58ecb-e936-4dc1-884b-9941f7079a73/download/ralie-usina.csv"
    private val csvFileName = "ralie-usina.csv"
    private val logger = LoggerFactory.getLogger(CsvDownloadService::class.java)

    @Scheduled(cron = "0 0 0 * * *")
    fun downloadAndPersistCsv() {
        downloadCsv()
        persistCsvData()
    }

    private fun downloadCsv() {
        try {
            val file = File(csvFileName)
            URL(csvUrl).openStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            logger.info("Arquivo CSV baixado com sucesso.")
        } catch (e: Exception) {
            logger.error("Erro ao baixar o arquivo CSV: ${e.message}")
        }
    }

    private fun persistCsvData() {
        try {
            val file = File(csvFileName)
            if (file.exists()) {
                FileReader(file).use { fileReader ->
                    val csvReader = CSVReaderBuilder(fileReader).withSkipLines(1).build() // Pula a primeira linha (cabeçalho)
                    val records = csvReader.readAll()

                    val usinas = records.mapNotNull { record ->
                        try {
                            UsinaGeradora(
                                ideNucleoCEG = 0, // O ID será gerado automaticamente pelo banco
                                codCEG = record[0],
                                sigUFPrincipal = record[1],
                                dscOrigemCombustivel = record.getOrNull(9), // Usando getOrNull para evitar IndexOutOfBounds
                                sigTipoGeracao = record[10],
                                nomEmpreendimento = record[11],
                                mdaPotenciaOutorgadaKw = BigDecimal(record[15].replace(",", ".")), // Atenção à conversão de vírgula para ponto
                                nomComplexo = record.getOrNull(12)
                            )
                        } catch (e: Exception) {
                            logger.error("Erro ao processar a linha do CSV: ${record.joinToString()}", e)
                            null
                        }
                    }

                    if (usinas.isNotEmpty()) {
                        usinaGeradoraRepository.saveAll(usinas)
                        logger.info("${usinas.size} usinas foram persistidas no banco de dados.")
                    } else {
                        logger.info("Nenhuma usina encontrada no arquivo CSV para persistir.")
                    }
                }
            } else {
                logger.warn("Arquivo CSV não encontrado.")
            }
        } catch (e: Exception) {
            logger.error("Erro ao ler e persistir os dados do CSV: ${e.message}")
        }
    }
}