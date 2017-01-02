/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.dataset.domain;

import org.joda.time.DateTime;

/**
 * A {@link DatasetVariable} wrapper to represent the .
 */
public class PublishedDatasetVariable {

  private String id;

  private DateTime publicationDate;

  private DatasetVariable variable;

  public PublishedDatasetVariable() {}

  public PublishedDatasetVariable(DatasetVariable datasetVariable) {
    this.publicationDate = DateTime.now();
    this.variable = datasetVariable;
    this.id = datasetVariable.getId();
  }

  public String getId() {
    return id;
  }

  public DateTime getPublicationDate() {
    return publicationDate;
  }

  public DatasetVariable getVariable() {
    return variable;
  }
}
