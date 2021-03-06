/*******************************************************************************
 * Copyright (c) 2010-2014, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.querybasedfeatures.tooling

import java.io.IOException
import java.util.ArrayList
import java.util.StringTokenizer
import org.eclipse.emf.ecore.EcoreFactory
import org.eclipse.incquery.patternlanguage.patternLanguage.Annotation
import org.eclipse.incquery.patternlanguage.patternLanguage.Pattern
import org.eclipse.incquery.querybasedfeatures.runtime.handler.QueryBasedFeatures

import static extension org.eclipse.incquery.patternlanguage.helper.CorePatternLanguageHelper.*

/**
 * @author Abel Hegedus
 *
 */
class SettingDelegateBasedGenerator {
  
  extension QueryBasedFeatureGenerator gen
  
  new(QueryBasedFeatureGenerator generator) {
    gen = generator
  }
  
  def protected void updateAnnotations(Pattern pattern, Annotation annotation, boolean generate) {
    try{
      val parameters = pattern.processDerivedFeatureAnnotation(annotation, generate)
      if(generate){
        updateEcorePackage(parameters)
        updateFeatureAnnotation(parameters)
      } else {
        removeFeatureAnnotation(parameters)
      }
      
      try{
        parameters.ePackage.eResource.save(null)
      } catch(IOException e){
        // validator already checks write access, cannot do anything about this here
      }
    } catch(IllegalArgumentException e){
      if(generate){
        logger.error(e.message,e);
      }
    }
  }
  
  def private updateEcorePackage(QueryBasedFeatureParameters parameters) {
    try{
      val pckg = parameters.ePackage
      val annotations = new ArrayList(pckg.EAnnotations)
      var ecoreAnnotation = annotations.findFirst[
        source == QueryBasedFeatures::ECORE_ANNOTATION
      ]
      if(ecoreAnnotation == null) {
        ecoreAnnotation = EcoreFactory::eINSTANCE.createEAnnotation
        ecoreAnnotation.source = QueryBasedFeatures::ECORE_ANNOTATION
        pckg.EAnnotations.add(ecoreAnnotation)
      }
      var entry = ecoreAnnotation.details.findFirst[
        key == QueryBasedFeatures::SETTING_DELEGATES_KEY
      ]
      if(entry == null) {
        // add entry ("patternFQN", pattern.fullyQualifiedName)
        ecoreAnnotation.details.put(QueryBasedFeatures::SETTING_DELEGATES_KEY, QueryBasedFeatures::ANNOTATION_SOURCE)
      } else {
        val delegates = new StringTokenizer(entry.value)
        while(delegates.hasMoreTokens){
          val delegate = delegates.nextToken
          if(delegate == QueryBasedFeatures::ANNOTATION_SOURCE){
            return
          }
        }
        entry.value = entry.value + " " + QueryBasedFeatures::ANNOTATION_SOURCE
      }
    } catch(Exception e){
      logger.warn(String.format("Error happened when trying to add QBF annotation to package %s in Ecore!",parameters.ePackage.name), e)
    }
  }
  
  def private updateFeatureAnnotation(QueryBasedFeatureParameters parameters) {
    try{
      val feat = parameters.feature
      val annotations = new ArrayList(feat.EAnnotations)
      annotations.forEach[
        if(it.source == QueryBasedFeatures::ANNOTATION_SOURCE) {
          feat.EAnnotations.remove(it)
        }
      ]
      val annotation = EcoreFactory::eINSTANCE.createEAnnotation
      annotation.source = QueryBasedFeatures::ANNOTATION_SOURCE
      feat.EAnnotations.add(annotation)

      annotation.details.put(QueryBasedFeatures::PATTERN_FQN_KEY, parameters.pattern.fullyQualifiedName)
    } catch(Exception e) {
      logger.warn(String.format("Error happened when trying to add QBF annotation to feature %s in Ecore!",parameters.feature.name), e)
    }
  }
  
  def private removeFeatureAnnotation(QueryBasedFeatureParameters parameters) {
    try{
      val feat = parameters.feature
      val annotations = new ArrayList(feat.EAnnotations)
      annotations.forEach[
        if(it.source == QueryBasedFeatures::ANNOTATION_SOURCE){
          feat.EAnnotations.remove(it)
        }
      ]
    } catch(Exception e){
      logger.warn(String.format("Error happened when trying to remove annotation to feature %s in Ecore!",parameters.feature), e)
    }
  }
  
}