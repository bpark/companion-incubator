package com.github.bpark.companion;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.IOException;

public class FusekiUpload {

    public static void main(String[] args) {
        String sparqlEndpoint = "http://192.168.56.8:8080/demo/update";
        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.initialize();


        try (RepositoryConnection conn = repo.getConnection()) {
            conn.add(new File("data/skos-demo.ttl"), null, RDFFormat.TURTLE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
