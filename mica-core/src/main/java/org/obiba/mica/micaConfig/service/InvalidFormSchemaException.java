/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.micaConfig.service;

public class InvalidFormSchemaException extends RuntimeException {

  private static final long serialVersionUID = -8675643670421788807L;

  public InvalidFormSchemaException() {
    super("Invalid schema form exception.");
  }

}
