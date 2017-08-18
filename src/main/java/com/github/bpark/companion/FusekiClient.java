package com.github.bpark.companion;

import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

/**
 * @author ksr
 */
public class FusekiClient {

    public static void main(String[] args) {
        String sparqlEndpoint = "http://192.168.56.8:3030/demo/query";
        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.initialize();

        /*
        String sparqlEndpoint = "http://192.168.56.8:3030/demo/update";

        PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        INSERT
        {
            <http://edf.org/resource/dev> foaf:name "dev"
        }
        WHERE {}
         */

        String query = "SELECT ?subject ?predicate ?object\n" +
                "WHERE {\n" +
                "  ?subject ?predicate ?object\n" +
                "}\n" +
                "LIMIT 25";

        System.out.println(query);

        try (RepositoryConnection conn = repo.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            tupleQuery.evaluate(new SPARQLResultsTSVWriter(System.out));
            /*
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {  // iterate over the result
                    BindingSet bindingSet = result.next();
                    bindingSet.getValue("subject").stringValue();
                }
            } */
        }

    }
}
