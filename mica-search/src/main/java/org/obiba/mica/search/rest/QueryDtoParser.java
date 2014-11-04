/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.search.rest;

import java.util.stream.Stream;

import org.elasticsearch.index.query.BaseQueryBuilder;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.obiba.mica.web.model.MicaSearch;

public class QueryDtoParser {

  private QueryDtoParser() {}

  public static QueryDtoParser newParser() {
    return new QueryDtoParser();
  }

  public BaseQueryBuilder parse(MicaSearch.QueryDto queryDto) {
    BaseQueryBuilder query = null;

    if(queryDto.hasQueryString()) {
      QueryStringQueryBuilder queryStringBuilder = QueryBuilders.queryString(queryDto.getQueryString().getQuery());
      queryDto.getQueryString().getFieldsList().forEach(queryStringBuilder::field);
      query = queryStringBuilder;
    } else {
      query = QueryBuilders.matchAllQuery();
    }

    return queryDto.hasFilteredQuery() ? parseFilterQuery(query, queryDto.getFilteredQuery()) : query;
  }

  public SortBuilder parseSort(MicaSearch.QueryDto queryDto) {

    if(queryDto.hasSort()) {
      MicaSearch.QueryDto.SortDto sortDto = queryDto.getSort();
      return SortBuilders.fieldSort(sortDto.getField()).order(SortOrder.valueOf(sortDto.getOrder().name()));
    }

    return null;
  }

  private BaseQueryBuilder parseFilterQuery(BaseQueryBuilder query, MicaSearch.FilteredQueryDto filteredQueryDto) {
    return QueryBuilders.filteredQuery(query, parseFilteredQuery(filteredQueryDto));
  }

  private FilterBuilder parseFilteredQuery(MicaSearch.FilteredQueryDto filteredQueryDto) {
    if(filteredQueryDto.hasExtension(MicaSearch.BoolFilterQueryDto.filter)) {
      MicaSearch.BoolFilterQueryDto boolFilterDto = filteredQueryDto.getExtension(MicaSearch.BoolFilterQueryDto.filter);
      return parseFilter(boolFilterDto);
    }

    if(filteredQueryDto.hasExtension(MicaSearch.FieldFilterQueryDto.filter)) {
      MicaSearch.FieldFilterQueryDto fieldFilterDto = filteredQueryDto
        .getExtension(MicaSearch.FieldFilterQueryDto.filter);
      return parseFilter(fieldFilterDto);
    }

    return null;
    //throw new IllegalArgumentException("Invalid filtered query extension");
  }

  private FilterBuilder parseFilter(MicaSearch.BoolFilterQueryDto boolFilterDto) {
    BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();

    Stream<FilterBuilder> filterBuilders = boolFilterDto.getFilteredQueryList().stream().map(this::parseFilteredQuery);

    switch(boolFilterDto.getOp()) {
      case MUST:
        filterBuilders.forEach(boolFilter::must);
        break;
      case MUST_NOT:
        filterBuilders.forEach(boolFilter::mustNot);
        break;
      case SHOULD:
        filterBuilders.forEach(boolFilter::should);
        break;
    }

    return boolFilter;
  }

  private FilterBuilder parseFilter(MicaSearch.FieldFilterQueryDto filter) {
    String field = filter.getField();

    if(filter.hasExtension(MicaSearch.TermsFilterQueryDto.terms)) {
      return FilterBuilders
        .termsFilter(field, filter.getExtension(MicaSearch.TermsFilterQueryDto.terms).getValuesList());
    }

    if(filter.hasExtension(MicaSearch.RangeFilterQueryDto.range)) {
      return parseRangeFilter(field, filter.getExtension(MicaSearch.RangeFilterQueryDto.range));
    }

    throw new IllegalArgumentException("Invalid field filter extension");
  }

  private FilterBuilder parseRangeFilter(String field, MicaSearch.RangeFilterQueryDto rangeFilterDto) {
    RangeFilterBuilder filterBuilder = FilterBuilders.rangeFilter(field);
    if(rangeFilterDto.hasFrom()) parseRangeFilterCondition(filterBuilder, rangeFilterDto.getFrom());
    if(rangeFilterDto.hasTo()) parseRangeFilterCondition(filterBuilder, rangeFilterDto.getTo());
    return filterBuilder;
  }

  private void parseRangeFilterCondition(RangeFilterBuilder filterBuilder, MicaSearch.RangeConditionDto condition) {
    String value = condition.getValue();
    switch(condition.getOp()) {
      case LT:
        filterBuilder.lt(value);
        break;
      case LTE:
        filterBuilder.lte(value);
        break;
      case GT:
        filterBuilder.gt(value);
        break;
      case GTE:
        filterBuilder.gte(value);
        break;
    }
  }

}
