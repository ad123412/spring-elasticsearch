package com.ada.elastic.controller;

import com.ada.elastic.model.es.BankAccount;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

@RestController
@RequestMapping(value = {"/bank"})
public class BankAccountController {
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    public BankAccountController(ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    /**
     * example to show the query with and condition
     **/
    @GetMapping("/searchByState")
    public List<BankAccount> searchAccountsByStateAndCity(@RequestParam("state") String state,
                                                          @RequestParam(value = "city", required = false) String city) {
        BoolQueryBuilder stateQueryBuilder = boolQuery().must(matchQuery("state", state));
        QueryBuilder cityQueryBuilder =
                Objects.isNull(city) ?
                        stateQueryBuilder :
                        stateQueryBuilder.must(matchQuery("city", city));
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(cityQueryBuilder).build();
        SearchHits<BankAccount> searchResult =
                elasticsearchRestTemplate.search(query, BankAccount.class);
        List<BankAccount> searchedAccounts =
                searchResult.getSearchHits().stream()
                        .map(bankAccountSearchHit -> bankAccountSearchHit.getContent())
                        .collect(Collectors.toList());
        return searchedAccounts;
    }

    /**
     * example to show the query with or condition
     **/
    @GetMapping("/searchByBalance")
    public List<BankAccount> searchAccountsByBalanceForRange(@RequestParam("lte") int lte,
                                                             @RequestParam("gte") int gte) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery()
                        .should(rangeQuery("balance").lte(lte))
                        .should(rangeQuery("balance").gte(gte)))
                .build();
        SearchHits<BankAccount> searchedAccounts = elasticsearchRestTemplate.search(query, BankAccount.class);
        List<BankAccount> bankAccounts = searchedAccounts.stream()
                .map(bankAccountSearchHit -> bankAccountSearchHit.getContent())
                .collect(Collectors.toList());
        return bankAccounts;
    }

}
