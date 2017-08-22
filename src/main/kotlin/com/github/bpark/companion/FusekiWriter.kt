package com.github.bpark.companion

import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository

fun main(args: Array<String>) {
    val sparqlEndpoint = "http://192.168.56.8:8080/demo/update"
    val repo = SPARQLRepository(sparqlEndpoint)
    repo.initialize()


    val query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
            "INSERT\n" +
            "        {\n" +
            "            <http://localhost/me> foaf:name \"Donald Trump\"\n" +
            "        }\n" +
            "        WHERE {}"

    println(query)

    repo.connection.use { conn ->

        val update = conn.prepareUpdate(QueryLanguage.SPARQL, query)
        update.execute()
    }

}