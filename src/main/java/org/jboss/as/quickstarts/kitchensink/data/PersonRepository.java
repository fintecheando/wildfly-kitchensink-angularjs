/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.kitchensink.data;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;

import org.jboss.as.quickstarts.kitchensink.model.Person;

@ApplicationScoped
public class PersonRepository {

    @Inject
    private EntityManager em;

    public Person findById(String id) {
        return em.find(Person.class, id);
    }

    public Person findByEmail(String email) {        
        
        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        
        // create native Lucene query unsing the query DSL
        // alternatively you can write the Lucene query using the Lucene query parser
        // or the Lucene programmatic API. The Hibernate Search DSL is recommended though
        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Person.class).get();
        
        org.apache.lucene.search.Query luceneQuery = qb.keyword().onField("email").matching(email).createQuery();
        
        // wrap Lucene query in a javax.persistence.Query
        javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, Person.class);

        // execute search
        Person person = (Person)jpaQuery.getSingleResult();
        
        return person;
    }

    public List<Person> findAllOrderedByName() {
        return em.createQuery("from Person", Person.class ).getResultList();       
    }
}
