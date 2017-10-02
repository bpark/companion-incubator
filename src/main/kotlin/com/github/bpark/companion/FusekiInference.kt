package com.github.bpark.companion

import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.HttpClients
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriter
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.eclipse.rdf4j.rio.RDFFormat
import java.io.File

/*
Available reasoners:

http://jena.hpl.hp.com/2003/RDFSExptRuleReasoner
http://jena.hpl.hp.com/2003/TransitiveReasoner
http://jena.hpl.hp.com/2003/GenericRuleReasoner
http://jena.hpl.hp.com/2003/DAMLMicroReasonerFactory
http://jena.hpl.hp.com/2003/OWLFBRuleReasoner
http://jena.hpl.hp.com/2003/OWLMicroFBRuleReasoner
http://jena.hpl.hp.com/2003/OWLMiniFBRuleReasoner
 */

object FusekiInference {

    fun createDataSet() {
        val assembler = File("data/inference-dataset.ttl").readText()

        println(assembler)

        val client = HttpClients.createDefault();
        val httpPost = HttpPost("http://192.168.56.8:3030/\$/datasets");

        val entity = StringEntity(assembler);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-type", "application/turtle");

        val creds = UsernamePasswordCredentials("admin", "jPji3a1FUTZCVse")
        httpPost.addHeader(BasicScheme().authenticate(creds, httpPost, null))

        val response = client.execute(httpPost);
        client.close();

        println(response.statusLine.statusCode)
        println(response.statusLine.reasonPhrase)
    }

    fun insertData() {
        val updateEndpoint = "http://192.168.56.8:3030/animalinfer/update"

        val repo = SPARQLRepository(updateEndpoint)
        repo.initialize()


        repo.connection.use { conn -> conn.add(File("data/human-relations.ttl"), null, RDFFormat.TURTLE) }

    }

    fun queryData() {
        val updateEndpoint = "http://192.168.56.8:3030/animalinfer/sparql"

        val repo = SPARQLRepository(updateEndpoint)
        repo.initialize()

        val query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX hr: <http://xmlns.com/hr/0.1/>\n" +
                "SELECT ?son WHERE {\n" +
                "  hr:Anakin hr:hasSon ?son\n" +
                "}\n" +
                "LIMIT 10"

        println(query)

        repo.connection.use { conn ->
            val tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query)
            tupleQuery.evaluate(SPARQLResultsTSVWriter(System.out))

        }
    }

}

fun main(args: Array<String>) {
    FusekiInference.createDataSet()
    FusekiInference.insertData()
    FusekiInference.queryData()
}
