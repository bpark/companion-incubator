package com.github.bpark.companion;

import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

/**
 * @author ksr
 */
public class FusekiWriter {

    public static void main(String[] args) {
        String sparqlEndpoint = "http://192.168.56.8:8080/demo/update";
        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.initialize();


        String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "INSERT\n" +
                "        {\n" +
                "            <http://localhost/me> foaf:name \"Donald Trump\"\n" +
                "        }\n" +
                "        WHERE {}";

        System.out.println(query);

        try (RepositoryConnection conn = repo.getConnection()) {

            Update update = conn.prepareUpdate(QueryLanguage.SPARQL, query);
            update.execute();
        }

    }
}
