package com.github.bpark.companion

import org.eclipse.rdf4j.common.lang.FileFormat
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.query.resultio.QueryResultIO
import org.eclipse.rdf4j.query.resultio.TupleQueryResultParserRegistry
import org.eclipse.rdf4j.query.resultio.helpers.QueryResultCollector
import java.io.FileInputStream




fun main(args: Array<String>) {

    val inputStream = FileInputStream("./data/result.tsv")

    val keys = TupleQueryResultParserRegistry.getInstance().keys
    // text/tab-separated-values;
    val result = FileFormat.matchFileName("result.tsv", keys).get()

    val parser = QueryResultIO.createTupleParser(result, SimpleValueFactory.getInstance())
    //parser.setQueryResultHandler(SPARQLResultsTSVWriter(System.out))
    val collector = QueryResultCollector()
    parser.setQueryResultHandler(collector)
    parser.parseQueryResult(inputStream)

    collector.bindingSets.forEach {
        val foreCast = it.getValue("foreCast").stringValue()
        val temperature = it.getValue("temperature").stringValue()
        val description = it.getValue("description").stringValue()

        println("$foreCast $temperature $description")
    }

}
