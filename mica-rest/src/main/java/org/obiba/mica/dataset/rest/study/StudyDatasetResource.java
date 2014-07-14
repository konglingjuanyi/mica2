/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.dataset.rest.study;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.obiba.mica.dataset.domain.StudyDataset;
import org.obiba.mica.service.StudyDatasetService;
import org.obiba.mica.web.model.Dtos;
import org.obiba.mica.web.model.Mica;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Search;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

@Component
@Scope("request")
@RequiresAuthentication
public class StudyDatasetResource {

  @Inject
  private ApplicationContext applicationContext;

  @Inject
  private StudyDatasetService datasetService;

  @Inject
  private Dtos dtos;

  private String id;

  public void setId(String id) {
    this.id = id;
  }

  @GET
  public Mica.DatasetDto get() {
    return dtos.asDto(getDataset());
  }

  @PUT
  public Response index() {
    datasetService.index(id);
    return Response.noContent().build();
  }

  @GET
  @Path("/table")
  public Magma.TableDto getTable() {
    Magma.TableDto dto = datasetService.getTableDto(getDataset());
    return dto;
  }

  @GET
  @Path("/variables")
  public List<Mica.DatasetVariableDto> getVariables() {
    ImmutableList.Builder<Mica.DatasetVariableDto> builder = ImmutableList.builder();
    datasetService.getDatasetVariables(getDataset()).forEach(variable -> builder.add(dtos.asDto(variable)));
    return builder.build();
  }

  @Path("/variable/{variable}")
  public StudyDatasetVariableResource getVariable(@PathParam("variable") String variable) {
    StudyDatasetVariableResource resource = applicationContext.getBean(StudyDatasetVariableResource.class);
    resource.setDatasetId(id);
    resource.setName(variable);
    return resource;
  }

  @POST
  @Path("/facets")
  public Search.QueryResultDto getFacets(Search.QueryTermsDto query) {
    return datasetService.getFacets(getDataset(), query);
  }

  private StudyDataset getDataset() {
    return datasetService.findById(id);
  }

}