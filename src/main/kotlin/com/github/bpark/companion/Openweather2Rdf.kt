package com.github.bpark.companion

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
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
import java.io.StringWriter


@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherForecast(val city: City, val list: List<Forecast>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class City(val id: Int, val name: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Forecast(val dt: Int, val main: Main, val weather: List<Weather>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Main(val temp: Float)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Weather(val main: String, val description: String)

object WeatherInference {

    fun createDataSet() {
        val assembler = File("data/weather-inference.ttl").readText()

        println(assembler)

        val client = HttpClients.createDefault();
        val httpPost = HttpPost("http://192.168.56.8:3030/\$/datasets");

        val entity = StringEntity(assembler);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-type", "application/turtle");

        val creds = UsernamePasswordCredentials("admin", "1afbda62-8ec9-47a3-a96f-40b58b13c150")
        httpPost.addHeader(BasicScheme().authenticate(creds, httpPost, null))

        val response = client.execute(httpPost);
        client.close();

        println(response.statusLine.statusCode)
        println(response.statusLine.reasonPhrase)
    }

    fun uploadModel() {
        val updateEndpoint = "http://192.168.56.8:3030/weather/update"

        val repo = SPARQLRepository(updateEndpoint)
        repo.initialize()


        repo.connection.use { conn -> conn.add(File("data/weather.ttl"), null, RDFFormat.TURTLE) }

    }

    fun mapToQuery(weatherForecast: WeatherForecast): String {
        val cfg = Configuration(Configuration.VERSION_2_3_23)
        cfg.setDirectoryForTemplateLoading(File("./data"))
        cfg.defaultEncoding = "UTF-8"
        cfg.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        cfg.logTemplateExceptions = false

        val template = cfg.getTemplate("weather-insert-sparql.ftl")

        val parameters = mapOf("weatherForecast" to weatherForecast)

        val out = StringWriter()

        /* Merge data-model with template */
        template.process(parameters, out)

        return out.toString()
    }

    fun insertData(query: String) {


        val sparqlEndpoint = "http://192.168.56.8:3030/weather/update"
        val repo = SPARQLRepository(sparqlEndpoint)
        repo.initialize()

        println(query)

        repo.connection.use { conn ->

            val update = conn.prepareUpdate(QueryLanguage.SPARQL, query)
            update.execute()
        }
    }

    fun queryData() {
        val updateEndpoint = "http://192.168.56.8:3030/weather/sparql"

        val repo = SPARQLRepository(updateEndpoint)
        repo.initialize()

        val query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX weather: <http://xmlns.com/weather/0.1/>\n" +
                "PREFIX hr: <http://iserve.kmi.open.ac.uk/ns/hrests#>\n" +
                "SELECT ?foreCast ?temperature ?description WHERE {\n" +
                "  ?city weather:cityName \"Altstadt\" .\n" +
                "  ?city weather:forecast ?foreCast .\n" +
                "  ?foreCast weather:temperature ?temperature .\n" +
                "  ?foreCast weather:description ?description\n" +
                "}\n" +
                "ORDER BY ASC(?foreCast)\n" +
                "LIMIT 100"

        println(query)

        repo.connection.use { conn ->
            val tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query)
            tupleQuery.evaluate(SPARQLResultsTSVWriter(System.out))

        }
    }

}

fun main(args: Array<String>) {
    val objectMapper = ObjectMapper()
    objectMapper.registerModule(KotlinModule())
    val weather = objectMapper.readValue(File("./data/weather-sample.json"), WeatherForecast::class.java)

    println(weather)

    val query = WeatherInference.mapToQuery(weather)
    println(query)

    //WeatherInference.createDataSet()
    //WeatherInference.uploadModel()
    //WeatherInference.insertData(query)
    WeatherInference.queryData()
}