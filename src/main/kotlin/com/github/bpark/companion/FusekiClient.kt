package com.github.bpark.companion

import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriter
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository

fun main(args: Array<String>) {

    val sparqlEndpoint = "http://192.168.56.8:8080/demo/query"
    val repo = SPARQLRepository(sparqlEndpoint)
    repo.initialize()

    val query = "SELECT ?subject ?predicate ?object\n" +
            "WHERE {\n" +
            "  ?subject ?predicate ?object\n" +
            "}\n" +
            "LIMIT 25"

    println(query)

    repo.connection.use { conn ->
        val tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query)
        tupleQuery.evaluate(SPARQLResultsTSVWriter(System.out))

    }
}