/**
 */
package org.eclipse.incquery.runtime.rete.recipes;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Constant Recipe</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Simple node that stores constant values.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.incquery.runtime.rete.recipes.ConstantRecipe#getConstantValues <em>Constant Values</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.incquery.runtime.rete.recipes.RecipesPackage#getConstantRecipe()
 * @model
 * @generated
 */
public interface ConstantRecipe extends ReteNodeRecipe
{
  /**
   * Returns the value of the '<em><b>Constant Values</b></em>' attribute list.
   * The list contents are of type {@link java.lang.Object}.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * <!-- begin-model-doc -->
   * Stores constant values. May be empty.
   * 
   * TODO store constants as strings instead? (for easier serialization)
   * <!-- end-model-doc -->
   * @return the value of the '<em>Constant Values</em>' attribute list.
   * @see org.eclipse.incquery.runtime.rete.recipes.RecipesPackage#getConstantRecipe_ConstantValues()
   * @model unique="false"
   * @generated
   */
  EList<Object> getConstantValues();

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @model kind="operation" unique="false"
   *        annotation="http://www.eclipse.org/emf/2002/GenModel body='<%org.eclipse.incquery.runtime.rete.recipes.ConstantRecipe%> _this = this;\n<%org.eclipse.emf.common.util.EList%><<%java.lang.Object%>> _constantValues = _this.getConstantValues();\nreturn ((<%java.lang.Object%>[])<%org.eclipse.xtext.xbase.lib.Conversions%>.unwrapArray(_constantValues, <%java.lang.Object%>.class)).length;'"
   * @generated
   */
  int getArity();

} // ConstantRecipe
