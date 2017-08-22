package com.github.bpark.companion

import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository

fun main(args: Array<String>) {

    val prefixes = arrayOf("PREFIX owl: <http://www.w3.org/2002/07/owl#>",
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>",
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>",
            "PREFIX : <http://dbpedia.org/resource/>",
            "PREFIX dbpedia2: <http://dbpedia.org/property/>",
            "PREFIX dbpedia: <http://dbpedia.org/>",
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
            "PREFIX ont: <http://dbpedia.org/ontology/>")

    val sparqlEndpoint = "http://dbpedia.org/sparql"
    val repo = SPARQLRepository(sparqlEndpoint)
    repo.initialize()

    val prefix = prefixes.joinToString("\n")

    val query = prefix + "\nSELECT ?comment WHERE {\n" +
            "?body a ont:CelestialBody .\n" +
            "?body foaf:name \"Vega\"@en .\n" +
            "?body rdfs:comment ?comment .\n" +
            "FILTER ( lang(?comment) = \"en\")\n" +
            "}"

    println(query)

    repo.connection.use { conn ->
        val tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query)
        tupleQuery.evaluate().use { result ->
            while (result.hasNext()) {  // iterate over the result
                val bindingSet = result.next()
                val comment = bindingSet.getValue("comment")
                println(comment.stringValue())
            }
        }
    }
}