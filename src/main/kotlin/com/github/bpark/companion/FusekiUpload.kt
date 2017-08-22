package com.github.bpark.companion

import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.eclipse.rdf4j.rio.RDFFormat
import java.io.File
import java.io.IOException

fun main(args: Array<String>) {
    val sparqlEndpoint = "http://192.168.56.8:8080/demo/update"
    val repo = SPARQLRepository(sparqlEndpoint)
    repo.initialize()


    try {
        repo.connection.use { conn -> conn.add(File("data/skos-demo.ttl"), null, RDFFormat.TURTLE) }
    } catch (e: IOException) {
        throw RuntimeException(e)
    }

}