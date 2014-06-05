package org.obiba.mica.domain;

import java.util.Locale;

import javax.annotation.Nullable;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public abstract class AbstractAttributeAware implements AttributeAware {

  private LinkedListMultimap<String, Attribute> attributes;

  @Override
  public Multimap<String, Attribute> getAttributes() {
    return attributes;
  }

  @Override
  public void setAttributes(LinkedListMultimap<String, Attribute> attributes) {
    this.attributes = attributes;
  }

  @Override
  public void addAttribute(Attribute attribute) {
    if(attributes == null) attributes = LinkedListMultimap.create();
    attributes.put(attribute.getMapKey(), attribute);
  }

  @Override
  public void removeAttribute(Attribute attribute) {
    if(attributes != null) {
      attributes.remove(attribute.getMapKey(), attribute);
    }
  }

  @Override
  public void removeAllAttributes() {
    if(attributes != null) attributes.clear();
  }

  @Override
  public boolean hasAttribute(String attName, @Nullable String namespace) {
    return attributes != null && attributes.containsKey(Attribute.getMapKey(attName, namespace));
  }

  @Override
  public boolean hasAttribute(String attName, @Nullable String namespace, @Nullable Locale locale) {
    try {
      getAttribute(attName, namespace, locale);
      return true;
    } catch(NoSuchAttributeException e) {
      return false;
    }
  }

  @Override
  public Attribute getAttribute(String attName, @Nullable String namespace, @Nullable Locale locale)
      throws NoSuchAttributeException {
    if(hasAttribute(attName, namespace)) {
      for(Attribute attribute : attributes.get(Attribute.getMapKey(attName, namespace))) {
        if(attribute.isLocalisedWith(locale)) {
          return attribute;
        }
      }
    }
    throw new NoSuchAttributeException(attName, getClass().getName());
  }

}