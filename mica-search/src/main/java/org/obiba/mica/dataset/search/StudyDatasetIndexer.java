/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.dataset.search;

import java.util.List;

import javax.inject.Inject;

import org.obiba.mica.dataset.domain.DatasetVariable;
import org.obiba.mica.dataset.domain.StudyDataset;
import org.obiba.mica.dataset.event.DatasetDeletedEvent;
import org.obiba.mica.dataset.event.DatasetUpdatedEvent;
import org.obiba.mica.dataset.event.IndexDatasetsEvent;
import org.obiba.mica.service.StudyDatasetService;
import org.obiba.mica.study.domain.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

@Component
public class StudyDatasetIndexer extends DatasetIndexer<StudyDataset> {

  private static final Logger log = LoggerFactory.getLogger(StudyDatasetIndexer.class);

  @Inject
  private StudyDatasetService studyDatasetService;

  @Async
  @Subscribe
  public void datasetUpdated(DatasetUpdatedEvent event) {
    if(!event.isStudyDataset()) return;
    log.info("Dataset {} was updated", event.getPersistable());
    StudyDataset studyDataset = (StudyDataset) event.getPersistable();
    reIndex(studyDataset);
    reIndex(studyDataset, studyDataset.getStudyTable().getStudyId());
  }

  @Async
  @Subscribe
  public void datasetDeleted(DatasetDeletedEvent event) {
    if(!event.isStudyDataset()) return;
    log.info("Dataset {} was deleted", event.getPersistable());
    StudyDataset studyDataset = (StudyDataset) event.getPersistable();
    deleteFromDatasetIndices(studyDataset);
    deleteFromStudyIndices(studyDataset, studyDataset.getStudyTable().getStudyId());
  }

  @Async
  @Subscribe
  public void indexAll(IndexDatasetsEvent event) {
    reIndexAll();
  }

  @Override
  protected Iterable<DatasetVariable> getVariables(StudyDataset dataset) {
    return studyDatasetService.getDatasetVariables(dataset);
  }

  @Override
  protected Iterable<DatasetVariable> getVariables(StudyDataset dataset, Study study) {
    return getVariables(dataset);
  }

  @Override
  protected Iterable<StudyDataset> findAllDatasets() {
    List<StudyDataset> datasets = Lists.newArrayList();
    Iterables.addAll(datasets, studyDatasetService.findAllDatasets());
    return datasets;
  }

  protected Iterable<StudyDataset> findAllPublishedDatasets() {
    List<StudyDataset> datasets = Lists.newArrayList();
    Iterables.addAll(datasets, studyDatasetService.findAllPublishedDatasets());
    return datasets;
  }
}