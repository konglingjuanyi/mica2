/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.dataset;

import org.obiba.mica.dataset.domain.PublishedDatasetVariable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the {@link PublishedDatasetVariable} entity.
 */
public interface PublishedDatasetVariableRepository extends MongoRepository<PublishedDatasetVariable, String> {

  void deleteByVariableDatasetId(String id);

}
