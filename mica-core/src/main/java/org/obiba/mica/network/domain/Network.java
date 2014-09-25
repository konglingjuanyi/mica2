package org.obiba.mica.network.domain;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.URL;
import org.obiba.mica.core.domain.AbstractAuditableDocument;
import org.obiba.mica.core.domain.Authorization;
import org.obiba.mica.core.domain.Contact;
import org.obiba.mica.core.domain.LocalizedString;
import org.obiba.mica.file.Attachment;
import org.obiba.mica.study.domain.Study;

/**
 * A Network.
 */
public class Network extends AbstractAuditableDocument {

  private static final long serialVersionUID = -4271967393906681773L;

  @NotNull
  private LocalizedString name;

  private LocalizedString acronym;

  private boolean published = false;

  private List<Contact> investigators;

  private List<Contact> contacts;

  private LocalizedString description;

  @URL
  private String website;

  private List<Attachment> attachments;

  private LocalizedString infos;

  private List<String> studyIds;

  private Authorization maelstromAuthorization;

  //
  // Accessors
  //

  public LocalizedString getName() {
    return name;
  }

  public void setName(LocalizedString name) {
    this.name = name;
  }

  public LocalizedString getAcronym() {
    return acronym;
  }

  public void setAcronym(LocalizedString acronym) {
    this.acronym = acronym;
  }

  public boolean isPublished() {
    return published;
  }

  public void setPublished(boolean published) {
    this.published = published;
  }

  @NotNull
  public List<Contact> getInvestigators() {
    if(investigators == null) investigators = new ArrayList<>();
    return investigators;
  }

  public void addInvestigator(@NotNull Contact investigator) {
    getInvestigators().add(investigator);
  }

  public void setInvestigators(List<Contact> investigators) {
    this.investigators = investigators;
  }

  @NotNull
  public List<Contact> getContacts() {
    if(contacts == null) contacts = new ArrayList<>();
    return contacts;
  }

  public void addContact(@NotNull Contact contact) {
    getContacts().add(contact);
  }

  public void setContacts(List<Contact> contacts) {
    this.contacts = contacts;
  }

  public LocalizedString getDescription() {
    return description;
  }

  public void setDescription(LocalizedString description) {
    this.description = description;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  @NotNull
  public List<Attachment> getAttachments() {
    if(attachments == null) attachments = new ArrayList<>();
    return attachments;
  }

  public void addAttachment(@NotNull Attachment attachment) {
    getAttachments().add(attachment);
  }

  public void setAttachments(List<Attachment> attachments) {
    this.attachments = attachments;
  }

  public LocalizedString getInfos() {
    return infos;
  }

  public void setInfos(LocalizedString infos) {
    this.infos = infos;
  }

  @NotNull
  public List<String> getStudyIds() {
    if(studyIds == null) studyIds = new ArrayList<>();
    return studyIds;
  }

  public void addStudyId(@NotNull String studyId) {
    getStudyIds().add(studyId);
  }

  public void setStudyIds(List<String> studyIds) {
    this.studyIds = studyIds;
  }

  public void addStudy(@NotNull Study study) {
    if (!study.isNew()) addStudyId(study.getId());
  }

  public Authorization getMaelstromAuthorization() {
    return maelstromAuthorization;
  }

  public void setMaelstromAuthorization(Authorization maelstromAuthorization) {
    this.maelstromAuthorization = maelstromAuthorization;
  }

  @Override
  protected com.google.common.base.Objects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("name", name);
  }

}