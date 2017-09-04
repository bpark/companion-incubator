package com.github.bpark.companion

import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import java.io.FileInputStream
import java.io.FileOutputStream


fun main(args: Array<String>) {
    val rdfParser = Rio.createParser(RDFFormat.TURTLE)

    val inStream = FileInputStream("data/family.rdf")

    // Rio also accepts a java.io.Reader as input for the parser.
    val model = Rio.parse(inStream, "", RDFFormat.RDFXML)

    Rio.write(model, FileOutputStream("data/family.ttl"), RDFFormat.TURTLE);
}