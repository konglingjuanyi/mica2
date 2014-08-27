/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.web.model;

import java.util.Arrays;
import java.util.Locale;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.obiba.mica.config.JsonConfiguration;
import org.obiba.mica.dataset.domain.HarmonizationDataset;
import org.obiba.mica.dataset.domain.StudyDataset;
import org.obiba.mica.domain.Attribute;
import org.obiba.mica.domain.StudyTable;
import org.obiba.mica.micaConfig.MicaConfig;
import org.obiba.mica.micaConfig.MicaConfigService;
import org.obiba.mica.study.StudyService;
import org.obiba.mica.study.domain.StudyState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.mockito.Mockito.when;
import static org.obiba.mica.domain.LocalizedString.en;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
@ContextConfiguration(classes = { DatasetDtosTest.Config.class, JsonConfiguration.class })
@DirtiesContext
@SuppressWarnings({ "MagicNumber", "OverlyCoupledClass" })
public class DatasetDtosTest {

  @Inject
  private MicaConfigService micaConfigService;

  @Inject
  private StudyService studyService;

  @Inject
  private Dtos dtos;

  @Before
  public void before() {
    MicaConfig config = new MicaConfig();
    config.setLocales(Arrays.asList(Locale.ENGLISH, Locale.FRENCH));
    when(micaConfigService.getConfig()).thenReturn(config);
  }

  // TODO complete this class by using StudyDtosTest class

  @Test
  public void test_study_dataset_dto() throws Exception {
    StudyDataset studyDataset = createStudyDataset();
    Mica.DatasetDto dto = dtos.asDto(studyDataset);
    System.out.println(dto);
  }

  @Test
  public void test_harmonized_dataset_dto() throws Exception {
    HarmonizationDataset harmonizationDataset = createHarmonizedDataset();
    Mica.DatasetDto dto = dtos.asDto(harmonizationDataset);
    System.out.println(dto);
  }

  private StudyDataset createStudyDataset() {
    StudyDataset studyDataset = new StudyDataset();
    studyDataset.setName(en("FNAC").forFr("FNAC"));
    studyDataset.setEntityType("Participant");
    StudyTable table = new StudyTable();
    table.setStudyId("1111111111111111");
    table.setProject("study1");
    table.setTable("FNAC");
    studyDataset.setStudyTable(table);
    studyDataset.addAttribute(Attribute.Builder.newAttribute("att1").namespace("mica").value(Locale.FRENCH, "value fr").value(Locale.ENGLISH, "Value en").build());
    studyDataset.addAttribute(Attribute.Builder.newAttribute("att2").namespace("mica").value(Locale.FRENCH, "value fr").build());

    StudyState state = new StudyState();
    state.setId("1111111111111111");
    state.setName(en("S1").forFr("S1"));
    when(studyService.findStateById("1111111111111111")).thenReturn(state);

    return studyDataset;
  }

  private HarmonizationDataset createHarmonizedDataset() {
    HarmonizationDataset harmonizationDataset = new HarmonizationDataset();
    harmonizationDataset.setName(en("Healthy Obese Project").forFr("Projet des obeses en sante"));
    harmonizationDataset.setEntityType("Participant");
    harmonizationDataset.setProject("mica");
    harmonizationDataset.setTable("HOP");
    StudyTable table = new StudyTable();
    table.setStudyId("222222222222222");
    table.setProject("study1");
    table.setTable("HOP");
    harmonizationDataset.addStudyTable(table);

    StudyState state = new StudyState();
    state.setId("222222222222222");
    state.setName(en("S2").forFr("S2"));
    when(studyService.findStateById("222222222222222")).thenReturn(state);

    return harmonizationDataset;
  }

  @Configuration
  @ComponentScan("org.obiba.mica.web.model")
  static class Config {


    @Bean
    public MicaConfigService micaConfigService() {
      return Mockito.mock(MicaConfigService.class);
    }

    @Bean
    public StudyService studyService() {
      return Mockito.mock(StudyService.class);
    }

  }
}