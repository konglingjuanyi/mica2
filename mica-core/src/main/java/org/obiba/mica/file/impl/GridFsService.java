package org.obiba.mica.file.impl;

import java.io.InputStream;

import javax.inject.Inject;

import org.obiba.mica.file.FileRuntimeException;
import org.obiba.mica.file.FileService;
import org.obiba.mica.file.service.TempFileService;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Component;

import com.mongodb.gridfs.GridFSDBFile;

@Component
public class GridFsService implements FileService {

  @Inject
  private GridFsOperations gridFsOperations;

  @Inject
  private TempFileService tempFileService;

  @Override
  public InputStream getFile(String id) throws FileRuntimeException {
    GridFSDBFile f = gridFsOperations.findOne(new Query().addCriteria(Criteria.where("filename").is(id)));

    if(f == null)
      throw new FileRuntimeException(id);

    return f.getInputStream();
  }

  @Override
  public void save(String id, InputStream input) {
    gridFsOperations.store(input, id);
  }

  @Override
  public void save(String tempFileId) {
    save(tempFileId, tempFileService.getInputStreamFromFile(tempFileId));
    tempFileService.delete(tempFileId);
  }

  @Override
  public void delete(String id) {
    gridFsOperations.delete(new Query().addCriteria(Criteria.where("filename").is(id)));
  }
}