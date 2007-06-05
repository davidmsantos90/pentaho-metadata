/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.pms.schema;

import java.util.Iterator;

import org.pentaho.pms.messages.Messages;
import org.pentaho.pms.schema.concept.ConceptInterface;
import org.pentaho.pms.schema.concept.ConceptUtilityBase;
import org.pentaho.pms.schema.concept.ConceptUtilityInterface;
import org.pentaho.pms.util.Const;
import org.pentaho.pms.util.Settings;

import be.ibridge.kettle.core.ChangedFlagInterface;
import be.ibridge.kettle.core.list.ObjectAlreadyExistsException;
import be.ibridge.kettle.core.list.UniqueArrayList;
import be.ibridge.kettle.core.list.UniqueList;

/**
 * A business category is a way of categorising selectable business fields.
 *  
 * @author Matt
 * @since  17-AUG-2006
 */
public class BusinessCategory extends ConceptUtilityBase implements ChangedFlagInterface, ConceptUtilityInterface, Cloneable
{
    private UniqueList businessCategories;
    private UniqueList businessColumns;
    
    private boolean rootCategory;
    
    /**
     * @return the rootCategory
     */
    public boolean isRootCategory()
    {
        return rootCategory;
    }

    /**
     * @param rootCategory the rootCategory to set
     */
    public void setRootCategory(boolean rootCategory)
    {
        this.rootCategory = rootCategory;
    }

    public BusinessCategory()
    {
        this(null);
    }
    
    public BusinessCategory(String id)
    {
        super(id);
        this.businessCategories = new UniqueArrayList();
        this.businessColumns = new UniqueArrayList();
    }
    
    /**
     * @return the description of the model element 
     */
    public String getModelElementDescription()
    {
        return Messages.getString("BusinessCategory.USER_DESCRIPTION"); //$NON-NLS-1$
    }
    
    public Object clone() 
    {
        BusinessCategory businessCategory = new BusinessCategory();
        try
        {
            businessCategory.setId(getId());
        }
        catch (ObjectAlreadyExistsException e)
        {
            // should be OK
        }
        businessCategory.setRootCategory(rootCategory);

        businessCategory.getBusinessColumns().clear(); // clear the list of column
        for (int i=0;i<nrBusinessColumns();i++)
        {
            try
            {
                businessCategory.addBusinessColumn(getBusinessColumn(i));
            }
            catch (ObjectAlreadyExistsException e)
            {
                throw new RuntimeException(e); // This should not happen, but I don't like to swallow the error.
            }
        }
        
        businessCategory.getBusinessCategories().clear();
        for (int i=0;i<nrBusinessCategories();i++)
        {
            try
            {
                businessCategory.addBusinessCategory((BusinessCategory) getBusinessCategory(i).clone());
            }
            catch (ObjectAlreadyExistsException e)
            {
                throw new RuntimeException(e); // This should not happen, but I don't like to swallow the error.
            }
        }
        
        businessCategory.setConcept( (ConceptInterface) getConcept().clone() ); // deep copy
        
        return businessCategory;
    }
    
    /**
     * 
     * @param tables List of categories to compare new category id against
     * @return a new BusinessCategory, duplicate of this, with only the id changed to be unique in it's list
     */
    public BusinessCategory cloneUnique (String locale, UniqueList categories){
      
      BusinessCategory businessCategory  = (BusinessCategory)clone(); 

      businessCategory.getBusinessColumns().clear(); // clear the list of column
      for (int i=0;i<nrBusinessColumns();i++)
      {
          try{
            // Do NOT clone business columns in a category; business columns must 
            // have a representative id under the business tables, cannot stand on 
            // their own under the categories...
            businessCategory.addBusinessColumn(getBusinessColumn(i));
          }
          catch (ObjectAlreadyExistsException e)
          {
              throw new RuntimeException(e); // This should not happen, but I don't like to swallow the error.
          }
      }
      
      String newId = proposeId(locale, null, this, categories);
      try {
        businessCategory.setId(newId);
      } catch (ObjectAlreadyExistsException e) {
        return null;
      }
      
      return businessCategory;
      
    }
 
    public static final String proposeId(String locale, BusinessTable table, BusinessCategory category)
    {
        String baseID = (table != null) ? Const.toID( table.getTargetTable()): "";  //$NON-NLS-1$
        String namePart = ((category !=null)&& (category.getDisplayName(locale)!=null)) ? Const.toID(category.getDisplayName(locale)) : "";  //$NON-NLS-1$
        String id = Settings.getBusinessCategoryIDPrefix() + baseID+"_" + namePart; //$NON-NLS-1$
        if (Settings.isAnIdUppercase()) id = id.toUpperCase();
        return id;
    }
    
    public static final String proposeId(String locale, BusinessTable businessTable, BusinessCategory category, UniqueList categories){
      boolean gotNew = false;
      boolean found = false;
      String id = proposeId(locale, businessTable, category);
      int catNr = 1;
      String newId = id;
      
      
      while (!gotNew) {
        
        for (Iterator iter = categories.iterator(); iter.hasNext();) {
          ConceptUtilityBase element = (ConceptUtilityBase) iter.next();
          if (element.getId().equalsIgnoreCase(newId)){
            found = true;
            break;
          }
        }
        if (found){
          catNr++;
          newId = id + "_" + catNr; //$NON-NLS-1$
          found = false;
        }else{
          gotNew = true;
        }
      }
      
      if (Settings.isAnIdUppercase())
        newId = newId.toUpperCase();
      
      return newId;
    }
    
        
    public UniqueList getBusinessCategories()
    {
        return businessCategories;
    }

    public void setBusinessCategories(UniqueList businessCategories)
    {
        this.businessCategories = businessCategories;
    }
    
    public int nrBusinessCategories()
    {
        return businessCategories.size();
    }
    
    public BusinessCategory getBusinessCategory(int index)
    {
        return (BusinessCategory)businessCategories.get(index);
    }
    
    public void removeBusinessCategory(int index)
    {
        businessCategories.remove(index);
        setChanged();
    }
    
    public void removeBusinessCategory(BusinessCategory category)
    {
        businessCategories.remove(category);
        setChanged();
    }

    public void addBusinessCategory(BusinessCategory businessCategory) throws ObjectAlreadyExistsException
    {
        businessCategories.add(businessCategory);
        setChanged();
    }
    
    public void addBusinessCategory(int index, BusinessCategory businessCategory) throws ObjectAlreadyExistsException
    {
        businessCategories.add(index, businessCategory);
        setChanged();
   }
    
    public int indexOfBusinessCategory(BusinessCategory businessCategory)
    {
        return businessCategories.indexOf(businessCategory);
    }
    
    /**
     * @return the businessColumns
     */
    public UniqueList getBusinessColumns()
    {
        return businessColumns;
    }

    /**
     * @param businessColumns the businessColumns to set
     */
    public void setBusinessColumns(UniqueList businessColumns)
    {
        this.businessColumns = businessColumns;
    }
    
    public int nrBusinessColumns()
    {
        return businessColumns.size();
    }

    public BusinessColumn getBusinessColumn(int i)
    {
        return (BusinessColumn) businessColumns.get(i);
    }

    public void addBusinessColumn(BusinessColumn businessColumn) throws ObjectAlreadyExistsException
    {
        businessColumns.add(businessColumn);
        setChanged();
    }
    
    public void addBusinessColumn(int i, BusinessColumn businessColumn)throws ObjectAlreadyExistsException
    {
        businessColumns.add(i, businessColumn);
        setChanged();
    }

    public int indexOfBusinessColumn(BusinessColumn businessColumn)
    {
        return businessColumns.indexOf(businessColumn);
    }

    public void removeBusinessColumn(int index)
    {
        businessColumns.remove(index);
        setChanged();
    }

    public void removeBusinessColumn(BusinessColumn obj)
    {
        businessColumns.remove(obj);
        setChanged();
    }

    /**
     * @param id The id of the business column
     * @return The business column or null if nothing could be found.
     */ 
    public BusinessColumn findBusinessColumn(String id)
    {
        return findBusinessColumn(id, true);
    }

    /**
     * Perform a (recursive) search in the categories tree and look for a business column with a certain ID
     * @param id
     * @param searchRecursive
     * @return
     */
    public BusinessColumn findBusinessColumn(String id, boolean searchRecursive)
    {
        if (searchRecursive)
        {
            for (int i=0;i<nrBusinessCategories();i++)
            {
                BusinessCategory businessCategory = getBusinessCategory(i);
                BusinessColumn businessColumn = businessCategory.findBusinessColumn(id, true);
                if (businessColumn!=null) return businessColumn;
            }
        }

        for (int i=0;i<nrBusinessColumns();i++)
        {
            BusinessColumn businessColumn = getBusinessColumn(i);
            if (businessColumn.getId().equalsIgnoreCase(id)) return businessColumn; 
        }
        
        return null;
    }

    /**
     * @param name The name of the business column or the id in case nothing was found
     * @param locale the locale to search in
     * @return The business column or null if nothing could be found.
     */ 
    public BusinessColumn findBusinessColumn(String name, String locale)
    {
        return findBusinessColumn(name, true, locale);
    }
    
    /**
     * @param name The name of the business column or if nothing found, the id
     * @param searchRecursive true if you want to search recursively through the child categories as well.
     * @param locale the locale to look for
     * @return The business column or null if nothing could be found.
     */ 
    public BusinessColumn findBusinessColumn(String name, boolean searchRecursive, String locale)
    {
        if (searchRecursive)
        {
            for (int i=0;i<nrBusinessCategories();i++)
            {
                BusinessCategory businessCategory = getBusinessCategory(i);
                BusinessColumn businessColumn = businessCategory.findBusinessColumn(name, true, locale);
                if (businessColumn!=null) return businessColumn;
            }
        }

        for (int i=0;i<nrBusinessColumns();i++)
        {
            BusinessColumn businessColumn = getBusinessColumn(i);
            if (businessColumn.getDisplayName(locale).equalsIgnoreCase(name)) return businessColumn; 
        }
        
        return findBusinessColumn(name, searchRecursive);
    }
    
    public boolean hasChanged()
    {
        if (super.hasChanged()) return true;
        
        for (int i=0;i<nrBusinessCategories();i++) if (getBusinessCategory(i).hasChanged()) return true;
        for (int i=0;i<nrBusinessColumns();i++) if (getBusinessColumn(i).hasChanged()) return true;
        
        return false;
    }
    
    public void clearChanged()
    {
        super.clearChanged();
        
        for (int i=0;i<nrBusinessCategories();i++) getBusinessCategory(i).clearChanged();
        for (int i=0;i<nrBusinessColumns();i++) getBusinessColumn(i).clearChanged();
    }


    /**
     * Finds a business category by looking at the ID
     * @param id the category ID to look out for
     * @return the business category or null if nothing was found
     */
    public BusinessCategory findBusinessCategory(String id)
    {
        for (int i=0;i<nrBusinessCategories();i++)
        {
            BusinessCategory businessCategory = getBusinessCategory(i);
            if (businessCategory.getId().equalsIgnoreCase(id)) return businessCategory;
        }
        return null;
    }

    /**
     * Find a business category by looking for the name in a certain locale.  If nothing is found, it also performs a search on category ID.
     * @param locale the locale to look in
     * @param categoryName the category name or ID to search for.
     * @return the category or null if nothing could be found
     */
    public BusinessCategory findBusinessCategory(String locale, String categoryName)
    {
        for (int i=0;i<nrBusinessCategories();i++)
        {
            BusinessCategory businessCategory = getBusinessCategory(i);
            if (businessCategory.getDisplayName(locale).equalsIgnoreCase(categoryName)) return businessCategory;
        }
        return findBusinessCategory(categoryName);
    }       
}
 