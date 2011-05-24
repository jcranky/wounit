/**
 * Copyright (C) 2009 hprange <hprange@gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.wounit.matchers

import org.hamcrest.CoreMatchers.not
import org.hamcrest.{Description, Matcher, StringDescription}
import com.webobjects.eocontrol.{EOEditingContext, EOEnterpriseObject}

/**
 * This class is the entry point for writing assertions to WebObjects related
 * unit tests.
 * <p>
 * Before start using this framework, remember to use the import static feature
 * in order to write more readable code:
 * 
 * <pre>
 * import static com.wounit.EOAssert.*;
 * </pre>
 * 
 * The <code>EOAssert</code> class provides static methods to check whether an
 * enterprise object can be saved or deleted and if it has been saved or
 * deleted.
 * <h4>Confirm that an enterprise object can/cannot be saved</h4>
 * 
 * <pre>
 * // Checks whether the eo can be saved
 * confirm(eo, canBeSaved());
 * </pre>
 * 
 * <pre>
 * // Checks whether the eo cannot be saved
 * confirm(eo, cannotBeSaved());
 * </pre>
 * 
 * <pre>
 * // Safer check whether eo cannot be saved
 * confirm(eo, cannotBeSavedBecause(&quot;The foo property cannot be null&quot;));
 * </pre>
 * 
 * <h4>Confirm that an enterprise object can/cannot be deleted</h4>
 * 
 * <pre>
 * // Checks whether the eo can be deleted
 * confirm(eo, canBeDeleted());
 * </pre>
 * 
 * <pre>
 * // Checks whether the eo cannot be deleted
 * confirm(eo, cannotBeDeleted());
 * </pre>
 * 
 * <pre>
 * // Safer check whether eo cannot be deleted
 * confirm(eo, cannotBeDeletedBecause(&quot;It is a required object&quot;));
 * </pre>
 * 
 * <h4>Confirm that an enterprise object has/hasn't been saved</h4>
 * 
 * <pre>
 * // Checks whether the eo has been saved and has no pending changes
 * confirm(eo, hasBeenSaved());
 * </pre>
 * 
 * <pre>
 * // Checks whether the eo has not been saved
 * confirm(eo, hasNotBeenSaved());
 * </pre>
 * 
 * <h4>Confirm that an enterprise object has/hasn't been deleted</h4>
 * 
 * <pre>
 * // Checks whether the eo has been deleted
 * confirm(eo, hasBeenDeleted());
 * </pre>
 * 
 * <pre>
 * // Checks whether the eo has not been deleted
 * confirm(eo, hasNotBeenDeleted());
 * </pre>
 * 
 * The <code>EOAssert</code> class also offers methods to check if an
 * <code>EOEditingContext</code> can save changes correctly.
 * <h4>Confirm that an editing context save changes successfully or not</h4>
 * 
 * <pre>
 * // Checks whether the editing context save changes successfully
 * confirm(editingContext, saveChanges());
 * </pre>
 * 
 * <pre>
 * // Checks whether the editing context does not save changes successfully
 * confirm(editingContext, doNotSaveChanges());
 * </pre>
 * 
 * <pre>
 * // Safer check whether the editing context does not save changes successfully
 * confirm(editingContext, doNotSaveChangesBecause(&quot;The bar property of Foo cannot be null&quot;));
 * </pre>
 * 
 * @author <a href="mailto:hprange@gmail.com">Henrique Prange</a>
 * @since 1.0
 * @see org.junit.Assert
 * @see org.junit.matchers.JUnitMatchers
 */
object EOAssert {
  /**
   * Used to check whether an enterprise object can be deleted without
   * throwing any exceptions.
   * 
   * @return a <code>Matcher</code> matching if the enterprise object can be
   *         deleted
   */
  def canBeDeleted(): Matcher[EOEnterpriseObject] = new CanBeDeletedMatcher[EOEnterpriseObject]

  /**
   * Used to check whether an enterprise object can be saved without throwing
   * any exceptions.
   * 
   * @return a <code>Matcher</code> matching if the enterprise object can be
   *         saved
   */
  def canBeSaved(): Matcher[EOEnterpriseObject] = new CanBeSavedMatcher[EOEnterpriseObject]

  /**
   * Used to check whether an enterprise object can <b>NOT</b> be deleted.
   * 
   * @return a <code>Matcher</code> matching if the enterprise object can
   *         <b>NOT</b> be deleted
   */
  def cannotBeDeleted(): Matcher[EOEnterpriseObject] = not(canBeDeleted())

  /**
   * Used to check whether an enterprise object can <b>NOT</b> be deleted
   * because of the matching expected exception.
   * 
   * @param message
   *            the expected exception message
   * @return a <code>Matcher</code> matching if the enterprise object can
   *         <b>NOT</b> be deleted
   */
  def cannotBeDeletedBecause(message: String): Matcher[EOEnterpriseObject] =
    not(new CanBeDeletedMatcher[EOEnterpriseObject](message))

  /**
   * Used to check whether an enterprise object can <b>NOT</b> be saved.
   * 
   * @return a <code>Matcher</code> matching if the enterprise object can
   *         <b>NOT</b> be saved
   */
  def cannotBeSaved(): Matcher[EOEnterpriseObject] = not(canBeSaved())

  /**
   * Used to check whether an enterprise object can <b>NOT</b> be saved
   * because of the matching expected exception.
   * 
   * @param message
   *            the expected exception message
   * @return a <code>Matcher</code> matching if the enterprise object can
   *         <b>NOT</b> be saved
   */
  def cannotBeSavedBecause(message: String): Matcher[EOEnterpriseObject] =
    not(new CanBeSavedMatcher[EOEnterpriseObject](message))

  /**
   * Confirms that <code>enterpriseObject</code> satisfies the condition
   * specified by <code>matcher</code>. If not, an {@link AssertionError} is
   * thrown with information about the matcher and failing value. Example:
   * 
   * <pre>
   * confirm(eo, hasBeenSaved());
   * </pre>
   * 
   * The confirm word was selected to avoid conflicts with assertThat from
   * JUnit and verify from most mock objects libraries.
   * 
   * @param <T>
   *            the static type accepted by the matcher
   * @param enterpriseObject
   *            the enterprise object to be checked
   * @param matcher
   *            an expression, built of {@link Matcher}s, specifying allowed
   *            values
   * @see org.hamcrest.CoreMatchers
   * @see org.junit.matchers.JUnitMatchers
   */
  def confirm(enterpriseObject: EOEnterpriseObject, matcher: Matcher[EOEnterpriseObject]) =
    confirmImplementation(enterpriseObject, matcher)

  /**
   * Confirms that <code>actual</code> satisfies the condition specified by
   * <code>matcher</code>. If not, an {@link AssertionError} is thrown with
   * information about the matcher and failing value. Example:
   * 
   * <pre>
   * confirm(editingContext, doNotSaveChanges());
   * </pre>
   * 
   * The confirm word was selected to avoid conflicts with assertThat from
   * JUnit and verify from most mock objects libraries.
   * 
   * @param <T>
   *            the static type accepted by the matcher
   * @param editingContext
   *            the editing context to be checked
   * @param matcher
   *            an expression, built of {@link Matcher}s, specifying allowed
   *            values
   * @see org.hamcrest.CoreMatchers
   * @see org.junit.matchers.JUnitMatchers
   */
  def confirm(editingContext: EOEditingContext, matcher: Matcher[EOEditingContext]) =
    confirmImplementation(editingContext, matcher)

  private def confirmImplementation[A](actual: A, matcher: Matcher[A]) {
    if (matcher.matches(actual)) {
      return
    }

    val description: Description = new StringDescription()

    description.appendText("\nExpected: ")
    description.appendDescriptionOf(matcher)

    throw new AssertionError(description.toString())
  }

  /**
   * Used to check whether an editing context is <b>NOT</b> able to save
   * changes.
   * 
   * @return a <code>Matcher</code> matching if the editing context can
   *         <b>NOT</b> be saved
   */
  def doNotSaveChanges(): Matcher[EOEditingContext] = not(saveChanges())

  /**
   * Used to check whether an editing context is <b>NOT</b> able to save
   * changes because of the matching expected exception.
   * 
   * @param message
   *            the expected exception message
   * @return a <code>Matcher</code> matching if the editing context can
   *         <b>NOT</b> be saved
   */
  def doNotSaveChangesBecause(message: String): Matcher[EOEditingContext] =
    not(new SaveChangesMatcher[EOEditingContext](message))

  /**
   * Used to check whether an enterprise object has been deleted and is no
   * longer managed by an editing context.
   * 
   * @return a <code>Matcher</code> matching if the enterprise object has been
   *         deleted
   */
  def hasBeenDeleted(): Matcher[EOEnterpriseObject] = new HasBeenDeletedMatcher[EOEnterpriseObject]()

  /**
   * Used to check whether an enterprise object has been saved and has no
   * pending changes to be saved.
   * 
   * @return a <code>Matcher</code> matching if the enterprise object has been
   *         saved
   */
  def hasBeenSaved(): Matcher[EOEnterpriseObject] = new HasBeenSavedMatcher[EOEnterpriseObject]()

  /**
   * Used to check whether an enterprise object has <b>NOT</b> been deleted
   * and is still managed by an editing context.
   * 
   * @return a <code>Matcher</code> matching if the enterprise object has
   *         <b>NOT</b> been deleted
   */
  def hasNotBeenDeleted(): Matcher[EOEnterpriseObject] = not(hasBeenDeleted())

  /**
   * Used to check whether an enterprise object has <b>NOT</b> been saved or
   * still has pending changes to be saved.
   * 
   * @return a <code>Matcher</code> matching if the enterprise object has
   *         <b>NOT</b> been saved
   */
  def hasNotBeenSaved(): Matcher[EOEnterpriseObject] = not(hasBeenSaved())

  /**
   * Used to check whether an editing context is able to save changes
   * successfully.
   * 
   * @return a <code>Matcher</code> matching if the editing context can save
   *         changes correctly
   */
  def saveChanges(): Matcher[EOEditingContext] = new SaveChangesMatcher[EOEditingContext]()
}
