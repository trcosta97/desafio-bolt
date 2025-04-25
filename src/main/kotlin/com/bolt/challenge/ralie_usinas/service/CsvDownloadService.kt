package com.bolt.challenge.ralie_usinas.service

import com.bolt.challenge.ralie_usinas.entity.UsinaGeradora
import com.bolt.challenge.ralie_usinas.repository.UsinaGeradoraRepository
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.net.URL

@Service
class CsvDownloadService(private val usinaGeradoraRepository: UsinaGeradoraRepository) {

    private val csvUrl =
        "https://dadosabertos.aneel.gov.br/dataset/57e4b8b5-a5db-40e6-9901-27ca629d0477/resource/4a615df8-4c25-48fa-bbea-873a36a79518/download/ralie-usina.csv"
    private val csvFileName = "ralie-usina.csv"
    private val logger = LoggerFactory.getLogger(CsvDownloadService::class.java)

    // método que baixa o csv e persiste todas as infos no banco todo dia 00h
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
                InputStreamReader(FileInputStream(file), Charsets.UTF_8).use { fileReader ->
                    val csvReader = CSVReaderBuilder(fileReader)
                        .withCSVParser(CSVParserBuilder().withSeparator(';').build())
                        .withSkipLines(1)
                        .build()

                    val headers = CSVReaderBuilder(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                        .withCSVParser(CSVParserBuilder().withSeparator(';').build())
                        .build()
                        .readNext()

                    val potenciaColIndex = headers.indexOfFirst {
                        it.contains("MdaPotenciaOutorgadaKw", ignoreCase = true)
                    }

                    logger.info("Índice da coluna de potência: $potenciaColIndex")

                    if (potenciaColIndex == -1) {
                        logger.error("Coluna de potência não encontrada no CSV!")
                        return
                    }

                    val records = csvReader.readAll()

                    val usinas = records.mapNotNull { record ->
                        try {
                            val potenciaStr = record.getOrNull(potenciaColIndex)?.trim()
                            logger.info("Potência original: '$potenciaStr'")

                            val potencia = if (!potenciaStr.isNullOrBlank()) {
                                try {
                                    val cleanValue = potenciaStr
                                        .replace(",", ".")
                                        .replace("[^0-9.]".toRegex(), "")
                                    logger.info("Valor limpo: '$cleanValue'")
                                    if (cleanValue.isNotEmpty()) BigDecimal(cleanValue) else BigDecimal.ZERO
                                } catch (e: Exception) {
                                    logger.error("Erro ao converter '$potenciaStr': ${e.message}")
                                    BigDecimal.ZERO
                                }
                            } else {
                                BigDecimal.ZERO
                            }

                            logger.info("Potência convertida final: $potencia")

                            UsinaGeradora(
                                ideNucleoCEG = 0,
                                codCEG = record[3],
                                sigUFPrincipal = record[4],
                                dscOrigemCombustivel = record.getOrNull(9) ?: "",
                                sigTipoGeracao = record[10],
                                nomEmpreendimento = record[11],
                                mdaPotenciaOutorgadaKw = potencia,
                                nomComplexo = record.getOrNull(12)
                            )
                        } catch (e: Exception) {
                            logger.error("Erro ao processar linha do CSV: ${e.message}")
                            e.printStackTrace()
                            null
                        }
                    }

                    if (usinas.isNotEmpty()) {
                        usinas.take(5).forEach {
                            logger.info("Amostra: ${it.nomEmpreendimento} - Potência: ${it.mdaPotenciaOutorgadaKw}")
                        }

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
            e.printStackTrace()
        }
    }


    //método que persiste o primeiro registro do csv para ser usado como teste
    private fun persistFirstRecord() {
    val file = File(csvFileName)
    if (!file.exists()) {
        logger.warn("Arquivo CSV não encontrado ao tentar persistir o primeiro registro.")
        return
    }

    try {
        InputStreamReader(FileInputStream(file), Charsets.UTF_8).use { reader ->
            val csvReader = CSVReaderBuilder(reader)
                .withCSVParser(CSVParserBuilder().withSeparator(';').build())
                .withSkipLines(1)
                .build()

            val headers = CSVReaderBuilder(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                .withCSVParser(CSVParserBuilder().withSeparator(';').build())
                .build()
                .readNext()

            val potenciaColIndex = headers.indexOfFirst {
                it.contains("MdaPotenciaOutorgadaKw", ignoreCase = true)
            }

            val record = csvReader.readNext()
            if (record != null) {
                val potenciaStr = record.getOrNull(potenciaColIndex)?.trim()
                val potencia = if (!potenciaStr.isNullOrBlank()) {
                    try {
                        val cleanValue = potenciaStr
                            .replace(",", ".")
                            .replace("[^0-9.]".toRegex(), "")
                        if (cleanValue.isNotEmpty()) BigDecimal(cleanValue) else BigDecimal.ZERO
                    } catch (e: Exception) {
                        logger.error("Erro ao converter '$potenciaStr': ${e.message}")
                        BigDecimal.ZERO
                    }
                } else {
                    BigDecimal.ZERO
                }

                val usina = UsinaGeradora(
                    ideNucleoCEG = 0,
                    codCEG = record[3],
                    sigUFPrincipal = record[4],
                    dscOrigemCombustivel = record.getOrNull(9) ?: "",
                    sigTipoGeracao = record[10],
                    nomEmpreendimento = record[11],
                    mdaPotenciaOutorgadaKw = potencia,
                    nomComplexo = record.getOrNull(12)
                )

                usinaGeradoraRepository.save(usina)
                logger.info("Primeira usina persistida: ${usina.nomEmpreendimento} - Potência: ${usina.mdaPotenciaOutorgadaKw}")
            } else {
                logger.warn("Nenhum registro encontrado no CSV.")
            }
        }
    } catch (e: Exception) {
        logger.error("Erro ao persistir o primeiro registro do CSV: ${e.message}")
        e.printStackTrace()
    }
}

    fun getColumnNamesWithIndices(): Map<String, Int> {
        val file = File(csvFileName)
        return if (file.exists()) {
            InputStreamReader(FileInputStream(file), Charsets.UTF_8).use { fileReader ->
                val csvReader = CSVReaderBuilder(fileReader)
                    .withCSVParser(CSVParserBuilder().withSeparator(';').build())
                    .build()

                val headers = csvReader.readNext() ?: emptyArray()
                headers.mapIndexed { index, name -> name to index }.toMap()
            }
        } else {
            logger.warn("Arquivo CSV não encontrado ao tentar ler os cabeçalhos.")
            emptyMap()
        }
    }

    // método que executa assim que o programa é inicializado para ser usado como teste
//    @PostConstruct
//    fun init() {
//        downloadAndPersistCsv()
//        persistFirstRecord()
//    }

}
